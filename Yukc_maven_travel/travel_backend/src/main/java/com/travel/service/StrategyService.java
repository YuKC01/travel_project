package com.travel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travel.domain.dto.StrategyPublishDTO;
import com.travel.domain.entity.Strategy;
import com.travel.domain.vo.MyStrategyVO;
import com.travel.domain.vo.SearchStrategyVO;
import com.travel.domain.vo.StrategyDetailVO;

import java.util.List;

/**
 * 攻略业务接口层
 * 定义攻略相关所有业务方法声明
 */
public interface StrategyService extends IService<Strategy> {

    /**
     * 根据城市名称查询攻略列表
     * @param cityName 城市名字
     * @return 攻略集合
     */
    List<Strategy> listByCity(String cityName);

    /**
     * 根据景点名称查询攻略列表
     * @param spotName 城市名字
     * @return 攻略集合
     */
    List<Strategy> listBySpotLike(String spotName);


    /**
     * @deprecated 【废弃】同一个景点支持多条攻略，不能返回单个对象，禁止Controller调用
     * 根据景点名称查询攻略
     * @param spotName 景点名
     * @return 攻略对象
     */
    @Deprecated
    Strategy getBySpot(String spotName);
    //方法逻辑已经错误，但是删掉会引发大量编译报错：@Deprecated 标记 + 内部抛异常，保留代码，禁止使用

    /**
     * 攻略访问，浏览量自增
     * @param id 攻略id
     */
    void addViewCount(Long id);

    // ============ 新对外接口（Controller只调用下面这一组） ============
    /**
     * 攻略搜索对外接口
     * @param keyword 搜索关键词 城市 / 景点
     * @return 搜索结果VO
     */
    SearchStrategyVO search(String keyword);

    /**
     * 获取攻略详情，访问量自动+1
     * @param id 攻略id
     * @return 详情VO
     */
    StrategyDetailVO getDetail(Long id);


    /**
     * 发布新攻略
     * @param dto 攻略内容
     */
    void publish(StrategyPublishDTO dto);

    /**
     * 编辑攻略
     * @param dto 攻略内容（包含id）
     */
    void update(StrategyPublishDTO dto);

    /**
     * 删除攻略
     * @param id 攻略ID
     */
    void delete(Long id);
    /**
     * 管理员新增攻略
     * @param strategy 攻略实体
     * @return 是否成功
     */

    /**
     * 查询全量攻略列表（首页用）
     */
    List<Strategy> listAll();

    /**
     * 查询我发布的攻略
     * @param userId 用户ID
     * @return 攻略列表（含状态，方便显示上线/下线/待审核）
     */
    List<Strategy> listMyPublished(Long userId);

    /**
     * 攻略上下线（作者操作自己的攻略）
     * @param id 攻略ID
     * @param status 目标状态：0下线 1上线
     */
    void updateStatus(Long id, Integer status);

    /**
     * 管理员审核攻略（待审核→上线/下线），审核后清缓存
     * @param id 攻略ID
     * @param status 目标状态：1通过上线 0驳回下线
     */
    void audit(Long id, Integer status);

    /**
     * 按分类和标签筛选攻略（分页）
     * @param category 分类（可为null）
     * @param tag 标签（可为null）
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 分页攻略列表
     */
    com.travel.common.PageVO<Strategy> filter(String category, String tag, long pageNum, long pageSize);

    /**
     * 热门攻略（按浏览量排序取前N）
     * @param limit 条数
     * @return 攻略列表
     */
    List<Strategy> hotList(int limit);

    /**
     * 轮播图攻略（取浏览量Top且有封面的）
     * @param limit 条数
     * @return 攻略列表
     */
    List<Strategy> bannerList(int limit);

    /**
     * 按城市分组，返回所有城市及对应攻略数
     * @return 城市名称列表
     */
    List<String> listAllCities();
}
