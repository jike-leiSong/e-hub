package cn.sl.ehub.upstream.dto;

import lombok.Data;
import java.util.List;

/**
 * BigData历史数据请求（临时替代类）
 * 原：cn.enn.bigdata.req.HistoryReq
 */
@Data
public class HistoryReq {

    private String deviceId;
    private List<String> metrics;
    private String startTime;     // 改为String类型以匹配使用场景
    private String endTime;       // 改为String类型以匹配使用场景
    private String dataSource;
    private List<OpentsdbReq> listQueries;
}
