package com.polor.cloudimage.controller;

import com.polor.cloudimage.common.BaseResponse;
import com.polor.cloudimage.common.ResultUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
