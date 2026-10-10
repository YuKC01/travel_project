package com.travel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travel.domain.entity.UserCollect;
import com.travel.domain.vo.MyStrategyVO;

import java.util.List;

public interface UserCollectService extends IService<UserCollect> {
    /**
     * 收藏/取消收藏 切换
     * @param strategyId 攻略ID
     * @return true=收藏成功，false=取消收藏成功
     */
    boolean toggleCollect(Long strategyId);

    /**
     * 我收藏的攻略列表
     * @param userId 用户ID
     * @return 攻略VO列表
     */
    List<MyStrategyVO> listMyCollects(Long userId);
}
