package com.polar.cloudimage.model.vo;

import lombok.Data;

import java.util.List;

/**
 * @author polar
 * @version 1.0 图片标签分类视图
 * @since 2025/10/18 15:13
 */
@Data
public class PictureTagCategory {
    /**
     * 标签列表
     */
    private List<String> tagList;

    /**
     * 分类列表
     */
    private  List<String> categoryList;
}
