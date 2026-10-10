package com.travel.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 攻略发布/编辑 请求参数对象
 * 发布时id为空，编辑时id必传
 */
@Data
public class StrategyPublishDTO {

    /**
     * 攻略ID
     * 发布场景：不传，由后端自动生成
     * 编辑场景：必传，指定要修改的攻略
     */
    private Long id;

    /**
     * 攻略标题
     */
    @NotBlank(message = "攻略标题不能为空")
    private String title;
    /**
     * 城市名称
     */
    @NotBlank(message = "城市名称不能为空")
    private String cityName;

    /**
     * 景点名称
     */
    @NotBlank(message = "景点名称不能为空")
    private String spotName;

    /**
     * 收费标签
     */
    private String tag;

    /**
     * 内容分类
     */
    private String category;
    /**
     * 攻略封面图地址
     * 前端先调用上传接口拿到七牛云URL，再传入这里
     */
    private String imgUrl;

    /**
     * 攻略正文内容
     */
    @NotBlank(message = "攻略内容不能为空")
    private String content;

    /**
     * 攻略状态（仅管理员可指定）
     * 0下线 1上线 2待审核
     * 普通用户发布时不传，后端默认设为2（待审核）
     */
    private Integer status;
}
