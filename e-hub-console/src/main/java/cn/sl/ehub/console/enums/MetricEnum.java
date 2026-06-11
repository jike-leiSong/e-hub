package cn.sl.ehub.console.enums;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * 测点枚举
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Getter
@AllArgsConstructor
public enum MetricEnum {

    YES_POWER("P", "kW", "有功功率", true, "1", "有功功率"),
    NO_POWER("Q", "kW", "无功功率", true, "1", "无功功率"),
    IA("Ia", "A", "A相电流", false, "2", "用电电流"),
    IB("Ib", "A", "B相电流", false, "2", "用电电流"),
    IC("Ic", "A", "C相电流", false, "2", "用电电流"),
    USE_ELECTRIC("use_electric", "A", "用电电流", true, "3", "用电电流"),
    //    ZERO_POINT_ELECTRIC_QUANTITY("Eelec", "kWh", "当日零点电量", true, "4", "当日零点电量"),
    ZERO_POINT_ELECTRIC_QUANTITY("Eptp", "kWh", "当日零点电量", true, "4", "当日零点电量");

    private String code;
    private String unit;
    private String desc;
    private Boolean flag;
    private String groupCode;
    private String groupName;

    public static MetricEnum getMetricEnum(String code) {
        if (StringUtils.isNotEmpty(code)) {
            for (MetricEnum metric : MetricEnum.values()) {
                if (metric.getCode().equals(code)) {
                    return metric;
                }
            }
        }
        return null;
    }

    public static List<MetricEnum> getMetricEnumByFlag(Boolean flag) {
        List<MetricEnum> metricList = Lists.newArrayList();
        if (null != flag) {
            for (MetricEnum metric : MetricEnum.values()) {
                if (metric.getFlag().equals(flag)) {
                    metricList.add(metric);
                }
            }
        }
        return metricList;
    }
}
