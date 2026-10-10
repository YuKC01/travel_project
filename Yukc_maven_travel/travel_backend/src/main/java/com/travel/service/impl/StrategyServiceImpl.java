package com.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travel.common.PageVO;
import com.travel.domain.dto.StrategyPublishDTO;
import com.travel.domain.entity.Strategy;
import com.travel.common.exception.BusinessException;
import com.travel.mapper.StrategyMapper;
import com.travel.service.StrategyService;
import com.travel.service.WeatherService;
import com.travel.common.util.UserContext;
import com.travel.domain.vo.SearchStrategyVO;
import com.travel.domain.vo.StrategyCardVO;
import com.travel.domain.vo.StrategyDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 攻略业务实现类
 */
@Service
@RequiredArgsConstructor
public class StrategyServiceImpl extends ServiceImpl<StrategyMapper, Strategy> implements StrategyService {

    // 类中新增字段，@RequiredArgsConstructor会自动生成构造注入
    private final WeatherService weatherService;
    @Override
    public List<Strategy> listByCity(String cityName) {
        //构造lambda条件对象
        LambdaQueryWrapper<Strategy> wrapper = new LambdaQueryWrapper<>();
        //eq = equal,等于，ne 不等于，like 模糊查询，gt 大于
        //方法引用，MyBatis‑Plus 自动映射数据库字段 等价 SQL：`where city_name = #{cityName}`
        wrapper.eq(Strategy::getCityName,cityName);
        // 只查询前台上线展示的攻略 status=1
        wrapper.eq(Strategy::getStatus, 1);
        // baseMapper来自父类ServiceImpl,调用selectList查询多条返回
        List<Strategy> list = baseMapper.selectList(wrapper);

        return list;
    }
    /**
     * 关键词模糊查询：匹配攻略标题 或 景点名
     * 保证只返回上线状态的攻略
     */
    @Override
    public List<Strategy> listBySpotLike(String keyword){
        LambdaQueryWrapper<Strategy> wrapper = new LambdaQueryWrapper<>();
        // nested 把两个 like 包成一组，再整体 and status = 1
        // 等价 SQL：where (spot_name like ? or title like ?) and status = 1
        wrapper.nested(w -> w.like(Strategy::getSpotName, keyword)
                        .or()
                        .like(Strategy::getTitle, keyword))
                .eq(Strategy::getStatus, 1);
        return baseMapper.selectList(wrapper);
    }


