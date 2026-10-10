package com.travel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.travel.domain.entity.Strategy;
import org.apache.ibatis.annotations.Mapper;

/**
 * 旅游攻略Mapper接口
 * 负责strategy表的数据操作
 */
//只写`@Mapper`，MyBatis 生成代理并且注册到 Spring，不写@Repository够用
    //@Repository负责把 Mapper 接口交给 SpringIoC 容器，当做 Bean 管理，主要用来把数据库相关异常做翻译转换
    //只要你整合了 mybatis‑spring（MyBatisExceptionTranslator），不管写不写`@Repository`，异常翻译都会自动生效
@Mapper//告诉 Spring，这是 MyBatis 的 Mapper 接口，项目启动时自动扫描，生成这个接口的代理对象，才能注入到 Service 里面使用
public interface StrategyMapper extends BaseMapper<Strategy> {
    //泛型`<Strategy>`：告诉 MyBatisPlus，这个 Mapper 操作的实体是 Strategy，对应 strategy 表


}
