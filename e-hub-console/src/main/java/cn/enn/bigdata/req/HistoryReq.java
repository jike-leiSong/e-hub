package cn.enn.bigdata.req;

import lombok.Data;

import java.util.List;

@Data
public class HistoryReq {
    private String deviceId;
    private String metric;
    private String startTime;
    private String endTime;
    private String dataSource;
    private List<OpentsdbReq> listQueries;
}
