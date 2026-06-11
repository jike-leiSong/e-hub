package cn.sl.ehub.common.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * @Description: 定时短信发送时间类型
 * @Author sl
 * @Date 2026-05-28
 */
public enum SmsTimeTypeEnum {

    MM("MM", "上月"),
    YY("YY", "去年");

    private String code;
    private String desc;

    SmsTimeTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static SmsTimeTypeEnum getEnumByCode(String timeType){
        for (SmsTimeTypeEnum value : SmsTimeTypeEnum.values()) {
            if (StringUtils.equals(value.code,timeType)){
                return value;
            }
        }
        return null;
    }
}
