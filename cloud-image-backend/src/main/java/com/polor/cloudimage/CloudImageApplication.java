package com.polor.cloudimage;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@MapperScan("com.polor.cloudimage.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)  //启用获得当前代理对象功能
public class CloudImageApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudImageApplication.class, args);
    }

}
