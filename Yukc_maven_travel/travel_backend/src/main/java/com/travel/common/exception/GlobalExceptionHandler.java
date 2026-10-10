package com.travel.common.exception;

import com.travel.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 统一捕获所有Controller层抛出的异常，封装为标准Result格式返回
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理自定义业务异常
     */
    @ExceptionHandler(BusinessException.class)//业务 code + msg
    public Result<Void> handleBusinessException(BusinessException e) {
        // 业务异常是正常分支，打印warn日志即可
        log.warn("业务异常：{}", e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)//400 + 校验信息
    public Result<Void> handleValidException(MethodArgumentNotValidException e) {
        // 提取校验注解里的错误提示信息
        String message = e.getBindingResult().getFieldError().getDefaultMessage();
        log.warn("参数校验失败：{}", message);
        return Result.fail(400, message);
    }

    /**
     * 处理系统异常（兜底）
     */
    @ExceptionHandler(Exception.class)//500 + 通用提示
    public Result<Void> handleException(Exception e) {
        // 系统异常打印完整错误堆栈，方便排查问题
        log.error("系统异常", e);
        // 前端只返回通用提示，不暴露内部细节
        return Result.fail(500, "系统内部错误，请稍后重试");
    }
}
