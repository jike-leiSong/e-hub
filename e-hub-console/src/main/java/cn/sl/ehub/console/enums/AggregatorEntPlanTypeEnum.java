package cn.sl.ehub.console.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 聚合商企业用户申报计划类型
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Getter
@AllArgsConstructor
public enum AggregatorEntPlanTypeEnum {

    ALL("0", "全部计划不包含默认"),
    DEFAULT("1", "默认计划"),
    FINISH("2", "已完成计划"),
    NOW("3", "当前计划"),
    NOSTART("4", "未开始计划"),
    UPDATE("5", "已更新计划"),
    ;

    private String code;
    private String desc;
}
