package cn.sl.ehub.common.enums.guangzhou;

/**
 * @Description:
 * @Author sl
 * @Date 2026-05-28
 */
public enum InviteTypeGuangZhou {

    ONE_DAY_BEFORE("oneDayBefore", "日前", "1"),
    FOUR_HOURS_BEFORE("fourHoursBefore", "日内", "2"),
    REAL_TIME("realTime", "实时", "3");


    private final String code;
    private final String desc;
    private final String value;

    InviteTypeGuangZhou(String code, String desc, String value) {
        this.desc = desc;
        this.code = code;
        this.value = value;
    }

    public String getDesc() {
        return desc;
    }

    public String getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }

    public static String getValue(String code) {
        for (InviteTypeGuangZhou inviteTypeGuangZhou : InviteTypeGuangZhou.values()) {
            if (inviteTypeGuangZhou.code.equals(code)) {
                return inviteTypeGuangZhou.getValue();
            }
        }
        return null;
    }
}
