package cn.sl.ehub.console.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 申报状态枚举
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Getter
@AllArgsConstructor
public enum ApplyStatusEnum {

    APPLY_NO("0", "未申报"),
    INVITE("1", "邀约"),
    APPLY_YES("2", "已申报"),
    APPLYING("1", "申报中"),
    NO_ALLOW_APPLY("3", "未到申报时间，不允许申报"),
    ;

    private String code;
    private String desc;
}
