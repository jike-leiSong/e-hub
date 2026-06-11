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
public enum ApplyPlanStatusContentEnum {

    FINAL_APPLY_TODAY("当天完成申报", "%s已完成申报", "", "%s 申报计划"),
    FINAL_APPLY_NO_TODAY("非当天完成申报", "明日计划已提交", "因逢节假日，明日计划已于%s完成提交；", "%s 申报计划"),
    NO_FINAL_APPLY_TODAY("当天未完成申报", "今日申报已结束", "", "%s 申报计划"),
    NO_FINAL_APPLY_NO_TODAY("非当天未完成申报", "明日计划未提交", "因逢节假日，明日计划已于%s结束申报；", "%s 申报计划");

    private String desc;
    private String title;
    private String content;
    private String applyPlan;
}
