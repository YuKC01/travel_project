package com.travel.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户登录 请求参数对象
 * 接收前端登录提交的账号密码
 */
@Data
public class UserLoginDTO {

    @NotBlank(message = "用户名不能为空")//后端服务端的参数非空校验
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
