package com.travel.common;

import lombok.Data;

import java.util.List;

/**
 * 通用分页结果封装
 * 所有列表接口统一返回此结构，前端可以统一解析分页信息
 *
 * 知识点：
 * - records：当前页的数据列表
 * - total：符合条件的总记录数（前端用来计算总页数 = ceil(total / pageSize)）
 * - pageNum：当前页码（从 1 开始）
 * - pageSize：每页条数
 * - hasNext：是否有下一页（前端可以用这个控制"加载更多"按钮是否显示）
 *
 * 泛型 <T>：让这个 PageVO 可以装任意类型的列表数据，
 * 比如 PageVO<Strategy>、PageVO<TravelComment>，不用为每种类型写一个类
 */
@Data
public class PageVO<T> {

    private List<T> records;

    private long total;

    private long pageNum;

    private long pageSize;

    private boolean hasNext;

    /**
     * 从 MyBatis-Plus 的 IPage 对象转换为统一 PageVO
     * @param page MP 的分页结果
     * @param <T> 数据类型
     */
    public static <T> PageVO<T> of(com.baomidou.mybatisplus.core.metadata.IPage<T> page) {
        PageVO<T> vo = new PageVO<>();
        vo.setRecords(page.getRecords());
        vo.setTotal(page.getTotal());
        vo.setPageNum(page.getCurrent());
        vo.setPageSize(page.getSize());
        vo.setHasNext(page.getCurrent() < page.getPages());
        return vo;
    }
}
