package cn.enn.bigdata.resp;

import lombok.Data;

@Data
public class WeatherHourResp {
    private String time;
    private Double temperature;
    private Double humidity;
    private Double windSpeed;
}
