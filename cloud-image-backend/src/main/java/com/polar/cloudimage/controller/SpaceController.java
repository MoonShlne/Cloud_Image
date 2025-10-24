package com.polar.cloudimage.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.polar.cloudimage.annotation.AuthCheck;
import com.polar.cloudimage.common.BaseResponse;
import com.polar.cloudimage.common.DeleteRequest;
import com.polar.cloudimage.common.ResultUtils;
import com.polar.cloudimage.constant.UserConstant;
import com.polar.cloudimage.exception.BusinessException;
import com.polar.cloudimage.exception.ErrorCode;
import com.polar.cloudimage.exception.ThrowUtils;
import com.polar.cloudimage.model.dto.space.*;
import com.polar.cloudimage.model.entity.Space;
import com.polar.cloudimage.model.entity.User;
import com.polar.cloudimage.model.enums.SpaceLevelEnum;
import com.polar.cloudimage.model.vo.SpaceVO;
import com.polar.cloudimage.service.SpaceService;
import com.polar.cloudimage.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author polar
 * @version 1.0
 * @since 2025/10/16 16:36
 */
@RestController
@Slf4j
@Api(tags = "空间接口")
@RequestMapping("/space")
public class SpaceController {

    @Resource
    private UserService userService;
    @Resource
    private SpaceService SpaceService;


    /**
     * 添加空间
     *
     * @param spaceAddRequest 空间添加请求
     * @param request         请求
     * @return 新增空间id
     */
    @PostMapping("/add")
    public BaseResponse<Long> addSpace(@RequestBody SpaceAddRequest spaceAddRequest,
                                       HttpServletRequest request) {
        // 校验参数
        ThrowUtils.throwIf(spaceAddRequest == null, ErrorCode.PARAMS_ERROR);
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        long newSpaceId = SpaceService.addSpace(spaceAddRequest, loginUser);
        return ResultUtils.success(newSpaceId);
    }

    /**
     * 删除空间
     *
     * @param deleteRequest 删除请求
     * @param request       请求
     * @return 是否删除成功
     */
    @PostMapping("/delete")
    @ApiOperation(value = "删除空间")
    public BaseResponse<Boolean> deleteSpace(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        //校验参数
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        //获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        Long id = loginUser.getId();
        //仅管理员或本人可删除空间
        ThrowUtils.throwIf((!userService.isAdmin(loginUser) && !id.equals(deleteRequest.getId())), ErrorCode.NO_AUTH_ERROR);
        //操作数据库
        boolean b = SpaceService.removeById(deleteRequest.getId());
        ThrowUtils.throwIf(!b, ErrorCode.SYSTEM_ERROR, "删除空间失败");
        return ResultUtils.success(true);
    }

