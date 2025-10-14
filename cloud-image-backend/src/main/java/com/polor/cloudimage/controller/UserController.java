package com.polor.cloudimage.controller;

import com.polor.cloudimage.common.BaseResponse;
import com.polor.cloudimage.common.ResultUtils;
import com.polor.cloudimage.exception.ErrorCode;
import com.polor.cloudimage.exception.ThrowUtils;
import com.polor.cloudimage.model.dto.UserLoginRequest;
import com.polor.cloudimage.model.dto.UserRegisterRequest;
import com.polor.cloudimage.model.vo.LoginUserVO;
import com.polor.cloudimage.service.UserService;
import com.polor.cloudimage.service.impl.UserServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author polar
 * @version 1.0
 * @since 2025/10/14 13:34
 */
@RestController
@Slf4j
@RequestMapping("/user")
@Api(tags = "用户接口")
public class UserController {

    @Resource
    private UserService userService;


    /**
     * 用户注册
     *
     * @param userRegisterRequest 用户注册请求体
     * @return 新用户id
     */
    @PostMapping("/register")
    @ApiOperation(value = "用户注册")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.NOT_FOUND_ERROR);
        long result = userService.userRegister(userRegisterRequest);
        return ResultUtils.success(result);
    }

    @PostMapping("/login")
    @ApiOperation(value = "用户登录")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        LoginUserVO loginUserVO = userService.userLogin(userLoginRequest, request);
        return ResultUtils.success(loginUserVO);
    }


}
