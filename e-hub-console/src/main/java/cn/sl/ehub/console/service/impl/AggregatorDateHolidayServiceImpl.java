package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.console.service.IAggregatorDateHolidayService;
import cn.sl.ehub.service.vo.AggregatorDateHoliday;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 节假日ServiceImpl (空实现)
 *
 * @Author sl
 * @Date 2026-06-15
 */
@Slf4j
@Service
public class AggregatorDateHolidayServiceImpl implements IAggregatorDateHolidayService {

    @Override
    public List<String> getApplyDateList(String date, Boolean flag) {
        log.warn("getApplyDateList called with date: {}, flag: {} - empty implementation", date, flag);
        return new ArrayList<>();
    }

    @Override
    public Boolean getApplyDateCheck(String date) {
        log.warn("getApplyDateCheck called with date: {} - empty implementation", date);
        return false;
    }

    public Boolean getWeekend(String date) {
        log.warn("getWeekend called with date: {} - empty implementation", date);
        return false;
    }

    @Override
    public List<AggregatorDateHoliday> getDateList(String date) {
        log.warn("getDateList called with date: {} - empty implementation", date);
        return new ArrayList<>();
    }

    @Override
    public List<String> getLegalHoliday(String year, String legal) {
        log.warn("getLegalHoliday called with year: {}, legal: {} - empty implementation", year, legal);
        return new ArrayList<>();
    }

    @Override
    public Boolean initHoliday() {
        log.warn("initHoliday called - empty implementation");
        return false;
    }
}
