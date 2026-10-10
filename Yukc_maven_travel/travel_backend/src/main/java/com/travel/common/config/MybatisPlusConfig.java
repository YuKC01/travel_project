package com.travel.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置类
 * 核心作用：注册分页插件，让 service.page() 方法真正生效
 *
 * 知识点：
 * - MybatisPlusInterceptor 是 MP 的拦截器主体，可以挂载多个 InnerInterceptor
 * - PaginationInnerInterceptor 是分页拦截器，负责在 SQL 执行前自动添加 LIMIT 子句
 *   和 COUNT 查询，无需手写分页 SQL
 * - DbType.MYSQL 指定数据库类型，MP 会根据不同数据库生成对应的分页 SQL
 *   （MySQL 用 LIMIT，Oracle 用 ROWNUM，PostgreSQL 用 LIMIT/OFFSET）
 */
@Configuration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 注册分页拦截器，指定数据库为 MySQL
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
