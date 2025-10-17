package com.polar.cloudimage.manager;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.unit.DataUnit;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.polar.cloudimage.common.ResultUtils;
import com.polar.cloudimage.config.CosClientConfig;
import com.polar.cloudimage.constant.PictureConstant;
import com.polar.cloudimage.constant.UserConstant;
import com.polar.cloudimage.exception.BusinessException;
import com.polar.cloudimage.exception.ErrorCode;
import com.polar.cloudimage.exception.ThrowUtils;
import com.polar.cloudimage.model.dto.file.UploadPictureResult;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.OriginalInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author polar
 * @version 1.0
 * @since 2025/10/15 18:32
 * 进一步封装上传下载方法
 */
@Service
@Slf4j
public class FileManager {

    @Resource
    private CosManager cosManager;

    @Resource
    private COSClient cosClient;
    @Autowired
    private CosClientConfig cosClientConfig;


    /**
     * 上传图片
     *
     * @param multipartFile    文件
     * @param uploadPathPrefix 上传路径前缀
     * @return 图片上传结果
     */
    public UploadPictureResult uploadPicture(MultipartFile multipartFile, String uploadPathPrefix) {
        //校验图片
        ValidPicture(multipartFile);
        //图片上传
        //封装图片名字
        String uuid = RandomUtil.randomString(16);
        String suffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        String filename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, suffix);
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, filename);
        //解析结果
        File file = null;
        try {
            //创建临时文件 并且上传
            file = File.createTempFile(uploadPath, null);
            multipartFile.transferTo(file);
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            //封装返回结果
            int pictureWidth = imageInfo.getWidth();
            int pictureHeight = imageInfo.getHeight();
            String format = imageInfo.getFormat();
            //宽高比
            double pictureScale = NumberUtil.round(NumberUtil.div(pictureWidth, pictureHeight), 2).doubleValue();

            UploadPictureResult uploadPictureResult = new UploadPictureResult();
            uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + uploadPath);
            uploadPictureResult.setPicName(FileUtil.mainName(multipartFile.getOriginalFilename()));
            uploadPictureResult.setPicSize(FileUtil.size(file));
            uploadPictureResult.setPicWidth(pictureWidth);
            uploadPictureResult.setPicHeight(pictureHeight);
            uploadPictureResult.setPicScale(pictureScale);
            uploadPictureResult.setPicFormat(format);

            return uploadPictureResult;
        } catch (Exception e) {
            log.info("文件上传失败 path={}", uploadPath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传失败");
        } finally {
            //删除临时文件
            deleteTempFile(file);

        }


    }

    /**
     * 删除临时文件
     *
     * @param file 临时文件
     */
    private static void deleteTempFile(File file) {
        if (file != null) {
            boolean delete = file.delete();
            if (!delete) {
                log.info("文件删除失败 file={}", file.getAbsolutePath());
            }
        }
    }


    /**
     * 校验图片
     *
     * @param multipartFile 文件
     */
    private static void ValidPicture(MultipartFile multipartFile) {
        //1.文件是否为空
        ThrowUtils.throwIf(multipartFile == null || multipartFile.isEmpty(), ErrorCode.PARAMS_ERROR, "文件不能为空");
        //2.文件大小是否超过限制 5M
        long fileSize = multipartFile.getSize();
        final long MAX_SIZE = 5 * 1024 * 1024;
        ThrowUtils.throwIf(fileSize > MAX_SIZE, ErrorCode.PARAMS_ERROR, "文件大小不能超过5M");
        //3.文件类型是否符合要求
        String suffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        ThrowUtils.throwIf(!PictureConstant.ALLOW_SUFFIX.contains(suffix), ErrorCode.PARAMS_ERROR, "文件格式不正确");
    }


}
