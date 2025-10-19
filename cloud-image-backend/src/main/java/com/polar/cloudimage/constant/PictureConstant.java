package com.polar.cloudimage.constant;

import java.util.Arrays;
import java.util.List;

/**
 * @author polar
 * @version 1.0
 * @since 2025/10/15 20:24
 */
public interface PictureConstant {
    /**
     * 允许的图片后缀
     */
    final List<String> ALLOW_SUFFIX = Arrays.asList("jpg", "jpeg", "png", "gif");

    final List<String> ALLOW_CONTENT_TYPES = Arrays.asList("image/jpeg", "image/jpg", "image/png", "image/webp");

}
