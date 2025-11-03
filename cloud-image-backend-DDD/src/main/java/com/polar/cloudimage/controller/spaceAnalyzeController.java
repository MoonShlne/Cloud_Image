package com.polar.cloudimage.controller;

import com.polar.cloudimage.common.BaseResponse;
import com.polar.cloudimage.common.ResultUtils;
import com.polar.cloudimage.exception.ErrorCode;
import com.polar.cloudimage.exception.ThrowUtils;
import com.polar.cloudimage.model.dto.space.analyze.*;
import com.polar.cloudimage.model.entity.Space;
import com.polar.cloudimage.model.entity.User;
import com.polar.cloudimage.model.vo.space.analyze.*;
import com.polar.cloudimage.service.SpaceAnalyzeService;
import com.polar.cloudimage.service.SpaceService;
import com.polar.cloudimage.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.ResponseUtil;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author polar
 * @version 1.0
 * @since 2025/10/27 14:39
 */
@RestController
@Slf4j
@Api(tags = "空间分析接口")
@RequestMapping("/space/analyze")
public class spaceAnalyzeController {

    @Resource
    private UserService userService;
    @Resource
    private SpaceService SpaceService;

    @Resource
    private SpaceAnalyzeService spaceAnalyzeService;


    /**
     * 空间使用情况分析
     *
     * @param spaceUsageAnalyzeRequest 空间使用情况分析请求
     * @param request                  请求
     * @return 空间使用情况分析响应
     */
    @PostMapping("/usage")
    @ApiOperation(value = "空间使用情况分析")
    public BaseResponse<SpaceUsageAnalyzeResponse> getSpaceUsageAnalyze(
            @RequestBody SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, HttpServletRequest request) {
        //校验参数
        ThrowUtils.throwIf(spaceUsageAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        //获取登录用户
        User loginUser = userService.getLoginUser(request);
        SpaceUsageAnalyzeResponse analyzeResponse = spaceAnalyzeService.getSpaceUsageAnalyze(spaceUsageAnalyzeRequest, loginUser);
        return ResultUtils.success(analyzeResponse);
    }
    /**
     * 空间图片分类分析
     *
     * @param spaceCategoryAnalyzeRequest 空间图片分类分析请求
     * @param request                     请求
     * @return 空间图片分类分析响应
     */
    @PostMapping("/category")
    @ApiOperation(value = "空间图片分类分析")
    public BaseResponse<List<SpaceCategoryAnalyzeResponse>> getSpaceCategoryAnalyze(
            @RequestBody SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, HttpServletRequest request) {
        //校验参数
        ThrowUtils.throwIf(spaceCategoryAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        //获取登录用户
        User loginUser = userService.getLoginUser(request);
        List<SpaceCategoryAnalyzeResponse> spaceCategoryAnalyze = spaceAnalyzeService.getSpaceCategoryAnalyze(spaceCategoryAnalyzeRequest, loginUser);
        return ResultUtils.success(spaceCategoryAnalyze);
    }

    /**
     * 空间图片标签分析
     *
     * @param spaceTagAnalyzeRequest 空间图片标签分析请求
     * @param request                 请求
     * @return 空间图片标签分析响应
     */
    @PostMapping("/tag")
    @ApiOperation(value = "空间图片标签分析")
    public  BaseResponse<List<SpaceTagAnalyzeResponse>> getSpaceTagAnalyze(
            @RequestBody SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, HttpServletRequest request) {
        //校验参数
        ThrowUtils.throwIf(spaceTagAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        //获取登录用户
        User loginUser = userService.getLoginUser(request);
        List<SpaceTagAnalyzeResponse> spaceTagAnalyze = spaceAnalyzeService.getSpaceTagAnalyze(spaceTagAnalyzeRequest, loginUser);
        return ResultUtils.success(spaceTagAnalyze);
    }

    /**
     * 空间图片大小分析
     *
     * @param spaceSizeAnalyzeRequest 空间图片大小分析请求
     * @param request                 请求
     * @return 空间图片大小分析响应
     */
    @PostMapping("/size")
    @ApiOperation(value = "空间图片大小分析")
    public  BaseResponse<List<SpaceSizeAnalyzeResponse>> getSpaceSizeAnalyze(
            @RequestBody SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, HttpServletRequest request) {
        //校验参数
        ThrowUtils.throwIf(spaceSizeAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        //获取登录用户
        User loginUser = userService.getLoginUser(request);
        List<SpaceSizeAnalyzeResponse> spaceSizeAnalyze = spaceAnalyzeService.getSpaceSizeAnalyze(spaceSizeAnalyzeRequest, loginUser);
        return ResultUtils.success(spaceSizeAnalyze);
    }


    /**
     * 空间用户上传行为分析
     *
     * @param spaceUserAnalyzeRequest 空间用户上传行为分析请求
     * @param request                 请求
     * @return 空间用户上传行为分析响应
     */
    @PostMapping("/user")
    @ApiOperation(value = "空间用户上传行为分析")
    public  BaseResponse<List<SpaceUserAnalyzeResponse>> getSpaceUserAnalyze(
            @RequestBody SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, HttpServletRequest request) {
        //校验参数
        ThrowUtils.throwIf(spaceUserAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        //获取登录用户
        User loginUser = userService.getLoginUser(request);
        List<SpaceUserAnalyzeResponse> spaceUserAnalyze = spaceAnalyzeService.getSpaceUserAnalyze(spaceUserAnalyzeRequest, loginUser);
        return ResultUtils.success(spaceUserAnalyze);
    }

    /**
     * 空间使用排行分析
     *
     * @param spaceRankAnalyzeRequest 空间使用排行分析请求
     * @param request                 请求
     * @return 空间使用排行分析响应
     */
    @PostMapping("/rank")
    @ApiOperation(value = "空间使用排行分析")
    public BaseResponse<List<Space>> getSpaceRankAnalyze(
            @RequestBody SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, HttpServletRequest request) {
        //校验参数
        ThrowUtils.throwIf(spaceRankAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        //获取登录用户
        User loginUser = userService.getLoginUser(request);
        List<Space> spaceSizeAnalyze = spaceAnalyzeService.getSpaceRankAnalyze(spaceRankAnalyzeRequest, loginUser);
        return ResultUtils.success(spaceSizeAnalyze);
    }

}
