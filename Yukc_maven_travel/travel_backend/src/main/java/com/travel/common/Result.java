package com.travel.common;

import lombok.Data;

/**
 * 全局统一返回结果封装类
 * 规范所有接口响应JSON格式，便于前端统一解析
 */
@Data
public class Result<T> {

    /**
     * 响应码：200成功，500系统异常，400参数错误
     */
    private Integer code;

    /**
     * 响应提示信息
     */
    private String msg;

    /**
     * 返回业务数据，泛型T兼容任意类型
     */
    private T data;

    // ===================== 静态快捷构造方法 =====================
    /**
     * 成功返回（带数据）
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMsg(ResultCode.SUCCESS.getMessage());
        result.setData(data);
        return result;
    }

    /**
     * 成功返回（无返回数据）
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 失败返回（自定义编码和提示）
     */
    public static <T> Result<T> fail(Integer code, String msg) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(null);
        return result;
    }

    /**
     * 重载：直接传入枚举返回失败
     */
    public static <T> Result<T> fail(ResultCode resultCode) {
        return fail(resultCode.getCode(), resultCode.getMessage());
    }
}