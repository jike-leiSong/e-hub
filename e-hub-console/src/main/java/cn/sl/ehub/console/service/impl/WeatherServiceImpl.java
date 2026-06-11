package cn.sl.ehub.console.service.impl;

import cn.enn.bigdata.enums.StrategyEnum;
import cn.enn.bigdata.req.WeatherCityReq;
import cn.enn.bigdata.req.WeatherQueryParameterReq;
import cn.enn.bigdata.req.WeatherQueryReq;
import cn.enn.bigdata.req.WeatherQueryTotalReq;
import cn.enn.bigdata.resp.*;
import cn.enn.bigdata.service.BigDataServiceContext;
import cn.enn.cim.resp.SystemBaseInfo;
import cn.enn.cim.service.CimBaseService;
import cn.sl.ehub.common.enums.StatusCode;
import cn.sl.ehub.common.exception.BaseException;
import cn.sl.ehub.common.vo.DataResp;
import cn.sl.ehub.console.service.WeatherService;
import cn.sl.ehub.common.utils.MapGlobalUtil;
import cn.sl.ehub.common.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description:
 * @Author sl
 * @Date 2026-05-28
 */
@Slf4j
@Service
public class WeatherServiceImpl implements WeatherService {

    @Resource
    private CimBaseService cimBaseService;

    @Resource
    private BigDataServiceContext bigDataServiceContext;

    @Override
    public WeatherCityResp getCityInfo(WeatherQueryReq weatherQueryReq) {

        String simulate = MapGlobalUtil.getMapObj("simulate").toString();
        String location = getCityInfoByStationId(weatherQueryReq.getStationId(), "-");

        if (StringUtils.isBlank(location)) {
            throw new BaseException(500, "获取城市出错");
        }
        String[] info = location.split("-");
        String provinceName = info[0];
        String cityName = info[1];

        WeatherCityReq req = new WeatherCityReq();
        req.setCityName(cityName);
        req.setProvinceName(provinceName);
        req.setIsPrediction(null);
        req.setPageNum("1");
        req.setPageSize("1");

        return bigDataServiceContext.getCityInfo(req, StrategyEnum.getServiceName(simulate));
    }

    @Override
    public List<WeatherHourResp> getHourWeather(WeatherQueryReq weatherQueryReq) {

        String simulate = MapGlobalUtil.getMapObj("simulate").toString();

        WeatherCityResp cityInfo = getCityInfo(weatherQueryReq);
        WeatherQueryTotalReq req = new WeatherQueryTotalReq();
        List<WeatherQueryParameterReq> params = new ArrayList<>();
        WeatherQueryParameterReq weatherQueryParameterReq = new WeatherQueryParameterReq();
        weatherQueryParameterReq.setCityId(cityInfo.getCityId());
        weatherQueryParameterReq.setStartTime(weatherQueryReq.getStartTime());
        weatherQueryParameterReq.setEndTime(weatherQueryReq.getEndTime());
        weatherQueryParameterReq.setHourInterval(weatherQueryReq.getHourInterval());
        params.add(weatherQueryParameterReq);
        req.setParams(params);

        return bigDataServiceContext.getHourWeather(req, StrategyEnum.getServiceName(simulate));
    }

    @Override
    public List<WeatherDayResp> getDayWeather(WeatherQueryReq weatherQueryReq) {

        String simulate = MapGlobalUtil.getMapObj("simulate").toString();

        WeatherCityResp cityInfo = getCityInfo(weatherQueryReq);
        WeatherQueryTotalReq req = new WeatherQueryTotalReq();
        List<WeatherQueryParameterReq> params = new ArrayList<>();
        WeatherQueryParameterReq weatherQueryParameterReq = new WeatherQueryParameterReq();
        weatherQueryParameterReq.setCityId(cityInfo.getCityId());
        weatherQueryParameterReq.setStartTime(weatherQueryReq.getStartTime());
        weatherQueryParameterReq.setEndTime(weatherQueryReq.getEndTime());
        weatherQueryParameterReq.setHourInterval(weatherQueryReq.getHourInterval());
        params.add(weatherQueryParameterReq);
        req.setParams(params);

        return bigDataServiceContext.getDayWeather(req, StrategyEnum.getServiceName(simulate));

    }

    @Override
    public String getCityInfoByStationId(String stationId, String seperator) {
        ResultVO<SystemBaseInfo> response = cimBaseService.querySystemBaseInfo(stationId);
        if (response.getCode().equals(StatusCode.SUCCESS.getCode())) {
            String privinceName = null != response.getData() ? response.getData().getProvince() : "";
            String cityName = null != response.getData() ? response.getData().getCity() : "";

            if (StringUtils.equals("市辖区",cityName)){
                cityName = privinceName;
            }

            if (StringUtils.isNotBlank(privinceName) && privinceName.endsWith("省")) {
                privinceName = privinceName.replace("省", "");
            }

            if (StringUtils.isNotBlank(privinceName) && privinceName.endsWith("市")) {
                privinceName = privinceName.replace("市", "");
            }

            if (StringUtils.isNotBlank(cityName) && cityName.endsWith("市")) {
                cityName = cityName.replace("市", "");
            }

            if (StringUtils.isBlank(seperator)) {
                if (privinceName.equals(cityName)) {
                    return privinceName;
                } else {
                    return privinceName + cityName;
                }
            } else {
                return privinceName + seperator + cityName;
            }

        }
        return "";
    }
}
