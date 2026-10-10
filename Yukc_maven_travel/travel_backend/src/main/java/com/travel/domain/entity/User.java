package com.travel.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

// @Data：Lombok注解，自动生成getter、setter、toString、equals、hashCode
@Data
// @TableName：指定这个实体对应数据库哪张表
@TableName("user")
public class User {

    // @TableId：标记主键，type=AUTO表示数据库自增
    @TableId(type = IdType.AUTO)
    private Long id;

    // 用户名，对应表字段username
    private String username;

    // 密码，存BCrypt加密后的字符串，绝对不存明文
    private String password;

    // 邮箱
    private String email;

    // 是否管理员：0普通用户，1管理员
    // 对应表字段is_admin，MP自动驼峰转下划线映射
    private Integer isAdmin;

    // 账号状态：1正常 0禁用（管理员可禁用用户）
    private Integer status;

    // 昵称（用于评论区、作者位展示）
    private String nickName;

    // 头像URL（七牛云外链，用户可通过上传接口修改）
    private String avatar;

    // @TableField(fill = FieldFill.INSERT)：插入数据时自动填充时间
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    // @TableField(fill = FieldFill.INSERT_UPDATE)：插入和更新时自动填充时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
