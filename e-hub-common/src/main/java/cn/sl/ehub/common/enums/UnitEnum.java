package cn.sl.ehub.common.enums;

/**
 * @Description: 常用单位枚举类
 * @Author sl
 * @Date 2026-05-28
 */
public enum UnitEnum {

    KW("kW", "千瓦"),
    MW("mW", "兆瓦"),
    YUAN("元", "元"),
    WAN_YUAN("万元", "元"),
    PERCENT("%", "百分比");

    private String code;

    private String desc;

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    UnitEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
