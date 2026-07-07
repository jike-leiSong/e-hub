package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.common.utils.DateUtils;
import cn.sl.ehub.console.service.IAggregatorDateHolidayService;
import cn.sl.ehub.service.mapper.AggregatorDateHolidayMapper;
import cn.sl.ehub.service.vo.AggregatorDateHoliday;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.weekend.Weekend;
import tk.mybatis.mapper.weekend.WeekendCriteria;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class AggregatorDateHolidayServiceImpl implements IAggregatorDateHolidayService {

    private final AggregatorDateHolidayMapper aggregatorDateHolidayMapper;

    @Override
    public List<String> getApplyDateList(String date, Boolean flag) {
        List<String> resultList = new ArrayList<>();
        String queryDate = StringUtils.defaultIfBlank(date, DateUtils.getDay());
        List<AggregatorDateHoliday> holidayList = getAggregatorDateHolidayList(queryDate);
        if (holidayList == null || holidayList.isEmpty()) {
            resultList.add(DateUtils.getAddDate(queryDate));
        } else {
            List<String> dateList = holidayList.stream()
                    .map(AggregatorDateHoliday::getDate)
                    .collect(Collectors.toList());
            appendHolidayRange(queryDate, dateList, resultList);
        }
        if (Boolean.TRUE.equals(flag) && !resultList.isEmpty()) {
            resultList.add(0, DateUtils.getAddDate(resultList.get(0), -1));
        }
        return resultList;
    }

    @Override
    public Boolean getApplyDateCheck(String date) {
        Weekend<AggregatorDateHoliday> weekend = Weekend.of(AggregatorDateHoliday.class);
        WeekendCriteria<AggregatorDateHoliday, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorDateHoliday::getDate, date);
        return aggregatorDateHolidayMapper.selectCountByExample(weekend) > 0;
    }

    @Override
    public Boolean getWeekend(String date) {
        Weekend<AggregatorDateHoliday> weekend = Weekend.of(AggregatorDateHoliday.class);
        WeekendCriteria<AggregatorDateHoliday, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorDateHoliday::getDate, date);
        List<AggregatorDateHoliday> list = aggregatorDateHolidayMapper.selectByExample(weekend);
        return !list.isEmpty() && list.get(0).getLegalHoliday() == null;
    }

    @Override
    public List<AggregatorDateHoliday> getDateList(String date) {
        Weekend<AggregatorDateHoliday> weekend = Weekend.of(AggregatorDateHoliday.class);
        WeekendCriteria<AggregatorDateHoliday, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorDateHoliday::getDate, date);
        return aggregatorDateHolidayMapper.selectByExample(weekend);
    }

    @Override
    public List<String> getLegalHoliday(String year, String legal) {
        Weekend<AggregatorDateHoliday> weekend = Weekend.of(AggregatorDateHoliday.class);
        WeekendCriteria<AggregatorDateHoliday, Object> criteria = weekend.weekendCriteria();
        criteria.andEqualTo(AggregatorDateHoliday::getYear, year);
        criteria.andEqualTo(AggregatorDateHoliday::getLegalHoliday, legal);
        return aggregatorDateHolidayMapper.selectByExample(weekend).stream()
                .map(AggregatorDateHoliday::getDate)
                .collect(Collectors.toList());
    }

    @Override
    public Boolean initHoliday() {
        LocalDate date = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 12, 31);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<AggregatorDateHoliday> holidayList = new ArrayList<>();
        while (!date.isAfter(end)) {
            AggregatorDateHoliday holiday = new AggregatorDateHoliday();
            holiday.setYear("2024");
            holiday.setDate(date.format(formatter));
            Integer legalHoliday = resolveHolidayCode(date);
            if (legalHoliday > 0) {
                holiday.setLegalHoliday(String.valueOf(legalHoliday));
                holidayList.add(holiday);
            } else if ((date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY)
                    && !isWorkDay(date)) {
                holidayList.add(holiday);
            }
            date = date.plusDays(1);
        }
        if (!holidayList.isEmpty()) {
            aggregatorDateHolidayMapper.batchInsert(holidayList);
        }
        return true;
    }

    private List<AggregatorDateHoliday> getAggregatorDateHolidayList(String date) {
        Weekend<AggregatorDateHoliday> weekendCheck = Weekend.of(AggregatorDateHoliday.class);
        WeekendCriteria<AggregatorDateHoliday, Object> checkCriteria = weekendCheck.weekendCriteria();
        checkCriteria.andIn(AggregatorDateHoliday::getDate,
                Arrays.asList(DateUtils.getAddDate(date, -1), date, DateUtils.getAddDate(date)));
        if (aggregatorDateHolidayMapper.selectCountByExample(weekendCheck) == 0) {
            return Collections.emptyList();
        }

        Weekend<AggregatorDateHoliday> weekend = Weekend.of(AggregatorDateHoliday.class);
        WeekendCriteria<AggregatorDateHoliday, Object> criteria = weekend.weekendCriteria();
        if (date.contains("12-31")) {
            criteria.andBetween(AggregatorDateHoliday::getYear,
                    DateUtils.getYear(date),
                    DateUtils.getYear(DateUtils.getAddDate(date)));
        } else {
            criteria.andEqualTo(AggregatorDateHoliday::getYear, DateUtils.getYear(date));
        }
        return aggregatorDateHolidayMapper.selectByExample(weekend);
    }

    private void appendHolidayRange(String date, List<String> holidayList, List<String> resultList) {
        addForwardHoliday(date, holidayList, resultList);
        addBackwardHoliday(date, holidayList, resultList);
    }

    private void addForwardHoliday(String date, List<String> holidayList, List<String> resultList) {
        String nextDate = DateUtils.getAddDate(date);
        resultList.add(nextDate);
        if (holidayList.contains(nextDate)) {
            addForwardHoliday(nextDate, holidayList, resultList);
        }
    }

    private void addBackwardHoliday(String date, List<String> holidayList, List<String> resultList) {
        if (!holidayList.contains(date)) {
            return;
        }
        resultList.add(0, date);
        addBackwardHoliday(DateUtils.getLastDate(date), holidayList, resultList);
    }

    private static boolean isWorkDay(LocalDate date) {
        List<String> workdayList = Arrays.asList(
                "02-04", "02-18", "04-07", "04-28", "05-11", "09-14", "09-29", "10-12");
        return workdayList.contains(date.format(DateTimeFormatter.ofPattern("MM-dd")));
    }

    private static int resolveHolidayCode(LocalDate date) {
        int month = date.getMonthValue();
        int day = date.getDayOfMonth();
        if (month == 1 && day == 1) {
            return 1;
        }
        if (month == 2 && day >= 10 && day <= 17) {
            return 2;
        }
        if (month == 4 && day >= 4 && day <= 6) {
            return 3;
        }
        if (month == 5 && day >= 1 && day <= 5) {
            return 4;
        }
        if (month == 6 && day >= 8 && day <= 10) {
            return 5;
        }
        if (month == 9 && day >= 15 && day <= 17) {
            return 6;
        }
        if (month == 10 && day >= 1 && day <= 7) {
            return 7;
        }
        return 0;
    }
}
