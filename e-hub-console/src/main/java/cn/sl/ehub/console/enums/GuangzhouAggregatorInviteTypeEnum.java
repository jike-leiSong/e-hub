package cn.sl.ehub.console.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 邀约类型枚举
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Getter
@AllArgsConstructor
public enum GuangzhouAggregatorInviteTypeEnum {

    LAST("1", "日前", "日前邀约"),
    TODAY("2", "日内", "日内邀约"),
    REAL_TIME("3", "实时", "实时邀约");

    private String code;
    private String name;
    private String nameStr;

    public static String getNameStr(String code) {
        for (GuangzhouAggregatorInviteTypeEnum inviteTypeEnum : GuangzhouAggregatorInviteTypeEnum.values()) {
            if (inviteTypeEnum.code.equals(code)) {
                return inviteTypeEnum.getNameStr();
            }
        }
        return null;
    }
}
