package cn.enn.bigdata.req;

import lombok.Data;

@Data
public class WeatherQueryReq {
    private String stationId;
    private String startTime;
    private String endTime;
    private Integer hourInterval;
}
