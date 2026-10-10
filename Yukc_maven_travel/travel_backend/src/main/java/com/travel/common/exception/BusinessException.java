package com.travel.common.exception;

import lombok.Data;

/**
 * 自定义业务异常类
 * 用于业务逻辑中主动抛出可预期的业务错误
 * 由全局异常处理器统一捕获处理
 */
public class BusinessException extends RuntimeException {

    // 自定义错误码
    private final Integer code;

    /**
     * 构造方法
     * @param code 错误码
     * @param message 错误提示信息
     */
    public BusinessException(Integer code, String message) {
        // 调用父类构造方法，传入错误信息
        super(message);
        this.code = code;
    }

    // 只提供getter，不提供setter，保证异常对象不可修改
    public Integer getCode() {
        return code;
    }
}
