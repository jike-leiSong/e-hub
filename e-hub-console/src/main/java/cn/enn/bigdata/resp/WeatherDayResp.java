package cn.enn.bigdata.resp;

import lombok.Data;

@Data
public class WeatherDayResp {
    private String date;
    private Double maxTemperature;
    private Double minTemperature;
    private String weather;
}
