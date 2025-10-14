package com.polor.cloudimage.service;

import cn.hutool.http.server.HttpServerRequest;
import com.polor.cloudimage.model.dto.UserLoginRequest;
import com.polor.cloudimage.model.dto.UserRegisterRequest;
import com.polor.cloudimage.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.polor.cloudimage.model.vo.LoginUserVO;

import javax.servlet.http.HttpServletRequest;

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
}
