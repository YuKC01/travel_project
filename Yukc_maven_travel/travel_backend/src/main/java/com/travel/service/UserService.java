package com.travel.service;

import com.travel.domain.dto.UserLoginDTO;
import com.travel.domain.dto.UserRegisterDTO;
import com.travel.domain.vo.UserLoginVO;

/**
 * 用户业务接口
 * 定义用户模块所有业务方法
 */
public interface UserService {

    /**
     * 用户注册
     * @param dto 注册参数
     */
    void register(UserRegisterDTO dto);

    /**
     * 用户登录
     * @param dto 登录参数
     * @return 登录成功信息（含token）
     */
    UserLoginVO login(UserLoginDTO dto);
}
