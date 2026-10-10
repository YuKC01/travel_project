package com.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travel.common.exception.BusinessException;
import com.travel.common.util.UserContext;
import com.travel.domain.entity.Strategy;
import com.travel.domain.entity.TravelComment;
import com.travel.domain.entity.User;
import com.travel.domain.vo.CommentVO;
import com.travel.mapper.TravelCommentMapper;
import com.travel.mapper.UserMapper;
import com.travel.service.StrategyService;
import com.travel.service.TravelCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TravelCommentServiceImpl extends ServiceImpl<TravelCommentMapper, TravelComment> implements TravelCommentService {

    private final StrategyService strategyService;
    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addComment(Long strategyId, String content, Long parentId) {
        // 1. 校验攻略是否存在
        Strategy strategy = strategyService.getById(strategyId);
        if (strategy == null) {
            throw new BusinessException(404, "攻略不存在");
        }

        // 2. 校验评论内容
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException(400, "评论内容不能为空");
        }

        // 3. 如果是回复，校验父评论是否存在且属于同一攻略
        if (parentId != null && parentId > 0) {
            TravelComment parent = this.getById(parentId);
            if (parent == null) {
                throw new BusinessException(404, "被回复的评论不存在");
            }
            // 只允许回复同一攻略下的评论，且不允许回复二级评论（保持两层结构）
            if (!parent.getStrategyId().equals(strategyId)) {
                throw new BusinessException(400, "只能回复同一攻略下的评论");
            }
            if (parent.getParentId() != null && parent.getParentId() > 0) {
                throw new BusinessException(400, "暂不支持三级评论");
            }
        }

        // 4. 组装评论数据
        Long userId = UserContext.getUserId();
        TravelComment comment = new TravelComment();
        comment.setUserId(userId);
        comment.setStrategyId(strategyId);
        comment.setCommentContent(content.trim());
        comment.setParentId(parentId == null ? 0L : parentId);
        comment.setCreateTime(LocalDateTime.now());

        // 5. 保存评论
        this.save(comment);

        // 6. 攻略评论数+1
        int count = strategy.getCommentCount() == null ? 0 : strategy.getCommentCount();
        strategy.setCommentCount(count + 1);
        strategyService.updateById(strategy);

        return comment.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId) {
        // 1. 校验评论是否存在
        TravelComment comment = this.getById(commentId);
        if (comment == null) {
            throw new BusinessException(404, "评论不存在");
        }

        // 2. 权限校验：仅本人可删除
        Long currentUserId = UserContext.getUserId();
        if (!comment.getUserId().equals(currentUserId)) {
            throw new BusinessException(403, "无权限删除该评论");
        }

        // 3. 先统计该评论下的回复数（删除前统计，否则删完查不到了）
        long replyCount = this.count(new LambdaQueryWrapper<TravelComment>()
                .eq(TravelComment::getParentId, commentId));

        // 4. 删除评论及其所有回复
        LambdaQueryWrapper<TravelComment> replyWrapper = new LambdaQueryWrapper<>();
        replyWrapper.eq(TravelComment::getParentId, commentId);
        this.remove(replyWrapper);
        this.removeById(commentId);

        // 5. 攻略评论数 -1（被删评论） - replyCount（被删回复数）
        //    用 setSql 原子递减，避免并发丢失更新
        Strategy strategy = strategyService.getById(comment.getStrategyId());
        if (strategy != null) {
            int decrement = 1 + (int) replyCount;
            LambdaUpdateWrapper<Strategy> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Strategy::getId, comment.getStrategyId())
                    .setSql("comment_count = GREATEST(comment_count - " + decrement + ", 0)");
            strategyService.update(null, updateWrapper);
        }
    }

    @Override
    public List<CommentVO> getCommentList(Long strategyId) {
        // 1. 查询顶级评论（parentId=0），按时间倒序
        LambdaQueryWrapper<TravelComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TravelComment::getStrategyId, strategyId)
                .eq(TravelComment::getParentId, 0L)
                .orderByDesc(TravelComment::getCreateTime);
        List<TravelComment> topComments = this.list(wrapper);

        if (topComments.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 收集所有需要查询用户信息的 userId（顶级 + 后续回复）
        // 先查回复，再一起查用户，避免多次查询
        List<Long> topCommentIds = topComments.stream()
                .map(TravelComment::getId)
                .collect(Collectors.toList());
        List<TravelComment> replies = this.list(new LambdaQueryWrapper<TravelComment>()
                .in(TravelComment::getParentId, topCommentIds)
                .orderByAsc(TravelComment::getCreateTime));

        // 3. 收集所有 userId 批量查询用户（避免 N+1 查询）
        List<Long> allUserIds = new ArrayList<>();
        allUserIds.addAll(topComments.stream().map(TravelComment::getUserId).collect(Collectors.toList()));
        allUserIds.addAll(replies.stream().map(TravelComment::getUserId).collect(Collectors.toList()));
        Map<Long, User> userMap = userMapper.selectBatchIds(allUserIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        // 4. 组装顶级评论 VO
        List<CommentVO> result = topComments.stream().map(c -> {
            CommentVO vo = convertToVO(c, userMap);
            // 找出这条评论的所有回复
            List<CommentVO> replyList = replies.stream()
                    .filter(r -> r.getParentId().equals(c.getId()))
                    .map(r -> convertToVO(r, userMap))
                    .collect(Collectors.toList());
            vo.setReplies(replyList);
            return vo;
        }).collect(Collectors.toList());

        return result;
    }

    @Override
    public List<CommentVO> getMyComments(Long userId) {
        LambdaQueryWrapper<TravelComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TravelComment::getUserId, userId)
                .orderByDesc(TravelComment::getCreateTime);
        List<TravelComment> comments = this.list(wrapper);
        if (comments.isEmpty()) {
            return new ArrayList<>();
        }
        // 查用户自己的信息
        User user = userMapper.selectById(userId);
        Map<Long, User> userMap = Map.of(userId, user);
        return comments.stream()
                .map(c -> convertToVO(c, userMap))
                .collect(Collectors.toList());
    }

    /**
     * 评论实体转 VO：填充用户昵称、头像
     * BeanUtils.copyProperties 会把同名同类型属性自动拷贝
     */
    private CommentVO convertToVO(TravelComment comment, Map<Long, User> userMap) {
        CommentVO vo = new CommentVO();
        BeanUtils.copyProperties(comment, vo);
        User user = userMap.get(comment.getUserId());
        if (user != null) {
            // 昵称优先用 nickName，没有则用 username 兜底
            vo.setNickName(user.getNickName() != null ? user.getNickName() : user.getUsername());
            vo.setAvatar(user.getAvatar());
        } else {
            vo.setNickName("用户" + comment.getUserId());
        }
        return vo;
    }
}
