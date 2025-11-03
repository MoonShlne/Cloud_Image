package com.polar.cloudimage;

import com.polar.cloudimage.config.CosClientConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import javax.annotation.Resource;

/**
 * @author polar
 * @version 1.0
 * @since 2025/10/20 16:02
 */
@SpringBootTest
@ActiveProfiles("local")
public class RedisStringTest {
    //编写测试代码
    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Test
    public void test(){
        //存值
        redisTemplate.opsForValue().set("name","polar");
        //取值
        String name = redisTemplate.opsForValue().get("name");
        System.out.println("name = " + name);
    }

}
