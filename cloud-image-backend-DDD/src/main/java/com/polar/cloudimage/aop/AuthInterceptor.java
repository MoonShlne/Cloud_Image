package com.polar.cloudimage.aop;

import com.polar.cloudimage.annotation.AuthCheck;
import com.polar.cloudimage.exception.BusinessException;
import com.polar.cloudimage.exception.ErrorCode;
import com.polar.cloudimage.exception.ThrowUtils;
import com.polar.cloudimage.model.entity.User;
import com.polar.cloudimage.model.enums.UserRoleEnum;
import com.polar.cloudimage.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author polar
 * @version 1.0
 * @since 2025/10/14 16:14
 */
@Aspect
@Component
@Slf4j
public class AuthInterceptor {

    @Resource
    private UserService userService;

    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mushRole();  //获取当前注解方法需要的权限
        //获取request
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();

        User loginUser = userService.getLoginUser(request);   //获取当前登录用户
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);   //获取注解中需要的权限枚举

        //如果不需要权限，直接放行
        if (mustRoleEnum ==null) {
            return joinPoint.proceed();
        }
        //如果需要权限，判断用户权限是否够，以及是否登录
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());  //获取用户权限枚举
        //如果用户没有权限，抛出异常
        ThrowUtils.throwIf(userRoleEnum == null, ErrorCode.NO_AUTH_ERROR);

        //要求管理员权限，但是用户不是管理员
        if (UserRoleEnum.ADMIN.equals(mustRoleEnum) && !UserRoleEnum.ADMIN.equals(userRoleEnum)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        return joinPoint.proceed();

    }
}
