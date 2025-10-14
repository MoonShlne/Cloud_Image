package com.polor.cloudimage.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author polar
 * @version 1.0
 * @since 2025/10/14 16:11
 */
@Target(ElementType.METHOD)  // 作用于方法
@Retention(RetentionPolicy.RUNTIME)  // 运行时有效
public @interface AuthCheck {
    String mushRole() default ""; // 必须角色

}
