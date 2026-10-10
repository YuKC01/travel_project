package com.travel.domain.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 攻略详情页VO
 */
@Data
public class StrategyDetailVO {
    private Long id;
    private String title;

    private String cityName;
    private String spotName;
    private String content;
    private String imgUrl;
    private String tag;
    private String category;
    private Integer likeCount;
    private Integer viewCount;
    private LocalDateTime createTime;

    // 天气模块完整返回信息
    private String weather;
    private String temperature;
    /**
     * 出行提示
     */
    private String travelTip;
    /**
     * 是否热门，后端根据浏览量计算，不存入数据库
     */
    private Boolean isHot;
}
