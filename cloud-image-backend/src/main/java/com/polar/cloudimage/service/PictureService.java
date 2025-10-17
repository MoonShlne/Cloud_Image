package com.polar.cloudimage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.polar.cloudimage.model.dto.picture.PictureUploadRequest;
import com.polar.cloudimage.model.entity.Picture;
import com.polar.cloudimage.model.entity.User;
import com.polar.cloudimage.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

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
}
