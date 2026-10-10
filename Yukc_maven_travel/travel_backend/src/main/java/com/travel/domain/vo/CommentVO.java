package com.travel.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论展示 VO
 * 相比 TravelComment 实体，多了用户昵称/头像和二级回复列表
 *
 * 为什么需要 VO？
 * - 实体类对应数据库表，字段是"原样存储"的
 * - 前端展示需要"组装后"的数据：评论里只有 userId，但前端要显示昵称和头像
 * - VO 就是专门给前端看的数据视图，把多表查询的结果组装好返回
 *
 * 知识点：VO（View Object） vs Entity（实体）
 * - Entity 映射数据库表，字段和表列一一对应
 * - VO 面向前端展示，可以跨表聚合、可以增删字段、可以格式化数据
 * - 这是"关注点分离"思想：数据库结构和前端展示结构解耦，改表不影响前端，反之亦然
 */
@Data
public class CommentVO {

    private Long id;

    private Long userId;

    /** 用户昵称（从 user 表关联查询） */
    private String nickName;

    /** 用户头像URL（从 user 表关联查询） */
    private String avatar;

    private Long strategyId;

    private String commentContent;

    /** 父评论ID，0=顶级评论 */
    private Long parentId;

    private LocalDateTime createTime;

    /** 二级回复列表（楼中楼） */
    private List<CommentVO> replies;
}
