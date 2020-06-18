package me.kuku.yuq.pojo;

public class CommonResult<T> {
    private Integer code;
    private String msg;
    private T t;

    public CommonResult(Integer code, String msg, T t) {
        this.code = code;
        this.msg = msg;
        this.t = t;
    }

    public CommonResult() {
    }

    public CommonResult(Integer code, String msg){
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getT() {
        return t;
    }

    public void setT(T t) {
        this.t = t;
    }

    @Override
    public String toString() {
        return "CommonResult{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", t=" + t +
                '}';
    }
}
