package com.travel.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * "我的攻略"卡片 VO（用于我的发布/我的点赞/我的收藏列表）
 *
 * 为什么单独建这个 VO？
 * - 点赞表(travel_like)只存了 strategyId，要展示给用户需要攻略的标题/封面/城市等信息
 * - 这个 VO 就是把"关联表记录 + 攻略信息"组装在一起
 * - 三个列表（发布/点赞/收藏）都可以复用这个结构，区别只在"操作时间"来源不同
 */
@Data
public class MyStrategyVO {

    private Long id;

    /** 攻略标题 */
    private String title;

    /** 所属城市 */
    private String cityName;

    /** 景点名称 */
    private String spotName;

    /** 封面图 */
    private String imgUrl;

    /** 分类 */
    private String category;

    /** 标签 */
    private String tag;

    /** 浏览量 */
    private Integer viewCount;

    /** 点赞数 */
    private Integer likeCount;

    /** 收藏数 */
    private Integer collectCount;

    /** 评论数 */
    private Integer commentCount;

    /** 状态：0下线 1上线 2待审核（仅我的发布需要） */
    private Integer status;

    /** 操作时间（发布时间/点赞时间/收藏时间） */
    private LocalDateTime actionTime;
}
