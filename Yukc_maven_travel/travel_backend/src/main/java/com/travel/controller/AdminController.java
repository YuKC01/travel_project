package com.travel.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.common.PageVO;
import com.travel.common.Result;
import com.travel.common.exception.BusinessException;
import com.travel.common.util.UserContext;
import com.travel.domain.dto.StrategyPublishDTO;
import com.travel.domain.entity.Strategy;
import com.travel.domain.entity.User;
import com.travel.domain.vo.StrategyDetailVO;
import com.travel.mapper.UserMapper;
import com.travel.service.StrategyService;
import com.travel.service.TravelCommentService;
import com.travel.service.TravelLikeService;
import com.travel.service.UserCollectService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员后台控制器
 *
 * 所有接口都需要管理员权限（isAdmin=1）
 * 模块：
 * 1. 用户管理：列表、启用/禁用
 * 2. 攻略管理：列表（含待审核/下线）、审核、上下线、删除
 * 3. 数据统计：用户数、攻略数、浏览量、热门攻略
 *
 * 知识点：
 * - 权限拦截：每个接口开头校验 isAdmin，防止越权
 * - 全量数据：管理员能看到所有状态的攻略（包括待审核、下线），普通用户只能看到上线的
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserMapper userMapper;
    private final StrategyService strategyService;
    private final TravelLikeService travelLikeService;
    private final TravelCommentService travelCommentService;
    private final UserCollectService userCollectService;

    /** 校验当前用户是否为管理员，不是则抛异常 */
    private void checkAdmin() {
        Integer isAdmin = UserContext.getIsAdmin();
        if (isAdmin == null || isAdmin != 1) {
            throw new BusinessException(403, "无管理员权限");
        }
    }

    // ===================== 数据统计 =====================

    /**
     * 首页统计数据
     * GET /admin/stats
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        checkAdmin();
        Map<String, Object> stats = new HashMap<>();

        // 用户总数
        Long userCount = userMapper.selectCount(null);
        stats.put("userCount", userCount);

        // 攻略总数
        Long strategyCount = strategyService.count();
        stats.put("strategyCount", strategyCount);

        // 上线攻略数
        Long onlineCount = strategyService.count(new LambdaQueryWrapper<Strategy>().eq(Strategy::getStatus, 1));
        stats.put("onlineCount", onlineCount);

        // 待审核攻略数
        Long pendingCount = strategyService.count(new LambdaQueryWrapper<Strategy>().eq(Strategy::getStatus, 2));
        stats.put("pendingCount", pendingCount);

        // 总浏览量
        List<Strategy> allStrategies = strategyService.list();
        long totalViews = allStrategies.stream().mapToLong(s -> s.getViewCount() == null ? 0 : s.getViewCount()).sum();
        stats.put("totalViews", totalViews);

        // 总点赞数
        long totalLikes = allStrategies.stream().mapToLong(s -> s.getLikeCount() == null ? 0 : s.getLikeCount()).sum();
        stats.put("totalLikes", totalLikes);

        // 评论总数
        stats.put("commentCount", travelCommentService.count());

        // 热门攻略 Top5（按浏览量）
        List<Strategy> hotStrategies = strategyService.hotList(5);
        stats.put("hotStrategies", hotStrategies);

        return Result.success(stats);
    }

    // ===================== 用户管理 =====================

    /**
     * 用户列表（分页）
     * GET /admin/users?pageNum=1&pageSize=10
     */
    @GetMapping("/users")
    public Result<PageVO<User>> userList(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        checkAdmin();
        Page<User> page = userMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<User>().orderByDesc(User::getCreateTime));
        // 密码不返回给前端
        page.getRecords().forEach(u -> u.setPassword(null));
        return Result.success(PageVO.of(page));
    }

    /**
     * 启用/禁用用户
     * PUT /admin/users/{id}/status?status=0
     * status: 1启用 0禁用
     */
    @PutMapping("/users/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        checkAdmin();
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        user.setStatus(status);
        userMapper.updateById(user);
        return Result.success();
    }

    // ===================== 攻略管理 =====================

    /**
     * 攻略列表（全量，含所有状态，分页）
     * GET /admin/strategies?pageNum=1&pageSize=10&status=
     */
    @GetMapping("/strategies")
    public Result<PageVO<Strategy>> strategyList(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) Integer status) {
        checkAdmin();
        LambdaQueryWrapper<Strategy> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Strategy::getStatus, status);
        }
        wrapper.orderByDesc(Strategy::getCreateTime);
        Page<Strategy> page = strategyService.page(new Page<>(pageNum, pageSize), wrapper);
        return Result.success(PageVO.of(page));
    }

    /**
     * 管理员新增攻略（可直接上线）
     * POST /admin/strategies
     */
    @PostMapping("/strategies")
    public Result<Void> addStrategy(@RequestBody StrategyPublishDTO dto) {
        checkAdmin();
        strategyService.publish(dto);
        return Result.success();
    }

    /**
     * 管理员编辑攻略
     * PUT /admin/strategies
     */
    @PutMapping("/strategies")
    public Result<Void> updateStrategy(@RequestBody StrategyPublishDTO dto) {
        checkAdmin();
        strategyService.update(dto);
        return Result.success();
    }

    /**
     * 删除攻略
     * DELETE /admin/strategies/{id}
     */
    @DeleteMapping("/strategies/{id}")
    public Result<Void> deleteStrategy(@PathVariable Long id) {
        checkAdmin();
        strategyService.delete(id);
        return Result.success();
    }

    /**
     * 审核攻略（待审核→上线/下线）
     * PUT /admin/strategies/{id}/audit?status=1
     * status: 1通过上线 0驳回下线
     */
    @PutMapping("/strategies/{id}/audit")
    public Result<Void> auditStrategy(@PathVariable Long id, @RequestParam Integer status) {
        checkAdmin();
        // 调用 Service 的 audit 方法，内部会清空缓存
        strategyService.audit(id, status);
        return Result.success();
    }

    /**
     * 攻略详情（管理员看，不受状态限制）
     * GET /admin/strategies/{id}
     */
    @GetMapping("/strategies/{id}")
    public Result<Strategy> strategyDetail(@PathVariable Long id) {
        checkAdmin();
        Strategy strategy = strategyService.getById(id);
        if (strategy == null) {
            throw new BusinessException(404, "攻略不存在");
        }
        return Result.success(strategy);
    }
}
