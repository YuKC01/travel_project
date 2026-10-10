package com.travel.controller;

import com.travel.common.PageVO;
import com.travel.common.Result;
import com.travel.common.ResultCode;
import com.travel.common.annotation.RateLimit;
import com.travel.domain.dto.CommentDTO;
import com.travel.domain.dto.StrategyPublishDTO;
import com.travel.domain.entity.Strategy;
import com.travel.domain.vo.CommentVO;
import com.travel.domain.vo.SearchStrategyVO;
import com.travel.domain.vo.StrategyDetailVO;
import com.travel.service.StrategyService;
import com.travel.service.TravelCommentService;
import com.travel.service.TravelLikeService;
import com.travel.service.UserCollectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 攻略控制器
 * 接收前端http请求，调用service层，返回统一Result格式json
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/strategy")
public class StrategyController {

    //@Autowired
    private final StrategyService strategyService;
    private final TravelLikeService travelLikeService;
    private final UserCollectService userCollectService;
    private final TravelCommentService travelCommentService;

    /**
     * 攻略搜索接口
     * GET /strategy/search?keyword=泰山
     */
    @GetMapping("/search")
    public Result<SearchStrategyVO> search(@RequestParam(required = false) String keyword){
        SearchStrategyVO vo = strategyService.search(keyword);
        return Result.success(vo);
    }

    /**
     * 攻略详情接口
     * GET /strategy/detail/{id}
     */
    @GetMapping("/detail/{id}")
    public Result<StrategyDetailVO> detail(@PathVariable Long id){
        if(id == null || id <= 0){
            return Result.fail(ResultCode.PARAM_ERROR);
        }
        StrategyDetailVO detailVO = strategyService.getDetail(id);
        if(detailVO == null){
            return Result.fail(ResultCode.STRATEGY_NOT_EXIST);
        }
        return Result.success(detailVO);
    }

    /**
     * 发布攻略（限流：每秒只允许 1 次，防止恶意刷库）
     * 限流粒度：全局（不分用户），适合保护后端数据库
     */
    @RateLimit(qps = 1)
    @PostMapping("/publish")
    public Result<Void> publish(@RequestBody @Valid StrategyPublishDTO dto) {
        strategyService.publish(dto);
        return Result.success();
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody @Valid StrategyPublishDTO dto) {
        strategyService.update(dto);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        strategyService.delete(id);
        return Result.success();
    }

    /**
     * 全量攻略列表接口（首页/目的地/分类页用，返回全部上线攻略）
     * GET /strategy/list
     */
    @GetMapping("/list")
    public Result<List<Strategy>> list() {
        List<Strategy> list = strategyService.listAll();
        return Result.success(list);
    }

    /**
     * 分页攻略列表（数据量大时用）
     * GET /strategy/page?pageNum=1&pageSize=10
     */
    @GetMapping("/page")
    public Result<PageVO<Strategy>> page(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        return Result.success(strategyService.filter(null, null, pageNum, pageSize));
    }

    /**
     * 分类/标签筛选（分页）
     * GET /strategy/filter?category=历史古迹&tag=免费&pageNum=1&pageSize=10
     * category 和 tag 都是可选参数，不传表示不筛选
     */
    @GetMapping("/filter")
    public Result<PageVO<Strategy>> filter(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        return Result.success(strategyService.filter(category, tag, pageNum, pageSize));
    }

    /**
     * 热门推荐（按浏览量排序）
     * GET /strategy/hot?limit=6
     */
    @GetMapping("/hot")
    public Result<List<Strategy>> hot(@RequestParam(defaultValue = "6") int limit) {
        return Result.success(strategyService.hotList(limit));
    }

    /**
     * 首页轮播图攻略
     * GET /strategy/banner?limit=5
     */
    @GetMapping("/banner")
    public Result<List<Strategy>> banner(@RequestParam(defaultValue = "5") int limit) {
        return Result.success(strategyService.bannerList(limit));
    }

    /**
     * 所有城市列表（城市导航用）
     * GET /strategy/cities
     */
    @GetMapping("/cities")
    public Result<List<String>> cities() {
        return Result.success(strategyService.listAllCities());
    }

    /**
     * 攻略上下线（作者操作自己的攻略）
     * PUT /strategy/status/{id}?status=0
     * status: 0=下线, 1=上线
     */
    @PutMapping("/status/{id}")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        strategyService.updateStatus(id, status);
        return Result.success();
    }

    /**
     * 点赞切换（限流：每秒 2 次，防止刷点赞量）
     */
    @RateLimit(qps = 2)
    @PostMapping("/like/{id}")
    public Result<Boolean> toggleLike(@PathVariable Long id) {
        boolean isLiked = travelLikeService.toggleLike(id);
        return Result.success(isLiked);
    }

    @PostMapping("/collect/{id}")
    public Result<Boolean> toggleCollect(@PathVariable Long id) {
        boolean isCollected = userCollectService.toggleCollect(id);
        return Result.success(isCollected);
    }

    // 发布评论（支持二级回复：传 parentId）
    @PostMapping("/comment")
    public Result<Long> addComment(@RequestBody CommentDTO dto) {
        Long commentId = travelCommentService.addComment(dto.getStrategyId(), dto.getContent(), dto.getParentId());
        return Result.success(commentId);
    }

    // 删除评论
    @DeleteMapping("/comment/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        travelCommentService.deleteComment(id);
        return Result.success();
    }

    // 评论列表（公开接口，返回 CommentVO 含昵称头像+二级回复）
    @GetMapping("/comment/list/{strategyId}")
    public Result<List<CommentVO>> getCommentList(@PathVariable Long strategyId) {
        List<CommentVO> list = travelCommentService.getCommentList(strategyId);
        return Result.success(list);
    }
}
