package pers.ysc.common.lang;

import lombok.Data;

/**
 * @Date:2021/1/31
 * @describe:
 * @author:ysc
 */
@Data
public class Result {
    private int code; // 200是正常，非200表示异常
    private String msg;//消息
    private Object data;//数据

    public static Result success(Object data) {
        return success(200, "操作成功", data);
    }

    public static Result success(int code, String msg, Object data) {
        Result result = new Result();
        result.setCode(code);
        result.setData(data);
        result.setMsg(msg);
        return result;
    }

    //失败 无数据
    public static Result fail(String msg) {
        return fail(400, msg, null);
    }

    //有数据
    public static Result fail(String msg, Object data) {
        return fail(400, msg, data);
    }

    public static Result fail(int code, String msg, Object data) {
        Result r = new Result();
        r.setCode(code);
        r.setMsg(msg);
        r.setData(data);
        return r;
    }
}
