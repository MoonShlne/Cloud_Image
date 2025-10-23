package com.polar.cloudimage;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@MapperScan("com.polar.cloudimage.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)  //启用获得当前代理对象功能
@EnableCaching
@EnableAsync
public class CloudImageApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudImageApplication.class, args);
    }

}
