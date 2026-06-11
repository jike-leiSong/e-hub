package cn.sl.ehub.console.controller;

import cn.enn.bigdata.req.WeatherCityReq;
import cn.enn.bigdata.req.WeatherQueryReq;
import cn.enn.bigdata.req.WeatherQueryTotalReq;
import cn.enn.bigdata.resp.WeatherCityResp;
import cn.enn.bigdata.resp.WeatherDayResp;
import cn.enn.bigdata.resp.WeatherHourResp;
import cn.sl.ehub.console.service.WeatherService;
import cn.sl.ehub.common.utils.DateUtils;
import cn.sl.ehub.common.vo.ResultVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description: 天气查询接口
 * @Author sl
 * @Date 2026-05-28
 */
@Slf4j
@RestController
@RequestMapping("/weather")
@Api(tags = "天气查询接口")
public class WeatherController {

    @Resource
    private WeatherService weatherService;

    @PostMapping("/getCityInfo")
    @ApiOperation(value = "获取城市信息")
    public ResultVO<WeatherCityResp> getCityInfo(@RequestBody WeatherQueryReq weatherQueryReq) {
        return ResultVO.success(weatherService.getCityInfo(weatherQueryReq));
    }

    @PostMapping("/getHourWeather")
    @ApiOperation(value = "获取小时级气象信息")
    public ResultVO<List<WeatherHourResp>> getHourWeather(@RequestBody WeatherQueryReq weatherQueryReq) {
        return ResultVO.success(weatherService.getHourWeather(weatherQueryReq));
    }

    @PostMapping("/getDayWeather")
    @ApiOperation(value = "获取天级气象信息")
    public ResultVO<List<WeatherDayResp>> getDayWeather(@RequestBody WeatherQueryReq weatherQueryReq) {
        if (null == weatherQueryReq.getHourInterval()) {
            weatherQueryReq.setHourInterval(1);
        }
        weatherQueryReq.setStartTime(DateUtils.getAddDateTime(weatherQueryReq.getStartTime(), -1));
        weatherQueryReq.setEndTime(DateUtils.getAddDateTime(weatherQueryReq.getEndTime(), -1));
        return ResultVO.success(weatherService.getDayWeather(weatherQueryReq));
    }
}
