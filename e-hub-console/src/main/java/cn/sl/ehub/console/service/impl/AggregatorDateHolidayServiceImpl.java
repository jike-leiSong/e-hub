package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.service.mapper.AggregatorDateHolidayMapper;
import cn.sl.ehub.console.service.IAggregatorDateHolidayService;
import cn.sl.ehub.common.utils.DateUtils;
import cn.sl.ehub.service.vo.AggregatorDateHoliday;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.weekend.Weekend;
import tk.mybatis.mapper.weekend.WeekendCriteria;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 节假日ServiceImpl
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@RequiredArgsConstructor
@Service
public class AggregatorDateHolidayServiceImpl implements IAggregatorDateHolidayService {

    private final AggregatorDateHolidayMapper aggregatorDateHolidayMapper;

    @Override
    public List<String> getApplyDateList(String date, Boolean flag) {
        List<String> resultList = Lists.newArrayList();
        if (StringUtils.isEmpty(date)) {
            date = DateUtils.getDay();
        }
        List<AggregatorDateHoliday> holidayList = getAggregatorDateHolidayList(date);
        if (null != holidayList && holidayList.size() > 0) {
            List<String> dateList = holidayList.stream().map(AggregatorDateHoliday::getDate).collect(Collectors.toList());
            getHolidayList(date, dateList, resultList);
        } else {
            resultList.add(DateUtils.getAddDate(date));
        }
        if (flag) {
            resultList.add(0, DateUtils.getAddDate(resultList.get(0), -1));
        }
        return resultList;
    }

    @Override
    public Boolean getApplyDateCheck(String date) {
        Weekend<AggregatorDateHoliday> weekendDate = Weekend.of(AggregatorDateHoliday.class);
        WeekendCriteria<AggregatorDateHoliday, Object> criteriaDate = weekendDate.weekendCriteria();
        criteriaDate.andEqualTo(AggregatorDateHoliday::getDate, date);
        int count = aggregatorDateHolidayMapper.selectCountByExample(weekendDate);
        if (count > 0) {
            return true;
        }
        return false;
    }

    public Boolean getWeekend(String date){
        Weekend<AggregatorDateHoliday> weekendDate = Weekend.of(AggregatorDateHoliday.class);
        WeekendCriteria<AggregatorDateHoliday, Object> criteriaDate = weekendDate.weekendCriteria();
        criteriaDate.andEqualTo(AggregatorDateHoliday::getDate, date);
        List<AggregatorDateHoliday> aggregatorDateHolidays = aggregatorDateHolidayMapper.selectByExample(weekendDate);
        int count = aggregatorDateHolidayMapper.selectCountByExample(weekendDate);
        if (count > 0 && aggregatorDateHolidays.get(0).getLegalHoliday()==null) {
            return true;
        }
        return false;
    }

    @Override
    public List<AggregatorDateHoliday> getDateList(String date) {
        Weekend<AggregatorDateHoliday> weekendDate = Weekend.of(AggregatorDateHoliday.class);
        WeekendCriteria<AggregatorDateHoliday, Object> criteriaDate = weekendDate.weekendCriteria();
        criteriaDate.andEqualTo(AggregatorDateHoliday::getDate, date);
        return aggregatorDateHolidayMapper.selectByExample(weekendDate);
    }

    @Override
    public List<String> getLegalHoliday(String year, String legal) {
        Weekend<AggregatorDateHoliday> weekendDate = Weekend.of(AggregatorDateHoliday.class);
        WeekendCriteria<AggregatorDateHoliday, Object> criteriaDate = weekendDate.weekendCriteria();
        criteriaDate.andEqualTo(AggregatorDateHoliday::getYear,year);
        criteriaDate.andEqualTo(AggregatorDateHoliday::getLegalHoliday,legal);
        List<AggregatorDateHoliday> aggregatorDateHolidays = aggregatorDateHolidayMapper.selectByExample(weekendDate);
        return aggregatorDateHolidays.stream().map(AggregatorDateHoliday::getDate).collect(Collectors.toList());
    }

    @Override
    public Boolean initHoliday() {
        LocalDate date = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024,12,31);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<AggregatorDateHoliday> holidayList = new ArrayList<>();
        while (!date.isEqual(end)){
            AggregatorDateHoliday aggregatorDateHoliday = new AggregatorDateHoliday();
            aggregatorDateHoliday.setYear("2024");
            aggregatorDateHoliday.setDate(date.format(formatter));
            Integer legalHoliday = isHoliday(date);
            //判断是否是假期
            if(legalHoliday > 0){
                aggregatorDateHoliday.setLegalHoliday(legalHoliday.toString());
                holidayList.add(aggregatorDateHoliday);
           //判断是否为周末
            }else if(date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY){
                //判断是否为周末补班的情况
                 if(!isWorkDay(date)){
                     holidayList.add(aggregatorDateHoliday);
                 }
            }
            date = date.plusDays(1);
        }
        aggregatorDateHolidayMapper.batchInsert(holidayList);

