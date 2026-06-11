package cn.sl.ehub.console.enums;

import cn.sl.ehub.common.utils.DateUtils;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * 聚合商收益时间枚举
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Getter
@AllArgsConstructor
public enum AggregatorProfitTimeEnum {

    ONE("00:00:00", "07:00:00", "收益时间段"),
    TWO("12:00:00", "16:00:00", "收益时间段");

    private String startTime;
    private String endTime;
    private String desc;

    /**
     * 查询收益时间段
     *
     * @return
     */
    public static List<String> getMinuteList() {
        List<String> minuteList = Lists.newArrayList();
        String date = DateUtils.getDay();
        for (AggregatorProfitTimeEnum aggregatorProfitTimeEnum : AggregatorProfitTimeEnum.values()) {
            List<String> minuteListWithEnum = DateUtils.getMinuteList(date + " " + aggregatorProfitTimeEnum.getStartTime(), date + " " + aggregatorProfitTimeEnum.getEndTime());
            if (null != minuteListWithEnum && minuteListWithEnum.size() > 0) {
                minuteListWithEnum.forEach(minute -> {
                    minuteList.add(DateUtils.format(minute, "HH:mm:ss"));
                });
            }
        }
        return minuteList;
    }
}
