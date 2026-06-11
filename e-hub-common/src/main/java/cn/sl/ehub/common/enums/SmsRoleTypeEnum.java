package cn.sl.ehub.common.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * @Description: 定时短信发送时间类型
 * @Author sl
 * @Date 2026-05-28
 */
public enum SmsRoleTypeEnum {

    ENT("0", "企业"),
    AGG("1", "聚合商");

    private String code;
    private String desc;

    SmsRoleTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static SmsRoleTypeEnum getEnumByCode(String timeType) {
        for (SmsRoleTypeEnum value : SmsRoleTypeEnum.values()) {
            if (StringUtils.equals(value.code, timeType)) {
                return value;
            }
        }
        return null;
    }
}
