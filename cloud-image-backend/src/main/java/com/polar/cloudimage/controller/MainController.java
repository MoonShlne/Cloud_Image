package com.polar.cloudimage.controller;

import com.polar.cloudimage.common.BaseResponse;
import com.polar.cloudimage.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author polar
 * @version 1.0
 * @since 2025/10/13 20:24
 */
@RestController
@RequestMapping("/")
public class MainController {
//健康检查
    @GetMapping("health")
    public BaseResponse<String> health(){
        return ResultUtils.success("ok");
    }

}
