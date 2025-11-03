package com.polar.cloudimage.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.polar.cloudimage.model.dto.user.UserLoginRequest;
import com.polar.cloudimage.model.dto.user.UserQueryRequest;
import com.polar.cloudimage.model.dto.user.UserRegisterRequest;
import com.polar.cloudimage.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.polar.cloudimage.model.vo.LoginUserVO;
import com.polar.cloudimage.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author polar
 * @description 针对表【user(用户)】的数据库操作Service
 * @createDate 2025-10-14 12:52:49
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userRegisterRequest 用户注册请求体
     * @return 新用户id
     */
    long userRegister(UserRegisterRequest userRegisterRequest);


    /**
     * 用户登录
     *
     * @param userLoginRequest 用户登录请求体
     * @return 返回脱敏后的用户信息
     */
    LoginUserVO userLogin(UserLoginRequest userLoginRequest, HttpServletRequest request);


    /**     * 获取当前登录用户
     *
     * @param request 请求
     * @return 当前登录用户
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 获取加密后的密码
     *
     * @param password 明文密码
     * @return 加密后密码
     */
    String getEncryptedPassword(String password);

    /**
     * 获取脱敏后的用户信息
     *
     * @param user 用户信息
     * @return 脱敏后的用户信息
     */
    LoginUserVO getLoginUserVo(User user);


    /**
     * 获取脱敏后的用户信息
     *
     * @param user 用户信息
     * @return 脱敏后的用户信息
     */
    UserVO getUserVo(User user);


    /**
     * 获取脱敏后的用户信息列表
     *
     * @param userList 用户信息列表
     * @return 脱敏后的用户信息列表
     */
    List<UserVO> getUserVoList(List<User> userList);

    /**
     * 用户注销
     *
     * @param request 请求
     * @return 是否注销成功
     */
    boolean userLogout(HttpServletRequest request);


    /**
     * 封装用户分页查询wrapper
     * @param userQueryRequest 用户查询请求体
     * @return 用户查询条件
     */
    LambdaQueryWrapper<User>  getLambdaQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 判断用户是否为管理员
     *
     * @param user 用户信息
     * @return 是否为管理员
     */
    boolean isAdmin(User user);
}


