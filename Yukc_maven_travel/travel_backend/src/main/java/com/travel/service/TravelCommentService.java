package com.travel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travel.domain.entity.TravelComment;
import com.travel.domain.vo.CommentVO;

import java.util.List;

public interface TravelCommentService extends IService<TravelComment> {
    /**
     * 发布评论
     * @param strategyId 攻略ID
     * @param content 评论内容
     * @param parentId 父评论ID，0或null表示顶级评论
     * @return 评论ID
     */
    Long addComment(Long strategyId, String content, Long parentId);

    /**
     * 删除评论
     * @param commentId 评论ID
     */
    void deleteComment(Long commentId);

    /**
     * 查询攻略的评论列表（组装用户信息 + 二级回复结构）
     * @param strategyId 攻略ID
     * @return 评论VO列表
     */
    List<CommentVO> getCommentList(Long strategyId);

    /**
     * 查询当前用户的评论列表（个人中心用）
     * @param userId 用户ID
     * @return 评论VO列表（附带攻略标题）
     */
    List<CommentVO> getMyComments(Long userId);
}
