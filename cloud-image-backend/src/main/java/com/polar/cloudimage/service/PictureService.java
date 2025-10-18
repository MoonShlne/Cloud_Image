package com.polar.cloudimage.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.polar.cloudimage.model.dto.picture.PictureQueryRequest;
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
     * @param multipartFile        文件
     * @param pictureUploadRequest 图片上传请求
     * @param loginUser            登录用户
     * @return 图片视图
     */
    PictureVO uploadPicture(MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, User loginUser);


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
}
