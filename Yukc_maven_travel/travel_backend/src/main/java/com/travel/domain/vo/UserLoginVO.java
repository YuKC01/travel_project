package com.travel.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户登录成功 返回对象
 * 携带用户基础信息 + JWT令牌
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginVO {
    // 用户ID
    private Long id;
    // 用户名
    private String username;
    // 昵称（评论区/作者位展示）
    private String nickName;
    // 头像URL
    private String avatar;
    // 是否管理员：0普通 1管理员
    private Integer isAdmin;
    // JWT令牌，前端后续请求携带
    private String token;
}
