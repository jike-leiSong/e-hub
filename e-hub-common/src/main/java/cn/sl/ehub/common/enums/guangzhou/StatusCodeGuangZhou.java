package cn.sl.ehub.common.enums.guangzhou;

/**
 * @Description:
 * @Author sl
 * @Date 2026-05-28
 */
public enum StatusCodeGuangZhou {

    RECEIVE_FAIL("1", "RECEIVE FAIL"),
    RECEIVE_SUCCESS("0", "RECEIVE SUCCESS");

    private final String code;
    private final String msg;

    StatusCodeGuangZhou(String code, String msg) {
        this.msg = msg;
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public String getCode() {
        return code;
    }
}
