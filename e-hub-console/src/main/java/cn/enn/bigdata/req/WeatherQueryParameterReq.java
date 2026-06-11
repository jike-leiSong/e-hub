package cn.enn.bigdata.req;

import lombok.Data;

@Data
public class WeatherQueryParameterReq {
    private String stationId;
    private String startTime;
    private String endTime;
    private String parameter;
}
