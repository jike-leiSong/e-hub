package cn.enn.bigdata.req;

import lombok.Data;

import java.util.List;

@Data
public class RealTimeReq {
    private String deviceId;
    private String metric;
    private Integer days;
    private Boolean isClean;
    private String dataSource;
    private List<OpentsdbReq> listQueries;
}
