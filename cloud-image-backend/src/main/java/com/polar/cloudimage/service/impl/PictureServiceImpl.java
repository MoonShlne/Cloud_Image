package com.polar.cloudimage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.polar.cloudimage.exception.ErrorCode;
import com.polar.cloudimage.exception.ThrowUtils;
import com.polar.cloudimage.manager.FileManager;
import com.polar.cloudimage.mapper.PictureMapper;
import com.polar.cloudimage.model.dto.file.UploadPictureResult;
import com.polar.cloudimage.model.dto.picture.PictureUploadRequest;
import com.polar.cloudimage.model.entity.Picture;
import com.polar.cloudimage.model.entity.User;
import com.polar.cloudimage.model.vo.PictureVO;
import com.polar.cloudimage.service.PictureService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

/**
 * @author polar
 * @description 针对表【picture(图片)】的数据库操作Service实现
 * @createDate 2025-10-15 16:51:05
 */
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
        implements PictureService {

    private final FileManager fileManager;

    public PictureServiceImpl(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    /**
     * 上传图片
     *
     * @param multipartFile        文件
     * @param pictureUploadRequest 图片上传请求
     * @param loginUser            登录用户
     * @return 图片视图
     */
    @Override
    public PictureVO uploadPicture(MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, User loginUser) {
        //校验参数
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        //上传图片
        String uploadPathPrefix = String.format("public/%s", loginUser.getId());
        UploadPictureResult uploadPictureResult = fileManager.uploadPicture(multipartFile, uploadPathPrefix);

        //构造返回值信息

        String url = uploadPictureResult.getUrl();
        String picName = uploadPictureResult.getPicName();
        Long picSize = uploadPictureResult.getPicSize();
        int picWidth = uploadPictureResult.getPicWidth();
        int picHeight = uploadPictureResult.getPicHeight();
        Double picScale = uploadPictureResult.getPicScale();
        String picFormat = uploadPictureResult.getPicFormat();


        Picture picture = new Picture();
        picture.setUrl(url);
        picture.setName(picName);
        picture.setPicSize(picSize);
        picture.setPicWidth(picWidth);
        picture.setPicHeight(picHeight);
        picture.setPicScale(picScale);
        picture.setPicFormat(picFormat);
        picture.setUserId(loginUser.getId());

        //如果是更新图片，为editTime赋值，以及pictureId赋值
        if (pictureUploadRequest.getId() != null && pictureUploadRequest.getId() > 0) {
            picture.setId(pictureUploadRequest.getId());
            picture.setEditTime(new Date());
        } else {
            picture.setCreateTime(new Date());
        }

        //保存图片信息到数据库
        ThrowUtils.throwIf(!this.saveOrUpdate(picture), ErrorCode.SYSTEM_ERROR, "图片信息保存失败");
        //返回结果
        return PictureVO.objToVo(picture);

    }
}




