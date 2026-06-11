package cn.sl.ehub.console.enums;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * 聚合商推送刷新枚举
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Getter
@AllArgsConstructor
public enum AggregatorRefreshEnum {

    APP_INDEX_APPLY_STATUS("load-aggregator-business-1", "APP调峰首页企业申报状态", "app"),
    APP_INDEX_DEVICE_START_STOP_PLAN("load-aggregator-business-2", "APP调峰首页企业设备启停计划", "app"),
    PC_INDEX_TOMORROW_CHART("load-aggregator-business-3", "PC首页明日曲线图", "pc"),
    PC_INDEX_APPLY_STATUS("load-aggregator-business-4", "PC首页申报状态", "pc");

    private String code;
    private String desc;
    private String type;

    /**
     * 查询数据
     *
     * @param type
     * @return
     */
    public static List<String> getCodeByType(String type) {
        List<String> codeList = Lists.newArrayList();
        if (StringUtils.isNotEmpty(type)) {
            for (AggregatorRefreshEnum aggregatorRefreshEnum : AggregatorRefreshEnum.values()) {
                if (aggregatorRefreshEnum.getType().equals(type)) {
                    codeList.add(aggregatorRefreshEnum.getCode());
                }
            }
        }
        return codeList;
    }
}
