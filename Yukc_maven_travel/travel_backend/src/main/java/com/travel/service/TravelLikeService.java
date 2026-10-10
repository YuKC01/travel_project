package com.travel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travel.domain.entity.TravelLike;
import com.travel.domain.vo.MyStrategyVO;

import java.util.List;

public interface TravelLikeService extends IService<TravelLike> {
    /**
     * 点赞/取消点赞 切换
     * @param strategyId 攻略ID
     * @return true=点赞成功，false=取消点赞成功
     */
    boolean toggleLike(Long strategyId);

    /**
     * 我点赞的攻略列表
     * @param userId 用户ID
     * @return 攻略VO列表
     */
    List<MyStrategyVO> listMyLikes(Long userId);
}
