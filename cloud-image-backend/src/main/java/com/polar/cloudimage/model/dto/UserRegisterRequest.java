package com.polar.cloudimage.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户登录请求
 */
@Data
public class UserRegisterRequest implements Serializable {
    private static final long serialVersionUID = 3879293903861157844L;
    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;

    /**
     *  确认密码
     */
    private String checkPassword;
}
