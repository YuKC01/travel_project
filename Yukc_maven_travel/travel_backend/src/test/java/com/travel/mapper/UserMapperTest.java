package com.travel.mapper;

import com.travel.domain.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * UserMapper 持久层单元测试
 * 仅测试数据库底层CRUD，不调用Service、Controller层
 */
@SpringBootTest
class UserMapperTest {

    // 自动注入Mapper对象
    @Autowired
    private UserMapper userMapper;

    /**
     * 查询所有用户列表
     */
    @Test
    void testSelectAll() {
        List<User> userList = userMapper.selectList(null);
        userList.forEach(System.out::println);
    }

    /**
     * 根据ID单条查询
     */
    @Test
    void testSelectById() {
        User user = userMapper.selectById(1L);
        System.out.println(user);
    }
}