package com.youzi.blue.net.common.utils;

import java.util.HashMap;

public class Result extends  HashMap{
    //禁止其他类用new创建
    private Result() { }

    public Result put(String key, Object value) {
        super.put(key, value);
        return this;
    }

    public static Result of(int code, String msg,Object data) {
        return new Result()
                .put("code", code)
                .put("msg", msg)
                .put("data", data);
    }

    public static Result of(int code, String msg) {
        return new Result()
                .put("code", code)
                .put("msg", msg);
    }

    public static Result of(int code) {
        return new Result().put("code", code);
    }

    public static Result of() {
        return new Result();
    }
}
