package com.polor.cloudimage.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.polor.cloudimage.constant.UserConstant;
import com.polor.cloudimage.exception.BusinessException;
import com.polor.cloudimage.exception.ErrorCode;
import com.polor.cloudimage.exception.ThrowUtils;
import com.polor.cloudimage.model.dto.UserLoginRequest;
import com.polor.cloudimage.model.dto.UserRegisterRequest;
import com.polor.cloudimage.model.entity.User;
import com.polor.cloudimage.model.enums.UserRoleEnum;
import com.polor.cloudimage.model.vo.LoginUserVO;
import com.polor.cloudimage.service.UserService;
import com.polor.cloudimage.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;

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
}




