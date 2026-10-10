package com.travel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
@Tag(name = "测试接口", description = "用来验证SpringDoc文档是否正常")
public class TestController {

    @GetMapping("/hello")
    @Operation(summary = "测试Hello接口", description = "最简单的测试接口，验证文档页面和接口调用")
    public String hello(
            @Parameter(description = "你的名字", required = true)
            @RequestParam String name
    ){
        return "你好，" + name + "，旅图足迹项目";
    }
}
