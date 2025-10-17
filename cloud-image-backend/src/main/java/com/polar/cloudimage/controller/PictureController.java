package com.polar.cloudimage.controller;

import com.polar.cloudimage.annotation.AuthCheck;
import com.polar.cloudimage.common.BaseResponse;
import com.polar.cloudimage.common.ResultUtils;
import com.polar.cloudimage.constant.UserConstant;
import com.polar.cloudimage.model.dto.picture.PictureUploadRequest;
import com.polar.cloudimage.model.entity.User;
import com.polar.cloudimage.model.vo.PictureVO;
import com.polar.cloudimage.service.PictureService;
import com.polar.cloudimage.service.UserService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author polar
 * @version 1.0
 * @since 2025/10/16 16:36
 */
@RestController
@Slf4j
@Api(tags = "图片接口")
@RequestMapping("/picture")
public class PictureController {

    @Resource
    private UserService userService;
    @Resource
    private PictureService pictureService;


    /**
     * 上传图片 &更新图片
     *
     * @param multipartFile        文件
     * @param pictureUploadRequest 图片上传请求
     * @param request              请求
     * @return 图片视图
     */
    @PostMapping("/upload")
    @AuthCheck(mushRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<PictureVO> uploadPicture(@RequestPart("file") MultipartFile multipartFile
            , PictureUploadRequest pictureUploadRequest
            , HttpServletRequest request) {

        //获取登录用户
        User loginUser = userService.getLoginUser(request);
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);

        return ResultUtils.success(pictureVO);

    }

}
