package com.travel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.travel.domain.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户持久层Mapper
 * 继承BaseMapper获得MyBatis-Plus封装的单表CRUD方法
 * DAO层只负责数据库操作，无任何业务逻辑
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 现阶段基础 CRUD 都继承自 BaseMapper，不用自己写
    // 后面需要复杂查询（比如多表关联、自定义 SQL）时，再在这里加方法
}