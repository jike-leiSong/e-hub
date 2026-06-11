package cn.sl.ehub.console.service;
import cn.enn.bigdata.req.WeatherCityReq;
import cn.enn.bigdata.req.WeatherQueryReq;
import cn.enn.bigdata.req.WeatherQueryTotalReq;
import cn.enn.bigdata.resp.WeatherCityResp;
import cn.enn.bigdata.resp.WeatherDayResp;
import cn.enn.bigdata.resp.WeatherHourResp;

import java.util.List;

/**
 * @Description:
 * @Author sl
 * @Date 2026-05-28
 */
public interface WeatherService {

    WeatherCityResp getCityInfo(WeatherQueryReq weatherQueryReq);

    List<WeatherHourResp> getHourWeather(WeatherQueryReq weatherQueryReq);

    List<WeatherDayResp> getDayWeather(WeatherQueryReq weatherQueryReq);

    String getCityInfoByStationId(String stationId,String seperator);

}
