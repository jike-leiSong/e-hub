package cn.sl.ehub.console.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 聚合商收益类型枚举
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Getter
@AllArgsConstructor
public enum AggregatorProfitTypeEnum {

    ISSUE("issueProfit", "电网调度下发金额"),
    AGGREGATOR("aggregatorProfit", "负荷聚合商收益"),
    ENT("entProfit", "用户总收益");

    private String code;
    private String desc;
}
