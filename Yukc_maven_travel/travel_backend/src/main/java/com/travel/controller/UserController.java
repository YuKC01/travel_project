package com.travel.controller;

import com.travel.common.Result;
import com.travel.domain.dto.UserLoginDTO;
import com.travel.domain.dto.UserRegisterDTO;
import com.travel.service.UserService;
import com.travel.domain.vo.UserLoginVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户模块控制层
 * 提供用户注册、登录相关HTTP接口
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
//Lombok 注解，生成包含所有 final 字段的构造方法。Spring 会自动通过构造方法注入 UserService 对象，不用手动写 @Autowired，这是 Spring 推荐的注入方式。
public class UserController {

    private final UserService userService;

    /**
     * 用户注册接口
     * 请求方式：POST
     * 请求路径：/user/register
     */
    @PostMapping("/register")
    public Result<Void> register(@RequestBody @Valid UserRegisterDTO dto) {
        userService.register(dto);
        return Result.success();
    }

    /**
     * 用户登录接口
     * 请求方式：POST
     * 请求路径：/user/login
     */
    @PostMapping("/login")
    public Result<UserLoginVO> login(@RequestBody @Valid UserLoginDTO dto) {
        UserLoginVO loginVO = userService.login(dto);
        return Result.success(loginVO);
    }
}