    @Override
    public Strategy getBySpot(String spotName) {
        /*
        【废弃说明】
        旧逻辑假设一个景点只有一篇攻略，使用selectOne；
        现在业务允许同一个景点有多套游玩攻略，selectOne会抛出TooManyResultsException，禁止调用
         */
        LambdaQueryWrapper<Strategy> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Strategy::getSpotName,spotName);
        // 直接抛出运行时异常，防止误调用，不再执行数据库查询
        throw new RuntimeException("该方法已废弃，景点查询请使用selectList集合查询");
    }

    @Override
    public void addViewCount(Long id) {
        if(id == null){
            return;
        }
        LambdaUpdateWrapper<Strategy> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Strategy::getId, id)
                .setSql("view_count = view_count + 1");
        baseMapper.update(null, updateWrapper);
    }
    @Override
    public SearchStrategyVO search(String keyword) {
        SearchStrategyVO resultVo = new SearchStrategyVO();

        //1、优先按城市精确查询
        List<Strategy> cityStrategyList = listByCity(keyword);
        if (!cityStrategyList.isEmpty()) {
            List<StrategyCardVO> targetVoList = cityStrategyList.stream()
                    .map(this::convertToCardVO)
                    .collect(Collectors.toList());
            //天气填充
            fillWeatherForCardList(targetVoList);
            resultVo.setTargetList(targetVoList);
            resultVo.setRecommendList(null);
            return resultVo;
        }

        //2、城市没有命中，走景点模糊查询（调用已有的listBySpotLike）
        List<Strategy> targetEntityList = listBySpotLike(keyword);

        //完全没有搜到任何结果
        if (targetEntityList.isEmpty()) {
            resultVo.setTargetList(new ArrayList<>());
            resultVo.setRecommendList(null);
            resultVo.setMsg("未找到相关攻略");
            return resultVo;
        }

        //组装命中结果
        List<StrategyCardVO> targetVoList = targetEntityList.stream()
                .map(this::convertToCardVO)
                .collect(Collectors.toList());
        resultVo.setTargetList(targetVoList);

        //天气填充
        fillWeatherForCardList(targetVoList);

        //取出命中数据所属城市，查询同城其它攻略做推荐
        String sameCity = targetEntityList.get(0).getCityName();
        List<Strategy> allSameCity = listByCity(sameCity);

        //排除本次命中的id，不要重复出现在推荐列表
        List<Long> targetIdList = targetEntityList.stream()
                .map(Strategy::getId)
                .collect(Collectors.toList());

        List<Strategy> recommendEntityList = allSameCity.stream()
                .filter(item -> !targetIdList.contains(item.getId()))
                .collect(Collectors.toList());

        List<StrategyCardVO> recommendList = recommendEntityList.stream()
                .map(this::convertToCardVO)
                .collect(Collectors.toList());

        fillWeatherForCardList(recommendList);

        resultVo.setRecommendList(recommendList);
        return resultVo;
    }


    @Override
    public StrategyDetailVO getDetail(Long id) {
        // 查询并且只查询上架攻略 status=1
        LambdaQueryWrapper<Strategy> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Strategy::getId, id)
                .eq(Strategy::getStatus, 1);
        Strategy strategy = baseMapper.selectOne(wrapper);

        if(strategy == null){
            return null;
        }
        // 浏览量原子 +1，避免并发丢失更新
        addViewCount(id);

        // 内存中直接 +1 返回，省去第二次数据库查询
        int newView = (strategy.getViewCount() == null ? 0 : strategy.getViewCount()) + 1;
        strategy.setViewCount(newView);

        return convertToDetailVO(strategy, newView);
    }

    /**
     * 发布攻略
     *
     * 【@CacheEvict 执行语义】
     * 方法正常执行完后，删掉指定缓存。下次查询自然重新查库、重建缓存。
     * 与 @Cacheable 区别：@Cacheable 是方法执行前查缓存，@CacheEvict 是方法执行后删缓存。
     *
     * 【为什么 allEntries=true 清空全部而不是清单个】
     * 一次发布可能同时影响热门列表、轮播图、城市列表三个维度，
     * 精确判断影响哪些 key 太复杂且容易漏，干脆一刀切全清。
     * 这是业界最常见的「先更新数据库，再删缓存」策略——简单可靠。
     *
     * 【为什么写操作要清缓存而读操作不清】
     * 写操作低频（发布攻略远远少于浏览），清空成本低；
     * 如果不清，缓存里的旧数据会一直返回到 TTL 过期，导致用户看到下线后的攻略。
     */
    @Override
    @CacheEvict(value = {"strategy:hot", "strategy:banner", "strategy:cities"}, allEntries = true)
    public void publish(StrategyPublishDTO dto) {
        Strategy strategy = new Strategy();
        // 拷贝基础字段
        strategy.setCityName(dto.getCityName());
        strategy.setSpotName(dto.getSpotName());
        strategy.setTag(dto.getTag());
        strategy.setCategory(dto.getCategory());
        strategy.setImgUrl(dto.getImgUrl());
        strategy.setContent(dto.getContent());
        strategy.setTitle(dto.getTitle());

        // 从上下文取当前登录用户，自动绑定作者，前端无法伪造
        Long userId = UserContext.getUserId();
        strategy.setAuthorId(userId);

        // 初始化统计字段
        strategy.setViewCount(0);
        strategy.setLikeCount(0);
        strategy.setCollectCount(0);
        strategy.setCommentCount(0);

        // 状态策略：管理员可直接指定上线(1)，普通用户默认为待审核(2)
        Integer isAdmin = UserContext.getIsAdmin();
        if (isAdmin != null && isAdmin == 1 && dto.getStatus() != null) {
            strategy.setStatus(dto.getStatus());
        } else {
            strategy.setStatus(2); // 普通用户默认待审核
        }

        // MP 父类内置方法，直接保存
        this.save(strategy);
    }

    // ========== 新增：编辑攻略 ==========
    /**
     * 编辑攻略后清空缓存
     */
    @Override
    @CacheEvict(value = {"strategy:hot", "strategy:banner", "strategy:cities"}, allEntries = true)
    public void update(StrategyPublishDTO dto) {
        // 1. 校验 id
        if (dto.getId() == null) {
            throw new BusinessException(400, "攻略ID不能为空");
        }
        // 2. 校验攻略是否存在
        Strategy strategy = this.getById(dto.getId());
        if (strategy == null) {
            throw new BusinessException(404, "攻略不存在或已删除");
        }

        // 3. 权限校验：管理员直接放行，普通用户只能编辑自己的攻略
        Long currentUserId = UserContext.getUserId();
        Integer isAdmin = UserContext.getIsAdmin();
        if (isAdmin != 1 && !strategy.getAuthorId().equals(currentUserId)) {
            throw new BusinessException(403, "无权编辑他人的攻略");
        }

        // 4. 只更新非空字段，避免 null 覆盖已有数据
        LambdaUpdateWrapper<Strategy> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Strategy::getId, dto.getId());
        if (dto.getCityName() != null) updateWrapper.set(Strategy::getCityName, dto.getCityName());
        if (dto.getSpotName() != null) updateWrapper.set(Strategy::getSpotName, dto.getSpotName());
        if (dto.getTag() != null) updateWrapper.set(Strategy::getTag, dto.getTag());
        if (dto.getCategory() != null) updateWrapper.set(Strategy::getCategory, dto.getCategory());
        if (dto.getImgUrl() != null) updateWrapper.set(Strategy::getImgUrl, dto.getImgUrl());
        if (dto.getContent() != null) updateWrapper.set(Strategy::getContent, dto.getContent());
        if (dto.getTitle() != null) updateWrapper.set(Strategy::getTitle, dto.getTitle());
        this.update(null, updateWrapper);
    }

    // ========== 新增：删除攻略 ==========
    /**
     * 删除攻略后清空缓存
     */
    @Override
    @CacheEvict(value = {"strategy:hot", "strategy:banner", "strategy:cities"}, allEntries = true)
    public void delete(Long id) {
        // 1. 校验攻略是否存在
        Strategy strategy = this.getById(id);
        if (strategy == null) {
            throw new BusinessException(404, "攻略不存在或已删除");
        }

        // 2. 权限校验：管理员直接放行，普通用户只能删除自己的攻略
        Long currentUserId = UserContext.getUserId();
        Integer isAdmin = UserContext.getIsAdmin();
        if (isAdmin != 1 && !strategy.getAuthorId().equals(currentUserId)) {
            throw new BusinessException(403, "无权删除他人的攻略");
        }

        // MP 父类内置方法，按主键物理删除
        this.removeById(id);
    }
    @Override
    public List<Strategy> listAll() {
        // 只返回上线(status=1)的攻略，下线/待审核的不展示给普通用户
        LambdaQueryWrapper<Strategy> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Strategy::getStatus, 1)
                .orderByDesc(Strategy::getCreateTime);
        return this.list(wrapper);
    }


    /**
     * 【私有工具】实体转详情VO
     * @param strategy 数据库实体
     * @param realViewCount 真实浏览量（数据库更新之后的数值，处理内存对象不刷新问题）
     * @return 组装完成VO
     */
    //执行了浏览量 + 1 更新，内存实体不会刷新，需要传入数据库浏览量的最新值
    private StrategyDetailVO convertToDetailVO(Strategy strategy, Integer realViewCount) {
        StrategyDetailVO vo = new StrategyDetailVO();
        vo.setId(strategy.getId());
        vo.setTitle(strategy.getTitle());
        vo.setCityName(strategy.getCityName());
        vo.setSpotName(strategy.getSpotName());
        vo.setContent(strategy.getContent());
        vo.setImgUrl(strategy.getImgUrl());
        vo.setTag(strategy.getTag());
        vo.setCategory(strategy.getCategory());
        vo.setLikeCount(strategy.getLikeCount());
        vo.setViewCount(realViewCount);
        vo.setCreateTime(strategy.getCreateTime());

        // 计算是否热门，阈值20
        boolean hot = realViewCount >= 20;
        vo.setIsHot(hot);

        // ========== 新增：填充天气与出行提示 ==========
        String[] weatherInfo = weatherService.getWeatherInfo(strategy.getCityName());
        if (weatherInfo != null) {
            vo.setWeather(weatherInfo[0]);
            vo.setTemperature(weatherInfo[1]);
            vo.setTravelTip(weatherInfo[2]);
        } else {
            // 接口失败降级：返回友好默认值，不影响页面渲染
            vo.setWeather("暂无数据");
            vo.setTemperature("--");
            vo.setTravelTip("出行前请留意当地天气变化，注意安全。");
        }

        return vo;
    }

    /**
     * 【私有工具】实体转卡片VO，用于搜索列表、同城推荐
     * @param entity 攻略实体
     * @return 组装完成卡片VO
     */
   //只做查询，无数据库更新，实体内字段可用
    private StrategyCardVO convertToCardVO(Strategy entity){
        StrategyCardVO card = new StrategyCardVO();
        card.setId(entity.getId());
        card.setTitle(entity.getTitle());
        card.setCityName(entity.getCityName());
        card.setSpotName(entity.getSpotName());
        card.setImgUrl(entity.getImgUrl());
        card.setTag(entity.getTag());
        card.setCategory(entity.getCategory());
        card.setViewCount(entity.getViewCount());
        card.setLikeCount(entity.getLikeCount());
        card.setWeather(null);
        card.setTemperature(null);

        //处理多标签，逗号分割，去除空格

        //计算是否热门，阈值10
        boolean hot = entity.getViewCount() != null && entity.getViewCount() >= 20;
        card.setIsHot(hot);

        return card;
    }
    private void fillWeatherForCardList(List<StrategyCardVO> cardList) {
        if (cardList == null || cardList.isEmpty()) {
            return;
        }

        // 提取城市去重，同城市只调用一次接口
        java.util.Set<String> citySet = cardList.stream()
                .map(StrategyCardVO::getCityName)
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());

        java.util.Map<String, String[]> weatherMap = new java.util.HashMap<>();
        for (String city : citySet) {
            String[] weatherInfo = weatherService.getWeatherInfo(city);
            weatherMap.put(city, weatherInfo);
        }

        // 只回填天气+温度
        for (StrategyCardVO card : cardList) {
            String[] weatherInfo = weatherMap.get(card.getCityName());
            if (weatherInfo != null) {
                card.setWeather(weatherInfo[0]);
                card.setTemperature(weatherInfo[1]);
            } else {
                card.setWeather("暂无");
                card.setTemperature("--");
            }
        }
    }

    // ===================== 新增：个人中心 + 筛选 + 热门 =====================

    @Override
    public List<Strategy> listMyPublished(Long userId) {
        // 查当前用户发布的所有攻略（包括下线和待审核，方便用户管理）
        LambdaQueryWrapper<Strategy> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Strategy::getAuthorId, userId)
                .orderByDesc(Strategy::getCreateTime);
        return this.list(wrapper);
    }

    /**
     * 上下线切换后清空缓存（状态变化会影响热门/轮播/城市列表）
     */
    @Override
    @CacheEvict(value = {"strategy:hot", "strategy:banner", "strategy:cities"}, allEntries = true)
    public void updateStatus(Long id, Integer status) {
        Strategy strategy = this.getById(id);
        if (strategy == null) {
            throw new BusinessException(404, "攻略不存在");
        }
        // 权限校验：作者本人或管理员
        Long currentUserId = UserContext.getUserId();
        Integer isAdmin = UserContext.getIsAdmin();
        if (isAdmin != 1 && !strategy.getAuthorId().equals(currentUserId)) {
            throw new BusinessException(403, "无权操作他人的攻略");
        }
        // 只允许在 0(下线) 和 1(上线) 之间切换，不能自行改为 2(待审核)
        if (status != 0 && status != 1) {
            throw new BusinessException(400, "状态值非法");
        }
        strategy.setStatus(status);
        this.updateById(strategy);
    }

    /**
     * 管理员审核攻略，审核通过/驳回后清空缓存
     * 与 updateStatus 的区别：audit 不校验作者权限（管理员可审核任意攻略），
     * 且允许从待审核(2)状态变更；updateStatus 只允许在 0/1 之间切换且校验作者。
     */
    @Override
    @CacheEvict(value = {"strategy:hot", "strategy:banner", "strategy:cities"}, allEntries = true)
    public void audit(Long id, Integer status) {
        Strategy strategy = this.getById(id);
        if (strategy == null) {
            throw new BusinessException(404, "攻略不存在");
        }
        if (status != 0 && status != 1) {
            throw new BusinessException(400, "审核状态非法");
        }
        strategy.setStatus(status);
        this.updateById(strategy);
    }

    @Override
    public PageVO<Strategy> filter(String category, String tag, long pageNum, long pageSize) {
        LambdaQueryWrapper<Strategy> wrapper = new LambdaQueryWrapper<>();
        // 只展示上线攻略
        wrapper.eq(Strategy::getStatus, 1);
        // 分类筛选（可选）：StringUtils.isNotBlank 避免空字符串误匹配
        if (category != null && !category.trim().isEmpty()) {
            wrapper.eq(Strategy::getCategory, category);
        }
        // 标签筛选（可选）
        if (tag != null && !tag.trim().isEmpty()) {
            wrapper.eq(Strategy::getTag, tag);
        }
        // 按创建时间倒序
        wrapper.orderByDesc(Strategy::getCreateTime);
        // 分页查询：Page(pageNum, pageSize)
        Page<Strategy> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        return PageVO.of(page);
    }

    /**
     * 热门攻略列表（缓存）
     *
     * 【@Cacheable 执行流程】
     * 1. Spring AOP 代理拦截方法调用
     * 2. 计算 key：value + "::" + SpEL("#limit") → strategy:hot::5
     * 3. 查 Redis：
     *    - 命中 → 直接返回，方法体根本不执行（这行 this.list(wrapper) 不会跑）
     *    - 未命中 → 执行方法体查 MySQL → 结果写入 Redis（TTL 30分钟）→ 返回
     *
     * 【属性详解】
     * - value="strategy:hot"：缓存命名空间（分组），相当于 Redis key 前缀
     * - key="#limit"：SpEL 表达式，# 表示引用方法参数，所以 limit=5 时 key 后缀是 5
     *   对比：key="#dto.id" 是取参数对象属性，key="'all'" 是字符串字面量（见 listAllCities）
     *
     * 【验证实验】
     * redis-cli 执行 del strategy:hot::5 后访问接口，再 keys strategy:* 会发现 key 重新出现
     */
    @Override
    @Cacheable(value = "strategy:hot", key = "#limit")
    public List<Strategy> hotList(int limit) {
        // 热门 = 上线且浏览量最高的前 N 条
        LambdaQueryWrapper<Strategy> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Strategy::getStatus, 1)
                .orderByDesc(Strategy::getViewCount)
                .last("LIMIT " + limit);
        return this.list(wrapper);
    }

    /**
     * 轮播图列表（缓存）
     *
     * 与 hotList 同样的缓存逻辑，区别在查询条件：isNotNull(imgUrl) 过滤掉没有封面图的攻略
     * key="#limit" → 不同 limit 参数会生成不同缓存项（如 strategy:banner::5、strategy:banner::10）
     */
    @Override
    @Cacheable(value = "strategy:banner", key = "#limit")
    public List<Strategy> bannerList(int limit) {
        // 轮播图 = 上线 + 有封面图 + 浏览量最高的前 N 条
        LambdaQueryWrapper<Strategy> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Strategy::getStatus, 1)
                .isNotNull(Strategy::getImgUrl)
                .orderByDesc(Strategy::getViewCount)
                .last("LIMIT " + limit);
        return this.list(wrapper);
    }

    /**
     * 所有城市列表（缓存）
     *
     * 【key 写法对比】
     * - hotList/bannerList：key="#limit"，参数不同生成不同缓存项
     * - listAllCities：key="'all'"，方法无参，用字符串字面量做固定 key
     *   单引号表示字符串字面量，不写 #，所有调用共享同一个缓存项 strategy:cities::all
     *
     * 【SpEL 速记】
     * - #变量名 → 引用方法参数
     * - #对象.属性 → 引用参数对象属性
     * - '字面量' → 字符串常量
     * - #result → 引用返回值（只能用在 unless 属性）
     */
    @Override
    @Cacheable(value = "strategy:cities", key = "'all'")
    public List<String> listAllCities() {
        // 从所有上线攻略中提取去重的城市名
        LambdaQueryWrapper<Strategy> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Strategy::getStatus, 1)
                .select(Strategy::getCityName)
                .groupBy(Strategy::getCityName);
        return this.list(wrapper).stream()
                .map(Strategy::getCityName)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
