package cn.sl.ehub.console.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 推送枚举
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Getter
@AllArgsConstructor
public enum LoadAggregatorBusinessWebSocketContentEnum {

    LAST_DAY_INVITE("lastDayInvite", "日前邀约"),
    TODAY_INVITE("todayInvite", "日内邀约"),
    REALLY_INVITE("reallyInvite", "实时邀约"),
    INVITE_NUM("inviteNum", "邀约数量"),
    INVITE_NEW_RECORD("inviteNewRecord", "邀约新记录"),
    INVITE_AGGREGATOR("inviteAggregator", "邀约企业申报/聚合商申报/执行方案下发"),
    INVITE_CANCEL_NUM("inviteCancelNum", "取消邀约数量"),
    POLICE_NEW_RECORD("policeNewRecord", "新告警"),
    REMIND_NEW_RECORD("remindNewRecord", "新户号功率未达标提醒"),
    ;

    private String code;
    private String desc;
}
