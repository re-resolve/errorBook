package com.example.errorBook.common.lang;

import lombok.Data;

import java.io.Serializable;

@Data
public class Res implements Serializable {
    
    private int code; // 200是正常，非200表示异常
    private String msg;
    private Object data;
    
    public static Res succ(Object data) {
        return succ(200, "操作成功", data);
    }
    
    public static Res succ(int code, String msg, Object data) {
        Res r = new Res();
        r.setCode(code);
        r.setMsg(msg);
        r.setData(data);
        return r;
    }
    
    public static Res fail(String msg) {
        return fail(400, msg, null);
    }
    
    public static Res fail(String msg, Object data) {
        return fail(400, msg, data);
    }
    
    public static Res fail(int code, String msg, Object data) {
        Res r = new Res();
        r.setCode(code);
        r.setMsg(msg);
        r.setData(data);
        return r;
    }
    
}