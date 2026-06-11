package cn.sl.ehub.common.enums.guangzhou.jintian;

import org.apache.commons.lang3.math.NumberUtils;

/**
 * @Description: 广州劲天接口状态枚举
 * @Author sl
 * @Date 2026-05-28
 */
public enum StatusCodeGuangZhouJinTian {

    SUCCESS(200, "请求成功"),
    ERROR(500, "系统错误"),
    SIGN_ERROR(4001, "签名错误"),
    TOKEN_ERROR(4002, "Token 错误"),
    POST_PARAMS_INVALIDE(4003, "POST 参数不合法"),
    BIZ_PARAMS_INVALIDE(4004, "请求的业务参数不合法"),
    API_REQUEST_FORBID(4005, "API 禁止访问"),
    NO_COOPERATION(4006, "找不到合作方信息");

    private final Integer code;
    private final String msg;

    StatusCodeGuangZhouJinTian(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public Integer getCode() {
        return code;
    }

    public static StatusCodeGuangZhouJinTian getByCode(Integer code) {
        StatusCodeGuangZhouJinTian[] values = StatusCodeGuangZhouJinTian.values();
        for (StatusCodeGuangZhouJinTian value : values) {
            if (NumberUtils.compare(code, value.code) == 0) {
                return value;
            }
        }
        return null;
    }
}
