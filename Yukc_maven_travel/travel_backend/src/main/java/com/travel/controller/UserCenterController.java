package com.travel.controller;

import com.travel.common.Result;
import com.travel.common.util.UserContext;
import com.travel.domain.entity.Strategy;
import com.travel.domain.vo.CommentVO;
import com.travel.domain.vo.MyStrategyVO;
import com.travel.service.StrategyService;
import com.travel.service.TravelCommentService;
import com.travel.service.TravelLikeService;
import com.travel.service.UserCollectService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 个人中心控制器
 * 所有接口都需要登录（JWT 拦截器默认拦截），从 UserContext 获取当前用户ID
 *
 * 知识点：
 * - UserContext.getUserId() 是从 JWT 拦截器解析 token 后存入线程上下文的
 *   所以这里不需要前端传 userId，避免越权（用户只能看自己的数据）
 * - 四个接口都返回 List，前端渲染时按需展示
 */
@RestController
@RequestMapping("/user/center")
@RequiredArgsConstructor
public class UserCenterController {

    private final StrategyService strategyService;
    private final TravelLikeService travelLikeService;
    private final UserCollectService userCollectService;
    private final TravelCommentService travelCommentService;

    /**
     * 我的发布
     * GET /user/center/published
     */
    @GetMapping("/published")
    public Result<List<Strategy>> myPublished() {
        Long userId = UserContext.getUserId();
        return Result.success(strategyService.listMyPublished(userId));
    }

    /**
     * 我的点赞
     * GET /user/center/likes
     */
    @GetMapping("/likes")
    public Result<List<MyStrategyVO>> myLikes() {
        Long userId = UserContext.getUserId();
        return Result.success(travelLikeService.listMyLikes(userId));
    }

    /**
     * 我的收藏
     * GET /user/center/collects
     */
    @GetMapping("/collects")
    public Result<List<MyStrategyVO>> myCollects() {
        Long userId = UserContext.getUserId();
        return Result.success(userCollectService.listMyCollects(userId));
    }

    /**
     * 我的评论
     * GET /user/center/comments
     */
    @GetMapping("/comments")
    public Result<List<CommentVO>> myComments() {
        Long userId = UserContext.getUserId();
        return Result.success(travelCommentService.getMyComments(userId));
    }
}
