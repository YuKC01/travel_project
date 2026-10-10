package com.travel.domain.vo;

import lombok.Data;
import java.util.List;

/**
 * 攻略搜索接口返回外层VO
 * targetList：搜索主体结果
 * recommendList：同城市其他推荐景点，城市搜索时为null
 */
@Data
public class SearchStrategyVO {
    /**
     * 关键词真正命中的攻略列表
     */
    private List<StrategyCardVO> targetList;

    /**
     * 同城推荐攻略列表
     */
    private List<StrategyCardVO> recommendList;

    /**
     * 提示消息：未找到结果时返回提示
     */
    private String msg;
}
