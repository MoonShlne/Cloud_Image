package com.polar.cloudimage.api.imagesearch;

import com.polar.cloudimage.api.imagesearch.module.ImageSearchResult;
import com.polar.cloudimage.api.imagesearch.sub.GetImageFirstUrlApi;
import com.polar.cloudimage.api.imagesearch.sub.GetImageListApi;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.polar.cloudimage.api.imagesearch.sub.getImagePageUrlApi.getImagePageUrl;

/**
 * @author polar
 * @version 1.0
 * @since 2025/10/24 21:25
 */
@Slf4j
public class ImageSearchApiFacade {
    //使用门面模式封装图片搜索的各个步骤
    public static List<ImageSearchResult> searchImage(String imageUrl) {
        // 第一步：获取图片搜索页面的 URL
        String imagePageUrl = getImagePageUrl(imageUrl);
        log.info("图片搜索页面 URL: {}", imagePageUrl);
        // 第二步：从图片搜索页面获取 firstUrl
        String firstUrl = GetImageFirstUrlApi.getImageFirstUrl(imagePageUrl);
        log.info("firstUrl: {}", firstUrl);
        // 第三步：使用 firstUrl 获取图片列表
        List<ImageSearchResult> imageSearchResults = GetImageListApi.getImageList(firstUrl);
        return imageSearchResults;

    }

    //    测试
    public static void main(String[] args) {
        String testImageUrl = "https://cloudimage-1382780159.cos.ap-guangzhou.myqcloud.com/space/1981627003492651009/2025-10-24_aHx86yQm1YgV6qtr_thumbnail.webp";
        List<ImageSearchResult> results = searchImage(testImageUrl);
        System.out.println(results);
    }
}
