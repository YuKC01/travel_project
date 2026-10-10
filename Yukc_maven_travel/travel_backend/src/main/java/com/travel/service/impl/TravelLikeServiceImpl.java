package com.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travel.common.exception.BusinessException;
import com.travel.common.util.UserContext;
import com.travel.domain.entity.TravelLike;
import com.travel.domain.entity.Strategy;
import com.travel.domain.vo.MyStrategyVO;
import com.travel.mapper.TravelLikeMapper;
import com.travel.service.StrategyService;
import com.travel.service.TravelLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TravelLikeServiceImpl extends ServiceImpl<TravelLikeMapper, TravelLike> implements TravelLikeService {

    private final StrategyService strategyService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleLike(Long strategyId) {
        Strategy strategy = strategyService.getById(strategyId);
        if (strategy == null) {
            throw new BusinessException(404, "攻略不存在");
        }

        Long userId = UserContext.getUserId();

        LambdaQueryWrapper<TravelLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TravelLike::getUserId, userId)
                .eq(TravelLike::getStrategyId, strategyId);
        TravelLike like = this.getOne(wrapper);

        if (like != null) {
            this.removeById(like.getId());
            // 点赞数 -1，用 SQL 原子操作避免并发丢失更新和 NPE
            LambdaUpdateWrapper<Strategy> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Strategy::getId, strategyId)
                    .setSql("like_count = GREATEST(like_count - 1, 0)");
            strategyService.update(null, updateWrapper);
            return false;
        } else {
            TravelLike newLike = new TravelLike();
            newLike.setUserId(userId);
            newLike.setStrategyId(strategyId);
            newLike.setCreateTime(LocalDateTime.now());
            this.save(newLike);
            // 点赞数 +1，SQL 原子递增
            LambdaUpdateWrapper<Strategy> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Strategy::getId, strategyId)
                    .setSql("like_count = like_count + 1");
            strategyService.update(null, updateWrapper);
            return true;
        }
    }

    @Override
    public List<MyStrategyVO> listMyLikes(Long userId) {
        // 1. 查当前用户点赞的所有记录
        List<TravelLike> likes = this.list(new LambdaQueryWrapper<TravelLike>()
                .eq(TravelLike::getUserId, userId)
                .orderByDesc(TravelLike::getCreateTime));
        if (likes.isEmpty()) {
            return List.of();
        }
        // 2. 批量查对应的攻略（避免循环单条查）
        List<Long> strategyIds = likes.stream().map(TravelLike::getStrategyId).collect(Collectors.toList());
        Map<Long, Strategy> strategyMap = strategyService.listByIds(strategyIds).stream()
                .collect(Collectors.toMap(Strategy::getId, s -> s));
        // 3. 组装：点赞时间作为 actionTime
        return likes.stream()
                .map(like -> {
                    Strategy s = strategyMap.get(like.getStrategyId());
                    if (s == null) return null;
                    MyStrategyVO vo = convertStrategyToMyVO(s);
                    vo.setActionTime(like.getCreateTime());
                    return vo;
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }

    /** Strategy 转 MyStrategyVO（抽取公共逻辑，点赞/收藏/发布都复用） */
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
