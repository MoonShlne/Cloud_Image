package com.polar.cloudimage.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.polar.cloudimage.common.BaseResponse;
import com.polar.cloudimage.model.dto.picture.PictureQueryRequest;
import com.polar.cloudimage.model.dto.picture.PictureReviewRequest;
import com.polar.cloudimage.model.dto.picture.PictureUploadByBatchRequest;
import com.polar.cloudimage.model.dto.picture.PictureUploadRequest;
import com.polar.cloudimage.model.entity.Picture;
import com.polar.cloudimage.model.entity.User;
import com.polar.cloudimage.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
 * @author polar
 * @description 针对表【picture(图片)】的数据库操作Service
 * @createDate 2025-10-15 16:51:05
 */
public interface PictureService extends IService<Picture> {

    /**
     * 上传图片
     *
     * @param inputSource        源文件
     * @param pictureUploadRequest 图片上传请求
     * @param loginUser            登录用户
     * @return 图片视图
     */
    PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser);


    /**     * 获取图片查询包装类
     *
     * @param pictureQueryRequest 图片查询请求体
     * @return 图片查询包装类
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * 获取图片视图
     *
     * @param picture 图片
     * @param request 请求
     * @return 图片视图
     */
    PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    /**
     * 获取图片视图分页
     *
     * @param picturePage 图片分页
     * @param request     请求
     * @return 图片视图分页
     */
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

    /**
     * 校验图片信息
     *
     * @param picture 图片信息
     */
    void validPicture(Picture picture);

    /**
     * 图片审核
     *
     * @param pictureReviewRequest 图片审核请求
     * @param loginUser            登录用户
     */
    void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);

    /**
     * 填充图片审核参数
     *
     * @param picture   图片
     * @param loginUser 登录用户
     */
    void fillReviewParams(Picture picture, User loginUser);

    /**
     * 批量上传图片
     *
     * @param pictureUploadByBatchRequest 批量上传请求
     * @param loginUser                   登录用户
     * @return 上传成功数量
     */
    Integer uploadPictureByBach(PictureUploadByBatchRequest pictureUploadByBatchRequest,User loginUser);

}
