package com.travel.common;

/**
 * 全局响应状态码枚举
 * 统一管理系统返回编码，避免硬编码数字
 */
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    FAIL(500, "服务器内部异常"),
    PARAM_ERROR(400, "请求参数非法"),
    USER_NOT_FOUND(404, "用户不存在"),
    KEYWORD_EMPTY(400,"搜索关键词不能为空"),
    STRATEGY_NOT_EXIST(404,"攻略不存在或者已经下线"),
    UNAUTHORIZED(401, "未授权");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}