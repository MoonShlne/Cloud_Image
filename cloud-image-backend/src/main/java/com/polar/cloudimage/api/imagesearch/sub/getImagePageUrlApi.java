package com.polar.cloudimage.api.imagesearch.sub;

import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;
import com.polar.cloudimage.exception.BusinessException;
import com.polar.cloudimage.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author polar
 * @version 1.0
 * @since 2025/10/24 19:13
 */
@Slf4j
public class getImagePageUrlApi {


    public static String getImagePageUrl(String imageUrl) {
//载荷
//      https%3A%2F%2Fcloudimage-1382780159.cos.ap-guangzhou.myqcloud.com%2Fspace%2F1981627003492651009%2F2025-10-24_aHx86yQm1YgV6qtr_thumbnail.webp
//      tn pc
//      from  pc
//      image_source PC_UPLOAD_URL
//      sdkParams {"data":"1457cfdf6fd46befc5ce6ad4585e26568777062108df1b3730b68c66ff760109eeb75f3df39ff6e2aee
//      f8427474b5a72392a495d1c8881627248bc204d8b20d2fedcdf4adb5751d5d3802cc0e2c0c2ad","key_id":"23","sign":"b1d5a9a5"}
//        1准备请求参数
        // 1. 准备请求参数
        Map<String, Object> formData = new HashMap<>();
        formData.put("image", imageUrl);
        formData.put("tn", "pc");
        formData.put("from", "pc");
        formData.put("image_source", "PC_UPLOAD_URL");
        // 获取当前时间戳
        long uptime = System.currentTimeMillis();
        // 请求地址
        String url = "https://graph.baidu.com/upload?uptime=" + uptime;
        String accessToken ="1761282837663_1761311004639_eSzPsFKHqcB+fodNijk1T3Nthfd/J/HAGiy93K2faG1SmvAzI//PhrtkmbCEFUmIE0cH5srOsWlV0Cuiis+ArJTIRsqc0nUVeidleDboXlpxVAsJXtgFftPPt5GQmBEn9ZST2/j3oM0eayTEaiWd2A4cno/CVHS8NTQgikZZoavfT8Xyub7jot9kXG/vKW9YQcPpFA1u3dBHTyEHWu9GaCFn+XLA1VpqCcwu9v4oA6MZsfPdPlUMOd/3cxJaL20FUj2Xc736DER9D69reWuZgjT2BRqQGkOpc0S8MdJ58x6KngTCRgiZ/r5Cdl5ROf0GUkux9AezH6rt7N8KNk1/xz9/ppCT6p42JahCvhGAxOEqqamVCQwC/Jh8Adjr653e23gaKayQAcdex977GLKpbCG/zWvL1rpvoQMkq48CMOvbA9PMNDpV3hLLAnvdGZdmDpIiHkgk4pSrLjyPeCaJVw==";
        try {
            // 2. 发送 POST 请求到百度接口
            HttpResponse response = HttpRequest.post(url)
                    .form(formData)
                    .timeout(5000)
                    .header("acs-token",accessToken)
                    .execute();
            // 判断响应状态
            if (HttpStatus.HTTP_OK != response.getStatus()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "接口调用失败");
            }
            // 解析响应
            String responseBody = response.body();
            Map<String, Object> result = JSONUtil.toBean(responseBody, Map.class);

            // 3. 处理响应结果
            if (result == null || !Integer.valueOf(0).equals(result.get("status"))) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "接口调用失败");
            }
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            String rawUrl = (String) data.get("url");
            // 对 URL 进行解码
            String searchResultUrl = URLUtil.decode(rawUrl, StandardCharsets.UTF_8);
            // 如果 URL 为空
            if (searchResultUrl == null) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "未返回有效结果");
            }
            return searchResultUrl;
        } catch (Exception e) {
            log.error("搜索失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "搜索失败");
        }
    }


    public static void main(String[] args) {
         //测试
        String imageUrl="https://cloudimage-1382780159.cos.ap-guangzhou.myqcloud.com/space/1981627003492651009/2025-10-24_aHx86yQm1YgV6qtr_thumbnail.webp";

        String result = getImagePageUrl(imageUrl);
        System.out.println(result);
    }
}