        return true;
    }


    private static  Boolean isWorkDay(LocalDate date){
        Boolean result = Boolean.FALSE;
        String[] days = {"02-04","02-18","04-07","04-28","05-11","09-14","09-29","10-12"};
        List<String> dayList = Arrays.asList(days);
        String format = date.format(DateTimeFormatter.ofPattern("MM-dd"));
        if(dayList.stream().anyMatch(a->a.equals(format))){
            result = Boolean.TRUE;
        }
        return result;

    }

    /**
     * @description 判断是否为节假日 元旦1、春节2、清明节3、劳动节4、端午节5、中秋节6、国庆节7
     * @param
     * @return
     * @author sl
     * @date 2026-05-28
     */
    private static Integer isHoliday(LocalDate date) {

        // 判断是否是法定节假日，根据具体规定实现逻辑
        // 这里仅为示例，假设元旦、春节、清明节、劳动节、端午节、中秋节、国庆节为假期
        Integer flag=0;

        int month = date.getMonthValue();
        int day = date.getDayOfMonth();

        if (month == 1 && day == 1) { // 元旦
            flag=1;
        }
        if (month == 2 && day >= 10 && day <= 17) { // 春节
            flag=2;
        }
        if (month == 4 && day >= 4 && day <= 6) { // 清明节
            flag=3;
        }
        if (month == 5 && day >= 1 && day <= 5) { // 劳动节
            flag=4;
        }
        if (month == 6 && day >= 8 && day <= 10) { // 端午节
            flag=5;
        }
        if (month == 9 && day >= 15 && day <= 17) { // 中秋节
            flag=6;
        }
        if (month == 10 && day >= 1 && day <= 7) { // 国庆节
            flag=7;
        }

        return flag;
    }




    /**
     * 查询节假日
     *
     * @param date
     * @return
     */
    private List<AggregatorDateHoliday> getAggregatorDateHolidayList(String date) {
        Weekend<AggregatorDateHoliday> weekendDate = Weekend.of(AggregatorDateHoliday.class);
        WeekendCriteria<AggregatorDateHoliday, Object> criteriaDate = weekendDate.weekendCriteria();
        criteriaDate.andIn(AggregatorDateHoliday::getDate, Arrays.asList(DateUtils.getAddDate(date, -1), date, DateUtils.getAddDate(date)));
        int count = aggregatorDateHolidayMapper.selectCountByExample(weekendDate);
        if (count == 0) {
            return null;
        }
        Weekend<AggregatorDateHoliday> weekend = Weekend.of(AggregatorDateHoliday.class);
        WeekendCriteria<AggregatorDateHoliday, Object> criteria = weekend.weekendCriteria();
        if (date.contains("12-31")) {
            criteria.andBetween(AggregatorDateHoliday::getYear, DateUtils.getYear(date), DateUtils.getYear(DateUtils.getAddDate(date)));
        } else {
            criteria.andEqualTo(AggregatorDateHoliday::getYear, DateUtils.getYear(date));
        }
        return aggregatorDateHolidayMapper.selectByExample(weekend);
    }

    /**
     * 查询节假日
     *
     * @param date
     * @param holidayList
     * @param resultList
     * @return
     */
    private List<String> getHolidayList(String date, List<String> holidayList, List<String> resultList) {
        getAddHolidayList(date, holidayList, resultList);
        getSubHolidayList(date, holidayList, resultList);
        return resultList;
    }

    /**
     * 查询节假日
     *
     * @param date
     * @param holidayList
     * @param resultList
     * @return
     */
    private List<String> getAddHolidayList(String date, List<String> holidayList, List<String> resultList) {
        String nextDate = DateUtils.getAddDate(date);
        resultList.add(nextDate);
        if (!holidayList.contains(nextDate)) {
            return resultList;
        }
        return getAddHolidayList(nextDate, holidayList, resultList);
    }

    /**
     * 查询节假日
     *
     * @param date
     * @param holidayList
     * @param resultList
     * @return
     */
    private List<String> getSubHolidayList(String date, List<String> holidayList, List<String> resultList) {
        if (holidayList.contains(date)) {
            resultList.add(0, date);
            return getSubHolidayList(DateUtils.getLastDate(date), holidayList, resultList);
        }
        return resultList;
    }
}
