package com.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travel.common.exception.BusinessException;
import com.travel.common.util.UserContext;
import com.travel.domain.entity.Strategy;
import com.travel.domain.entity.UserCollect;
import com.travel.domain.vo.MyStrategyVO;
import com.travel.mapper.UserCollectMapper;
import com.travel.service.StrategyService;
import com.travel.service.UserCollectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserCollectServiceImpl extends ServiceImpl<UserCollectMapper, UserCollect> implements UserCollectService {

    private final StrategyService strategyService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleCollect(Long strategyId) {
        Strategy strategy = strategyService.getById(strategyId);
        if (strategy == null) {
            throw new BusinessException(404, "攻略不存在");
        }

        Long userId = UserContext.getUserId();

        LambdaQueryWrapper<UserCollect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCollect::getUserId, userId)
                .eq(UserCollect::getStrategyId, strategyId);
        UserCollect collect = this.getOne(wrapper);

        if (collect != null) {
            this.removeById(collect.getId());
            // 收藏数 -1，SQL 原子操作避免并发丢失更新和 NPE
            LambdaUpdateWrapper<Strategy> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Strategy::getId, strategyId)
                    .setSql("collect_count = GREATEST(collect_count - 1, 0)");
            strategyService.update(null, updateWrapper);
            return false;
        } else {
            UserCollect newCollect = new UserCollect();
            newCollect.setUserId(userId);
            newCollect.setStrategyId(strategyId);
            newCollect.setCreateTime(LocalDateTime.now());
            this.save(newCollect);
            // 收藏数 +1，SQL 原子递增
            LambdaUpdateWrapper<Strategy> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Strategy::getId, strategyId)
                    .setSql("collect_count = collect_count + 1");
            strategyService.update(null, updateWrapper);
            return true;
        }
    }

    @Override
    public List<MyStrategyVO> listMyCollects(Long userId) {
        List<UserCollect> collects = this.list(new LambdaQueryWrapper<UserCollect>()
                .eq(UserCollect::getUserId, userId)
                .orderByDesc(UserCollect::getCreateTime));
        if (collects.isEmpty()) {
            return List.of();
        }
        List<Long> strategyIds = collects.stream().map(UserCollect::getStrategyId).collect(Collectors.toList());
        Map<Long, Strategy> strategyMap = strategyService.listByIds(strategyIds).stream()
                .collect(Collectors.toMap(Strategy::getId, s -> s));
        return collects.stream()
                .map(c -> {
                    Strategy s = strategyMap.get(c.getStrategyId());
                    if (s == null) return null;
                    MyStrategyVO vo = convertStrategyToMyVO(s);
                    vo.setActionTime(c.getCreateTime());
                    return vo;
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }

    private MyStrategyVO convertStrategyToMyVO(Strategy s) {
        MyStrategyVO vo = new MyStrategyVO();
        vo.setId(s.getId());
        vo.setTitle(s.getTitle());
        vo.setCityName(s.getCityName());
        vo.setSpotName(s.getSpotName());
        vo.setImgUrl(s.getImgUrl());
        vo.setCategory(s.getCategory());
        vo.setTag(s.getTag());
        vo.setViewCount(s.getViewCount());
        vo.setLikeCount(s.getLikeCount());
        vo.setCollectCount(s.getCollectCount());
        vo.setCommentCount(s.getCommentCount());
        vo.setStatus(s.getStatus());
        return vo;
    }
}
