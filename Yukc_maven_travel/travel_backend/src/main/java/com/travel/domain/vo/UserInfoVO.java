package com.travel.domain.vo;

import lombok.Data;

/**
 * 用户基础信息 返回对象
 * 用于普通用户信息查询，不含敏感字段、不含令牌
 */
@Data
public class UserInfoVO {
    private Long id;
    private String username;
    private String email;
    private Integer isAdmin;
}
