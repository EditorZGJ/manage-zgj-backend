package com.example.common;

public class Result<T> {
    private int code;
    private String message;
    private T data;

    // 成功的静态方法
    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.code = 200;
        r.message = "操作成功";
        r.data = data;
        return r;
    }

    // 失败的静态方法
    public static <T> Result<T> error(int code, String message) {
        Result<T> r = new Result<>();
        r.code = code;
        r.message = message;
        return r;
    }

    public static <T> Result<T> error(int code, String message, T data) {
        Result<T> r = new Result<>();
        r.code = code;
        r.message = message;
        r.data = data;
        return r;
    }

    // Getter（Spring 把对象转 JSON 时，必须要有 getter）
    public int getCode() { return code; }
    public String getMessage() { return message; }
    public T getData() { return data; }
}
