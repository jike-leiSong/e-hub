package cn.sl.ehub.common.exception;

public class ParamException extends RuntimeException {
    public ParamException() {
        super("参数错误");
    }

    public ParamException(String message) {
        super(message);
    }
}
