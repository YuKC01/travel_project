package com.travel.domain.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 攻略列表卡片VO，用于搜索页面卡片渲染
 * 只保留卡片需要字段，附带精简天气信息
 */
@Data
public class StrategyCardVO {
    /**
     * 攻略主键id
     */
    private Long id;
    /**
     * 攻略标题
     */
    private String title;
    /**
     * 所属城市
     */
    private String cityName;
    /**
     * 景点名称
     */
    private String spotName;
    /**
     * 图片外链
     */
    private String imgUrl;
    /**
     * 标签标记：免费/收费
     */
    private String tag;
    /**
     * 内容分类：历史古迹、自然风光
     */
    private String category;
    /**
     * 点赞数
     */
    private Integer likeCount;
    /**
     * 浏览量
     */
    private Integer viewCount;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 是否热门
     */
    private Boolean isHot;

    //==== 附加外部天气模块返回的精简天气信息 ====
    /**
     * 天气状况
     */
    private String weather;
    /**
     * 温度
     */
    private String temperature;
}
