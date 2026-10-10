package com.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.travel.domain.dto.UserLoginDTO;
import com.travel.domain.dto.UserRegisterDTO;
import com.travel.domain.entity.User;
import com.travel.common.exception.BusinessException;
import com.travel.mapper.UserMapper;
import com.travel.service.UserService;
import com.travel.common.util.BCryptUtil;
import com.travel.common.util.JwtUtil;
import com.travel.domain.vo.UserLoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    // 注入Mapper和工具类，final修饰配合@RequiredArgsConstructor实现构造器注入
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    /**
     * 用户注册业务逻辑
     */
    @Override
    public void register(UserRegisterDTO dto) {
        // 1. 校验用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        Long countUser = userMapper.selectCount(wrapper);
        if (countUser > 0) {
            throw new BusinessException(409, "用户名已存在");
        }

        // 2. 校验邮箱是否已被注册
        if (StringUtils.isNotBlank(dto.getEmail())) {
            wrapper.clear();
            wrapper.eq(User::getEmail, dto.getEmail());
            Long countEmail = userMapper.selectCount(wrapper);
            if (countEmail > 0) {
                throw new BusinessException(409, "邮箱已被注册");
            }
        }

        // 3. 构建用户对象，密码加密后入库
        User user = new User();
        user.setUsername(dto.getUsername());
        // BCrypt加密明文密码，数据库只存密文
        user.setPassword(BCryptUtil.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());
        // 默认注册为普通用户，禁止前端指定管理员身份
        user.setIsAdmin(0);
        // 新注册用户默认正常状态
        user.setStatus(1);

        // 4. 插入数据库
        userMapper.insert(user);
    }

    /**
     * 用户登录业务逻辑
     */
    @Override
    public UserLoginVO login(UserLoginDTO dto) {
        // 1. 根据用户名查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        User user = userMapper.selectOne(wrapper);

        // 2. 用户不存在，直接返回错误
        // 统一提示"账号或密码错误"，不明确告诉用户是账号错还是密码错，防止恶意枚举
        if (user == null) {
            throw new BusinessException(400, "账号或密码错误");
        }

        // 3. 比对密码
        boolean match = BCryptUtil.matches(dto.getPassword(), user.getPassword());
        if (!match) {
            throw new BusinessException(400, "账号或密码错误");
        }

        // 4. 校验账号状态：被禁用的用户不允许登录
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(403, "账号已被禁用，请联系管理员");
        }

        // 4. 生成JWT令牌
        String token = jwtUtil.generateToken(user.getId(), user.getIsAdmin());

        // 5. 封装返回对象（带昵称和头像，前端登录后可直接显示）
        return new UserLoginVO(
                user.getId(),
                user.getUsername(),
                user.getNickName(),
                user.getAvatar(),
                user.getIsAdmin(),
                token
        );
    }
}
