package com.polar.cloudimage.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.polar.cloudimage.constant.UserConstant;
import com.polar.cloudimage.exception.BusinessException;
import com.polar.cloudimage.exception.ErrorCode;
import com.polar.cloudimage.exception.ThrowUtils;
import com.polar.cloudimage.model.dto.user.UserLoginRequest;
import com.polar.cloudimage.model.dto.user.UserQueryRequest;
import com.polar.cloudimage.model.dto.user.UserRegisterRequest;
import com.polar.cloudimage.model.entity.User;
import com.polar.cloudimage.model.enums.UserRoleEnum;
import com.polar.cloudimage.model.vo.LoginUserVO;
import com.polar.cloudimage.model.vo.UserVO;
import com.polar.cloudimage.service.UserService;
import com.polar.cloudimage.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author polar
 * @description 针对表【user(用户)】的数据库操作Service实现
 * @createDate 2025-10-14 12:52:49
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    /**
     * 用户注册
     *
     * @param userRegisterRequest 用户注册请求体
     * @return 新用户id
     */
    @Override
    public long userRegister(UserRegisterRequest userRegisterRequest) {
        //1 校验参数
        if (StrUtil.hasBlank(userRegisterRequest.getUserAccount(), userRegisterRequest.getUserPassword(), userRegisterRequest.getCheckPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userRegisterRequest.getUserAccount().length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号长度不能小于4");
        }
        if (userRegisterRequest.getUserPassword().length() < 8 || userRegisterRequest.getCheckPassword().length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度不能小于8");
        }
        if (!userRegisterRequest.getUserPassword().equals(userRegisterRequest.getCheckPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        //2 检查账号是否重复
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(User::getUserAccount, userRegisterRequest.getUserAccount());
        Long count = this.baseMapper.selectCount(userLambdaQueryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号已存在");
        }
        //3 加密密码
        String encryptPassword = getEncryptedPassword(userRegisterRequest.getUserPassword());
        //4 插入数据
        User user = new User();
        user.setUserAccount(userRegisterRequest.getUserAccount());
        user.setUserPassword(encryptPassword);
        user.setUserName("用户" + userRegisterRequest.getUserAccount());
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean save = this.save(user);
        ThrowUtils.throwIf(!save, ErrorCode.SYSTEM_ERROR, "注册失败");
        //5 返回新用户id  mybatis-plus会自动回填id
        return user.getId();
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest 用户登录请求体
     * @return 返回脱敏后的用户信息
     */
    @Override
    public LoginUserVO userLogin(UserLoginRequest userLoginRequest, HttpServletRequest request) {
        //1 校验参数
        if (StrUtil.hasBlank(userLoginRequest.getUserAccount(), userLoginRequest.getUserPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userLoginRequest.getUserAccount().length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号长度不能小于4");
        }
        if (userLoginRequest.getUserPassword().length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度不能小于8");
        }

        //2 加密密码 校验数据库
        String encryptedPassword = getEncryptedPassword(userLoginRequest.getUserPassword());
        //3 查询数据库是否存在
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(User::getUserAccount, userLoginRequest.getUserAccount());
        userLambdaQueryWrapper.eq(User::getUserPassword, encryptedPassword);
        User user = this.baseMapper.selectOne(userLambdaQueryWrapper);

        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 保存用户登录状态
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
        return this.getLoginUserVo(user);


    }

    /**
     * 获取当前登录用户
     *
     * @param request 请求
     * @return 当前登录用户
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        Object attribute = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser = (User) attribute;
        // 校验用户是否登录
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询用户最新信息
        Long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }


    /**
     * 获取加密后的密码
     *
     * @param password 明文密码
     * @return 加密后密码
     */
    public String getEncryptedPassword(String password) {
        //加盐
        final String SALT = "polor";
        return DigestUtils.md5DigestAsHex((SALT + password).getBytes());
    }


    /**
     * 获取脱敏后的用户信息
     *
     * @param user 用户信息
     * @return 脱敏后的用户信息
     */
    @Override
    public LoginUserVO getLoginUserVo(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    /**
     * 获取脱敏后的用户信息
     *
     * @param user 用户信息
     * @return 脱敏后的用户信息
     */
    @Override
    public UserVO getUserVo(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    /**
     * 获取脱敏后的用户信息列表
     *
     * @param userList 用户信息列表
     * @return 脱敏后的用户信息列表
     */
    @Override
    public List<UserVO> getUserVoList(List<User> userList) {
        if (userList == null) {
            return new ArrayList<>();
        }
//        List<UserVO> userVOList = new ArrayList<>();
//        for (User user : userList) {
//            UserVO userVO = new UserVO();
//            BeanUtils.copyProperties(user, userVO);
//            userVOList.add(userVO);
//        }
//        return  userVOList;
        // 方式二：使用Java 8的Stream API
        return userList
                .stream()
                .map(this::getUserVo)
                .collect(Collectors.toList());

    }

    /**
     * 用户注销
     *
     * @param request 请求
     * @return 是否注销成功
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        //判断是否登录
        Object attribute = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (attribute == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        //删除登录信息
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return true;
    }

    @Override
    public LambdaQueryWrapper<User> getLambdaQueryWrapper(UserQueryRequest userQueryRequest) {
        //校验参数
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);

        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //拼接查询条件
        userLambdaQueryWrapper.eq(userQueryRequest.getId() != null, User::getId, userQueryRequest.getId());  //id
          //userRole
        userLambdaQueryWrapper.eq(StrUtil.isNotBlank(userQueryRequest.getUserRole()), User::getUserRole, userQueryRequest.getUserRole());
        //userAccount
        userLambdaQueryWrapper.like(StrUtil.isNotBlank(userQueryRequest.getUserAccount()), User::getUserAccount, userQueryRequest.getUserAccount());
        //userName
        userLambdaQueryWrapper.like(StrUtil.isNotBlank(userQueryRequest.getUserName()), User::getUserName, userQueryRequest.getUserName());
        //userProfile
        userLambdaQueryWrapper.like(StrUtil.isNotBlank(userQueryRequest.getUserProfile()), User::getUserProfile, userQueryRequest.getUserProfile());
        //sort
        userLambdaQueryWrapper.orderBy(StrUtil.isNotEmpty(userQueryRequest.getSortField()) ,userQueryRequest.getSortOrder().equals("ascend"), User::getId);

        return userLambdaQueryWrapper;

    }
}




