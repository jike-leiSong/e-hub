package cn.sl.ehub.console.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Description: 历史查询时间类聚
 * @Author sl
 * @Date 2026-05-28
 */
@Getter
@AllArgsConstructor
public enum HistoryQueryTimeEnum {
    YESTERDAY(0, "昨日"),
    CURRENT_MONTH(1, "当前月"),
    PREVIOUS_MONTH(2, "上个月");

    private Integer code;
    private String desc;
}
