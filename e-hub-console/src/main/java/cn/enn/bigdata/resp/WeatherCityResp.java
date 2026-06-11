package cn.enn.bigdata.resp;

import lombok.Data;

@Data
public class WeatherCityResp {
    private String cityName;
    private String cityCode;
    private String province;
}
