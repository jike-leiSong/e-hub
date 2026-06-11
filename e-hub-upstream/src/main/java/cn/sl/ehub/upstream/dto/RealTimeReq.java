package cn.sl.ehub.upstream.dto;

import lombok.Data;
import java.util.List;

/**
 * BigData实时数据请求（临时替代类）
 * 原：cn.enn.bigdata.req.RealTimeReq
 */
@Data
public class RealTimeReq {

    private String deviceId;
    private List<String> metrics;
    private String startTime;
    private String endTime;
    private List<OpentsdbReq> listQueries;
    private Integer days;
    private Boolean isClean;
    private String dataSource;
}
