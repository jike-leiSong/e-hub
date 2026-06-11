package cn.sl.ehub.common.enums.guangzhou;

/**
 * @Description: 广州电网实时监测调控类型
 * @Author sl
 * @Date 2026-05-28
 */
public enum AdjustTypeGuangZhou {

    UP_VALUE_30_MIN("upValue30min", "持续30分钟上调节能力"),
    DOWN_VALUE_30_MIN("downValue30min", "持续30分钟下调节能力"),

    UP_VALUE_60_MIN("upValue60min", "持续1小时上调节能力"),
    DOWN_VALUE_60_MIN("downValue60min", "持续1小时下调节能力"),

    UP_VALUE_120_MIN("upValue120min", "持续2小时上调节能力"),
    DOWN_VALUE_120_MIN("downValue120min", "持续2小时下调节能力");

    private final String code;
    private final String desc;

    AdjustTypeGuangZhou(String code, String desc) {
        this.desc = desc;
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public String getCode() {
        return code;
    }
}
