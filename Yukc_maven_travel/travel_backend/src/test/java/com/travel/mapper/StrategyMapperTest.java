package com.travel.mapper;

import com.travel.domain.entity.Strategy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.List;

/**
 * StrategyMapper单元测试
 */
@SpringBootTest//启动完整的 SpringBoot 上下文，会自动读取 yml 配置、连接数据库、扫描 Mapper，只有加这个注解，`@Autowired`注入 Mapper 才生效
public class StrategyMapperTest {

    // 自动注入我们写好的Mapper
    @Autowired
    private StrategyMapper strategyMapper;

    /**
     * 测试查询所有攻略
     */
    @Test//标记这个方法是测试方法，可以单独运行，不用 main 方法
    void testSelectAll() {
        //BaseMapper 自带方法：查询表里面所有数据；参数为 null 代表不加任何查询条件。
        List<Strategy> strategyList = strategyMapper.selectList(null);
        System.out.println("查询到的攻略数量：" + strategyList.size());
        for (Strategy strategy : strategyList) {
            System.out.println(strategy);
        }
    }
}
