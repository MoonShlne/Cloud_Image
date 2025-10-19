package com.polar.cloudimage.manager.upload;

import cn.hutool.core.io.FileUtil;
import com.polar.cloudimage.constant.PictureConstant;
import com.polar.cloudimage.exception.ErrorCode;
import com.polar.cloudimage.exception.ThrowUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * @author polar
 * @version 1.0
 * @since 2025/10/19 13:23
 * 图片 文件  上传实现类
 */
@Service
public class FilePictureUpload extends PictureUploadTemplate {
    /**
     * 校验图片
     *
     * @param inputSource 输入源
     */
    @Override
    protected void validPicture(Object inputSource) {
        //强制转换
        MultipartFile multipartFile = (MultipartFile) inputSource;
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

    /**
     * 获取原始文件名
     *
     * @param inputSource 输入源
     * @return 原始文件名
     */
    @Override
    protected String getOriginFilename(Object inputSource) {
        //强制转换
        MultipartFile multipartFile = (MultipartFile) inputSource;
        return multipartFile.getOriginalFilename();
    }

    /**
     * 处理文件上传逻辑
     *
     * @param inputSource 输入源
     * @param file        本地临时文件
     * @throws Exception 异常
     */
    @Override
    protected void processFile(Object inputSource, File file) throws Exception {
        //强制转换
        MultipartFile multipartFile = (MultipartFile) inputSource;
        //保存文件到本地临时文件
        multipartFile.transferTo(file);

    }
}
