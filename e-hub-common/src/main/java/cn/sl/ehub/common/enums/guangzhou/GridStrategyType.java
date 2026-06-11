package cn.sl.ehub.common.enums.guangzhou;

/**
 * @Description:
 * @Author sl
 * @Date 2026-05-28
 */
public enum GridStrategyType {
    PICK_CUT("削峰"),
    VALLERY_FILL("填谷");


    private final String desc;

    GridStrategyType(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

}
