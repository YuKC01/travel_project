package com.travel.service;

import com.travel.domain.entity.Strategy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * StrategyService单元测试
 */
//这个注解：启动SpringBoot完整上下文，把所有Bean全部创建出来，我们才可以注入Service
@SpringBootTest
public class StrategyServiceTest {

    // 把我们写好的StrategyService从Spring容器注入进来
    @Autowired
    private StrategyService strategyService;

    @Test
    void testQueryByCity(){
        //测试：根据城市查询攻略
        List<Strategy> list = strategyService.listByCity("西安");
        //打印结果，看控制台输出
        System.out.println("查询结果数量："+ list.size());
        for (Strategy strategy : list){
            System.out.println(strategy);
        }
    }
    @Test
    void testQueryBySpotName(){
        // 测试根据景点名字查询
        Strategy strategy = strategyService.getBySpot("兵马俑");
        System.out.println("查询到的攻略：" + strategy);
    }

    @Test
    void testAddViewCount(){
        // 测试浏览量+1，填写你数据库真实存在的攻略id
        strategyService.addViewCount(1L);//1L表示Long长整形
    }
    @Test
    void testLombok(){
        Strategy s = new Strategy();
        s.setCityName("西安");
        System.out.println(s.getCityName());
    }


}
