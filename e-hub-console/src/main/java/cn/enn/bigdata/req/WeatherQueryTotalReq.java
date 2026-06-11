package cn.enn.bigdata.req;

import lombok.Data;

@Data
public class WeatherQueryTotalReq {
    private String stationId;
    private String startTime;
    private String endTime;
}