    /**
     * 更新空间信息
     *
     * @param SpaceUpdateRequest 空间更新请求体
     * @return 是否更新成功
     */
    @PostMapping("/update")
    @AuthCheck(mushRole = UserConstant.ADMIN_ROLE)
    @ApiOperation(value = "更新空间信息")
    public BaseResponse<Boolean> updateSpace(@RequestBody SpaceUpdateRequest SpaceUpdateRequest, HttpServletRequest request) {

        if (SpaceUpdateRequest == null || SpaceUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 将实体类和 DTO 进行转换
        Space Space = new Space();
        BeanUtils.copyProperties(SpaceUpdateRequest, Space);
        // 数据校验
        SpaceService.validSpace(Space, false);
        //填充参数
        SpaceService.fillSpaceBySpaceLevel(Space);
        // 判断是否存在
        long id = SpaceUpdateRequest.getId();
        Space oldSpace = SpaceService.getById(id);
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = SpaceService.updateById(Space);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据id获取空间信息 仅管理员可用
     *
     * @param id      空间id
     * @param request 请求
     * @return 空间信息
     */
    @GetMapping("/get")
    @AuthCheck(mushRole = UserConstant.ADMIN_ROLE)
    @ApiOperation(value = "根据id获取空间信息")
    public BaseResponse<Space> getSpaceById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Space Space = SpaceService.getById(id);
        ThrowUtils.throwIf(Space == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(Space);
    }


    /**
     * 根据id获取空间视图 给普通用户使用
     *
     * @param id      空间id
     * @param request 请求
     * @return 空间视图
     */
    @GetMapping("/get/vo")
    @ApiOperation(value = "根据id获取空间视图")
    public BaseResponse<SpaceVO> getSpaceVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Space Space = SpaceService.getById(id);
        ThrowUtils.throwIf(Space == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(SpaceService.getSpaceVO(Space, request));
    }


    /**
     * 分页获取空间列表 仅管理员可用
     *
     * @param SpaceQueryRequest 空间查询请求体
     * @return 空间分页
     */
    @PostMapping("/list/page")
    @AuthCheck(mushRole = UserConstant.ADMIN_ROLE)
    @ApiOperation(value = "分页获取空间列表")
    public BaseResponse<Page<Space>> listSpaceByPage(@RequestBody SpaceQueryRequest SpaceQueryRequest) {
        long current = SpaceQueryRequest.getCurrent();
        long size = SpaceQueryRequest.getPageSize();
        // 查询数据库
        Page<Space> SpacePage = SpaceService.page(new Page<>(current, size),
                SpaceService.getQueryWrapper(SpaceQueryRequest));
        return ResultUtils.success(SpacePage);
    }

    /**
     * 分页获取空间视图列表 给普通用户使用
     * 实现多级缓存
     *
     * @param SpaceQueryRequest 空间查询请求体
     * @param request           请求
     * @return 空间视图分页
     */
    @PostMapping("/list/page/vo")
    @ApiOperation(value = "分页获取空间视图列表")
    public BaseResponse<Page<SpaceVO>> listSpaceVOByPage(@RequestBody SpaceQueryRequest SpaceQueryRequest,
                                                         HttpServletRequest request) {
        long current = SpaceQueryRequest.getCurrent();
        long size = SpaceQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<Space> SpacePage = SpaceService.page(new Page<>(current, size),
                SpaceService.getQueryWrapper(SpaceQueryRequest));
        // 获取封装类分页
        return ResultUtils.success(SpaceService.getSpaceVOPage(SpacePage, request));
    }

    /**
     * 编辑空间信息
     *
     * @param SpaceEditRequest 空间编辑请求体
     * @param request          请求
     * @return 是否编辑成功
     */
    @PostMapping("/edit")
    @ApiOperation(value = "编辑空间信息")
    public BaseResponse<Boolean> editSpace(@RequestBody SpaceEditRequest SpaceEditRequest, HttpServletRequest request) {
        if (SpaceEditRequest == null || SpaceEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 在此处将实体类和 DTO 进行转换
        Space Space = new Space();
        BeanUtils.copyProperties(SpaceEditRequest, Space);
        //数据填充
        SpaceService.fillSpaceBySpaceLevel(Space);
        // 设置编辑时间
        Space.setEditTime(new Date());
        // 数据校验
        SpaceService.validSpace(Space, false);
        User loginUser = userService.getLoginUser(request);
        // 判断是否存在
        long id = SpaceEditRequest.getId();
        Space oldSpace = SpaceService.getById(id);
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldSpace.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = SpaceService.updateById(Space);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 获取空间等级列表
     *
     * @return 空间等级列表
     */
    @GetMapping("list/level")
    @ApiOperation(value = "获取空间等级列表")
    public BaseResponse<List<SpaceLevel>> listSpaceLevel() {
        List<SpaceLevel> collect = Arrays.stream(SpaceLevelEnum.values())
                .map(spaceLevelEnum -> new SpaceLevel(spaceLevelEnum.getValue()
                        , spaceLevelEnum.getText(),
                        spaceLevelEnum.getMaxCount()
                        , spaceLevelEnum.getMaxSize()))
                .collect(Collectors.toList());

        return ResultUtils.success(collect);
    }

}
