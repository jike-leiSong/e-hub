package cn.sl.ehub.common.enums;

/**
 * @Description:
 * @Author sl
 * @Date 2026-05-28
 */
public enum StatusCode {
    ERROR(500, "服务器错误，请联系管理员！"),
    SUCCESS(200, "请求成功!"),
    DATA_EMPTY(204, "返回数据为空!"),
    CIM_SUCCESS(0, "请求成功!"),

    CIM_EMPTY(2004, "请求数据为空!"),
    CIM_STATION(2005, "获取CIM站点信息失败！"),

    UAC_ENT(3001, "获取uac获取企业失败！"),
    UAC_SMS(3002, "短信发送失败！"),
    UAC_SMS_NO_PHONE(3002, "手机号未配置！"),

    F_A(4001, "模板信息处理失败!"),
    F_URL_UNAVAILABLE(4002, "华北URL建立连接失败!"),
    F_NO_GROUP(4003, "无此资源类型!"),
    F_CODE_NOT_EXIST(4004, "三方编码不存在"),

    BIG_DATA_WEATHER_CITY(5001, "获取大数据城市信息失败"),
    BIG_DATA_WEATHER_HOUR(5002, "获取小时级天气信息失败"),
    BIG_DATA_WEATHER_DAY(5003, "获取天级天气信息失败"),

    SMS_TOKEN_ERROR(6001, "获取SMS token失败"),

    ISSUE_SUCCESS(7000, "下发成功!"),
    ISSUE_ERROR(7001, "下发失败!"),

    IOT_SUCCESS(8000, "下发成功!"),
    IOT_ERROR(8001, "下发失败!"),

    TS_SUCCESS(9000, "成功!"),
    TS_ERROR(9001, "失败!"),

    TARIFF_NO_DATA(12001, "未查询到电价数据"),
    TARIFF_VERSION_NOT_FOUND(12002, "电价版本不存在"),
    TARIFF_IMPORT_VALIDATE_FAILED(12003, "电价导入校验失败"),

    A(1000, "数据库错误!"),
    B(1001, "服务器错误!"),
    C(1002, "参数错误!"),
    D(1003, "token已失效，请重新获取！！"),
    E(1004, "ticket不存在或者已失效!!"),
    F(1005, "应用id或者密码错误!!"),
    G(1006, "用户名或密码错误!!"),
    H(1007, "图形验证码错误!!"),
    I(1008, "短信验证码错误!!"),
    J(1009, "用户手机号已注册！！"),
    K(1010, "用户手机号码不存在！！"),
    L(1011, "用户未登录！！"),
    M(1012, "密码错误！！"),
    N(1013, "OPENID错误！！"),
    O(1014, "邮箱重复！！"),
    P(1015, "手机号重复！！"),
    Q(1016, "登录名重复！！"),
    R(1017, "手机号码不正确！！"),
    S(1018, "邮箱格式错误！！"),
    T(1019, "用户名不存在!!"),
    U(1020, "没有权限!!"),
    V(1021, "密码输入错误次数过多，请15分钟后重试！"),
    W(1023, "用户状态异常！！"),
    X(1024, "用户已被锁定！！"),

    E_A(2000, "请求大数据平台发生错误---数据异常！！"),
    E_B(2001, "请求数据为空！！"),
    E_C(2002, "请求外部服务错误！！"),
    E_D(2003, "企业信息不存在！！"),
    E_E(2004, "主账号不能登录！！"),
    E_F(2005, "用户企业信息不完整或者不存在！！"),
    E_G(2006, "大数据平台服务发生错误---服务异常！！"),
    E_H(2007, "请联系平台管理员开通权限！！！"),
    E_I(2008, "请联系售电公司管理员补充档案！！！"),
    E_J(2009, "数据不存在！！！"),
    E_K(2010, "数据处理超时！！");


    private final Integer code;
    private final String msg;

    StatusCode(Integer code, String msg) {
        this.msg = msg;
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public Integer getCode() {
        return code;
    }
}
