package com.polar.cloudimage.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.polar.cloudimage.annotation.AuthCheck;
import com.polar.cloudimage.common.BaseResponse;
import com.polar.cloudimage.common.DeleteRequest;
import com.polar.cloudimage.common.ResultUtils;
import com.polar.cloudimage.constant.UserConstant;
import com.polar.cloudimage.exception.ErrorCode;
import com.polar.cloudimage.exception.ThrowUtils;
import com.polar.cloudimage.model.dto.user.*;
import com.polar.cloudimage.model.entity.User;
import com.polar.cloudimage.model.vo.LoginUserVO;
import com.polar.cloudimage.model.vo.UserVO;
import com.polar.cloudimage.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

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

    /**
     * 用户登录
     *
     * @param userLoginRequest 用户登录请求体
     * @param request          请求
     * @return 返回脱敏后的用户信息
     */
    @PostMapping("/login")
    @ApiOperation(value = "用户登录")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        LoginUserVO loginUserVO = userService.userLogin(userLoginRequest, request);
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 获取当前登录用户
     *
     * @param request 请求
     * @return 当前登录用户
     */
    @GetMapping("/get/login")
    @ApiOperation(value = "获取当前登录用户")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        LoginUserVO loginUserVO = userService.getLoginUserVo(loginUser);
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 用户注销(取消登录)
     *
     * @param request 请求
     * @return 是否注销成功
     */
    @PostMapping("/logout")
    @ApiOperation(value = "用户注销")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }


    /**
     * 添加用户（仅管理员可用）
     *
     * @param userAddRequest 用户添加请求体
     * @return 新用户id
     */
    @PostMapping("/add")
    @ApiOperation(value = "添加用户")
    @AuthCheck(mushRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        //检验参数
        ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);
        //属性复制
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        //设置默认密码
        final String DEFAULT_PASSWORD = "12345678";
        String encryptedPassword = userService.getEncryptedPassword(DEFAULT_PASSWORD);
        user.setUserPassword(encryptedPassword);
        //插入数据
        ThrowUtils.throwIf(!userService.save(user), ErrorCode.SYSTEM_ERROR, "添加用户失败");
        //返回结果
        return ResultUtils.success(user.getId());
    }


    /**
     * 根据id获取用户信息（仅管理员可用）
     *
     * @param id 用户id
     * @return 用户信息
     */
    @GetMapping("/get")
    @ApiOperation(value = "根据id获取用户信息")
    @AuthCheck(mushRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(Long id) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    /**
     * 根据id获取脱敏用户信息  给普通用户使用
     *
     * @param id 用户id
     * @return 脱敏用户信息
     */
    @GetMapping("/get/vo")
    @ApiOperation(value = "根据id获取脱敏用户信息")
    public BaseResponse<UserVO> getUserVoById(Long id) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        UserVO userVO = userService.getUserVo(user);
        return ResultUtils.success(userVO);
    }

    /**
     * 根据id删除用户
     *
     * @param deleteRequest 删除请求体
     * @return 是否删除成功
     */
    @PostMapping("/delete")
    @ApiOperation(value = "根据id删除用户")
    @AuthCheck(mushRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        //校验参数
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        boolean b = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }


    /**
     * 更新用户信息
     *
     * @param userUpdateRequest 用户更新请求体
     * @return 是否更新成功
     */
    @PostMapping("/update")
    @ApiOperation(value = "更新用户信息")
    @AuthCheck(mushRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        //校验参数
        ThrowUtils.throwIf(userUpdateRequest == null || userUpdateRequest.getId() == null || userUpdateRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        //给属性复制
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        //插入数据库
        ThrowUtils.throwIf(!userService.updateById(user), ErrorCode.SYSTEM_ERROR, "更新用户失败");
        //返回结果
        return ResultUtils.success(true);
    }

    @PostMapping("/list/page/vo")
    @ApiOperation(value = "分页获取脱敏用户信息列表")
    @AuthCheck(mushRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserVO>> listUserVoByPage(@RequestBody UserQueryRequest userQueryRequest) {
        //校验参数
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
        //分页
        int current = userQueryRequest.getCurrent();
        int pageSize = userQueryRequest.getPageSize();
        //分页条件
        LambdaQueryWrapper<User> lambdaQueryWrapper = userService.getLambdaQueryWrapper(userQueryRequest);
        Page<User> userPage = userService.page(new Page<>(current, pageSize), lambdaQueryWrapper);
        //封装脱敏数据
        Page<UserVO> userVOPage = new Page<>(current, pageSize, userPage.getTotal());
        List<UserVO> userVOList = userService.getUserVoList(userPage.getRecords());
        userVOPage.setRecords(userVOList);
        return ResultUtils.success(userVOPage);
    }

}
