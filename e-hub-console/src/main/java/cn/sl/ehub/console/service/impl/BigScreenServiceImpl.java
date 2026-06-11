package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.service.mapper.*;
import cn.sl.ehub.service.resp.BigScreenWeekDayAverageChartResp;
import cn.sl.ehub.console.service.IBigScreenService;
import cn.sl.ehub.common.utils.DateUtils;
import cn.sl.ehub.service.vo.*;
import com.alibaba.fastjson.JSONArray;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.weekend.Weekend;
import tk.mybatis.mapper.weekend.WeekendCriteria;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 大屏接口ServiceImpl
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@RequiredArgsConstructor
@Service
public class BigScreenServiceImpl implements IBigScreenService {

    private final BigScreenGeneralSituationMapper bigScreenGeneralSituationMapper;
    private final BigScreenWeekDateMapper bigScreenWeekDateMapper;
    private final BigScreenEntProfitMapper bigScreenEntProfitMapper;
    private final BigScreenWeekDayAverageChartMapper bigScreenWeekDayAverageChartMapper;
    private final BigScreenDayLogMapper bigScreenDayLogMapper;
    private final BigScreenEntTodayRateMapper bigScreenEntTodayRateMapper;

    @Override
    public BigScreenGeneralSituation getAll() {
        return bigScreenGeneralSituationMapper.getBigScreenGeneralSituation();
    }

    @Override
    public List<BigScreenWeekDate> getBigScreenWeekDateList() {
        String endDate = DateUtils.getLastDay();
        String startDate = DateUtils.getAddDate(endDate, -6);
        List<String> dayList = DateUtils.getDayList(startDate, endDate);
        List<BigScreenWeekDate> bigScreenWeekDateList = bigScreenWeekDateMapper.selectAll();
        bigScreenWeekDateList.forEach(resp -> {
            dayList.forEach(day -> {
                Long days = DateUtils.getDays(resp.getDate(), day);
                if (days % 7 == 0) {
                    resp.setDate(day);
                }
            });
        });
        return bigScreenWeekDateList.stream().sorted(Comparator.comparing(BigScreenWeekDate::getDate)).collect(Collectors.toList());
    }

    @Override
    public List<BigScreenEntProfit> getBigScreenEntProfitList() {
        Weekend<BigScreenEntProfit> weekend = Weekend.of(BigScreenEntProfit.class);
        weekend.orderBy("profit").desc();
        return bigScreenEntProfitMapper.selectByExample(weekend);
    }

    @Override
    public BigScreenWeekDayAverageChartResp getBigScreenWeekDayAverageChart() {
        BigScreenWeekDayAverageChartResp bigScreenWeekDayAverageChartResp = new BigScreenWeekDayAverageChartResp();
        BigScreenWeekDayAverageChart bigScreenWeekDayAverageChart = bigScreenWeekDayAverageChartMapper.getBigScreenWeekDayAverageChart();
        bigScreenWeekDayAverageChartResp.setOfferList(JSONArray.parseArray(bigScreenWeekDayAverageChart.getOffer(), DataResp.class));
        bigScreenWeekDayAverageChartResp.setPowerList(JSONArray.parseArray(bigScreenWeekDayAverageChart.getPower(), DataResp.class));
        return bigScreenWeekDayAverageChartResp;
    }

    @Override
    public List<BigScreenDayLog> getDayLog() {
        Weekend<BigScreenDayLog> weekend = Weekend.of(BigScreenDayLog.class);
        WeekendCriteria<BigScreenDayLog, Object> criteria = weekend.weekendCriteria();
        criteria.andLessThanOrEqualTo(BigScreenDayLog::getTime, DateUtils.format(DateUtils.getTime(), "HH:mm:ss"));
        weekend.orderBy("time").desc();
        return bigScreenDayLogMapper.selectByExample(weekend);
    }

    @Override
    public List<BigScreenEntTodayRate> getBigScreenTodayRate() {
        String now = "2021-03-07 " + DateUtils.format(DateUtils.getTime(), "HH:mm:ss");
        List<BigScreenEntTodayRate> bigScreenEntTodayRateList = bigScreenEntTodayRateMapper.getBigScreenEntTodayRateList(now);
        return bigScreenEntTodayRateList.stream().sorted(Comparator.comparing(BigScreenEntTodayRate::getFinishRate).reversed()).collect(Collectors.toList());
    }
}
