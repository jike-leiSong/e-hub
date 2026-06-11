package cn.sl.ehub.console.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 聚合商功率达标计算标准枚举
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Getter
@AllArgsConstructor
public enum AggregatorPowerRemindEnum {

    MIN(85D, 0.85, "最小值"),
    MAX(120D, 1.2, "最大值");

    private Double rate;
    private Double percent;
    private String desc;
}
