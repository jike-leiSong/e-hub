package cn.sl.ehub.common.exception;

/**
 * @Description: 通用异常
 * @Author sl
 * @Date 2026-05-28
 */
public class BaseException extends RuntimeException{

    private Integer code;
    private String msg;

    public BaseException(Integer code, String msg) {
        super(msg);
        this.msg = msg;
        this.code = code;
    }

    public BaseException(Integer code, String msg, Throwable cause) {
        super(msg, cause);
        this.msg = msg;
        this.code = code;
    }

    public Integer getCode() {
        return this.code;
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
}
