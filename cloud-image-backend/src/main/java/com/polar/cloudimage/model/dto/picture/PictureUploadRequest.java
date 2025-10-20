package com.polar.cloudimage.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * 图片上传请求
 */
@Data
public class PictureUploadRequest implements Serializable {
    private static final long serialVersionUID = -1847294040491966467L;
    /**
     * 图片 id（用于修改）
     */
    private Long id;

    /**
     * 图片地址
     */
    private String fileUrl;

    /**
     * 图片名称
     */
    private String picName;

}