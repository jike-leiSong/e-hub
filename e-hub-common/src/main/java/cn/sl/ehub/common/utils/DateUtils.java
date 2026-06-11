package cn.sl.ehub.common.utils;

import cn.sl.ehub.common.vo.DataResp;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author sl
 * @Description 时间工具类
 */
public class DateUtils {

    public static final String BASIC_PATTEN = "yyyy-MM-dd HH:mm:00";

    /**
     * flag before
     */
    public static final transient int BEFORE = 1;

    /**
     * flag after
     */
    public static final transient int AFTER = 2;

    /**
     * flag equal
     */
    public static final transient int EQUAL = 3;

    /**
     * 获取YYYY格式
     *
     * @return
     */
    public static String getYear() {
        return formatDate(new Date(), "yyyy");
    }

    /**
     * 获取YYYY格式
     *
     * @return
     */
    public static String getYear(String date) {
        return date.substring(0, 4);
    }

    /**
     * 获取YYYY格式
     *
     * @return
     */
    public static String getYear(Date date) {
        return formatDate(date, "yyyy");
    }

    /**
     * 获取YYYY-MM格式
     *
     * @return
     */
    public static String getMonth() {
        return formatDate(new Date(), "yyyy-MM");
    }

    /**
     * 获取YYYY-MM格式
     *
     * @param date
     * @return
     */
    public static String getMonth(Date date) {
        return formatDate(date, "yyyy-MM");
    }

    /**
     * 获取YYYY-MM-DD格式
     *
     * @return
     */
    public static String getDay() {
        return formatDate(new Date(), "yyyy-MM-dd");
    }

    /**
     * 获取YYYY-MM-DD格式
     *
     * @return
     */
    public static String getDay(String date) {
        return date.substring(0, 10);
    }

    /**
     * 获取YYYY-MM-DD格式
     *
     * @return
     */
    public static String getDay(Date date) {
        return formatDate(date, "yyyy-MM-dd");
    }

    /**
     * 获取YYYYMMDD格式
     *
     * @return
     */
    public static String getDays() {
        return formatDate(new Date(), "yyyyMMdd");
    }

    /**
     * 获取YYYYMMDD格式
     *
     * @return
     */
    public static String getDays(Date date) {
        return formatDate(date, "yyyyMMdd");
    }

    /**
     * 获取YYYY-MM-DD HH:mm:ss格式
     *
     * @return
     */
    public static String getTime() {
        return formatDate(new Date(), "yyyy-MM-dd HH:mm:ss");
    }

    /**
     * 获取YYYY-MM-DD HH:mm:ss.SSS格式
     *
     * @return
     */
    public static String getMsTime() {
        return formatDate(new Date(), "yyyy-MM-dd HH:mm:ss.SSS");
    }

    /**
     * 获取YYYYMMDDHHmmss格式
     *
     * @return
     */
    public static String getAllTime() {
        return formatDate(new Date(), "yyyyMMddHHmmss");
    }

    /**
     * 获取YYYY-MM-DD HH:mm:ss格式
     *
     * @return
     */
    public static String getTime(Date date) {
        return formatDate(date, "yyyy-MM-dd HH:mm:ss");
    }

    public static String formatDate(Date date, String pattern) {
        String formatDate = null;
        if (StringUtils.isNotBlank(pattern)) {
            formatDate = DateFormatUtils.format(date, pattern);
        } else {
            formatDate = DateFormatUtils.format(date, "yyyy-MM-dd");
        }
        return formatDate;
    }

    /**
     * @param s
     * @param e
     * @return boolean
     * @throws
     * @Title: compareDate
     * @Description:(日期比较，如果s>=e 返回true 否则返回false)
     * @author sl
     */
    public static boolean compareDate(String s, String e) {
        if (parseDate(s) == null || parseDate(e) == null) {
            return false;
        }
        return parseDate(s).getTime() >= parseDate(e).getTime();
    }


    /**
     * @param startDate
     * @param endDate
     * @return boolean
     * @throws
     * @Title: compareDate
     * @Description:(日期比较，如果s>=e 返回true 否则返回false)
     * @author sl
     */
    public static boolean compareDateForGreater(String startDate, String endDate) {
        if (parseDate(startDate) == null || parseDate(endDate) == null) {
            return false;
        }
        return parseDate(startDate).getTime() > parseDate(endDate).getTime();
    }

    /**
     * (时间比较，如果s>=e 返回true 否则返回false)
     *
     * @param s
     * @param e
     * @return
     */
    public static boolean comparTime(String s, String e) {
        if (parseTime(s) == null || parseTime(e) == null) {
            return false;
        }
        return parseTime(s).getTime() >= parseTime(e).getTime();
    }

    public static boolean equalDate(String s, String e) {
        if (parseTime(s) == null || parseTime(e) == null) {
            return false;
        }
        return parseTime(s).getTime() == parseTime(e).getTime();
    }

    /**
     * 格式化日期
     *
     * @return
     */
    public static Date parseDate(String date) {
        return parse(date, "yyyy-MM-dd");
    }

    public static Date parseMinute(String date) {
        return parse(date, "MMdd HH:mm");
    }


    /**
     * 格式化日期
     *
     * @return
     */
    public static Date parseTime(String date) {
        return parse(date, "yyyy-MM-dd HH:mm:ss");
    }

    public static String parseTime2(String date) {
        String s = date.replaceAll("-", "/");
        return s;
    }

    public static Long stringToLong(String time) {
        if (StringUtils.isEmpty(time)) {
            return null;
        }
        Date date = parse(time, "yyyy-MM-dd HH:mm:ss");
        return date.getTime() / 1000;
    }

    /**
     * 查询两个日期相隔天数
     *
     * @param startDate
     * @param endDate
     * @return
     */
    public static Long getDays(String startDate, String endDate) {
        return getDaySub(startDate + " 00:00:00", endDate + " 00:00:00");
    }

    /**
     * 格式化日期
     *
     * @return
     */
    public static Date parse(String date, String pattern) {
        try {
            return org.apache.commons.lang3.time.DateUtils.parseDate(date, pattern);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 格式化日期
     *
     * @return
     */
    public static String format(Date date, String pattern) {
        return DateFormatUtils.format(date, pattern);
    }

    /**
     * 格式化日期
     *
     * @return
     */
    public static String format(String time, String pattern) {
        if (StringUtils.isEmpty(time)) {
            return null;
        }
        return DateFormatUtils.format(parseTime(time), pattern);
    }

    /**
     * 把日期转换为Timestamp
     *
     * @param date
     * @return
     */
    public static Timestamp format(Date date) {
        return new Timestamp(date.getTime());
    }

    /**
     * 校验日期是否合法
     *
     * @return
     */
    public static boolean isValidTime(String s) {
        return parse(s, "yyyy-MM-dd HH:mm:ss") != null;
    }

    /**
     * 校验日期是否合法
     *
     * @return
     */
    public static boolean isValidTime(String s, String pattern) {
        return parse(s, pattern) != null;
    }

    public static int getDiffYear(String startTime, String endTime) {
        DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        try {
            int years = (int) (((fmt.parse(endTime).getTime() - fmt.parse(
                    startTime).getTime()) / (1000 * 60 * 60 * 24)) / 365);
            return years;
        } catch (Exception e) {
            // 如果throw java.text.ParseException或者NullPointerException，就说明格式不对
            return 0;
        }
    }

    /**
     * <li>功能描述：时间相减得到天数
     *
     * @param beginDateStr
     * @param endDateStr
     * @return long
     * @author sl
     */
    public static long getDaySub(String beginDateStr, String endDateStr) {
        long day = 0;
        SimpleDateFormat format = new SimpleDateFormat(
                "yyyy-MM-dd");
        Date beginDate = null;
        Date endDate = null;

        try {
            beginDate = format.parse(beginDateStr);
            endDate = format.parse(endDateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        day = (endDate.getTime() - beginDate.getTime()) / (24 * 60 * 60 * 1000);

        return day;
    }

    /**
     * 得到n天之后的日期
     *
     * @param days
     * @return
     */
    public static String getAfterDayDate(String days) {
        int daysInt = Integer.parseInt(days);
        Calendar canlendar = Calendar.getInstance();
        // 日期减 如果不够减会将月变动
        canlendar.add(Calendar.DATE, daysInt);
        Date date = canlendar.getTime();

        SimpleDateFormat sdfd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateStr = sdfd.format(date);

        return dateStr;
    }

    /**
     * 得到n天之后是周几
     *
     * @param days
     * @return
     */
    public static String getAfterDayWeek(String days) {
        int daysInt = Integer.parseInt(days);

        Calendar canlendar = Calendar.getInstance(); // java.util包
        canlendar.add(Calendar.DATE, daysInt); // 日期减 如果不够减会将月变动
        Date date = canlendar.getTime();

        SimpleDateFormat sdf = new SimpleDateFormat("E");
        String dateStr = sdf.format(date);

        return dateStr;
    }

    /**
     * 返回----年--月
     *
     * @param yearMonth
     */
    public static String getDateYearMonth(String yearMonth) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        SimpleDateFormat sdfs = new SimpleDateFormat("yyyy年MM月");
        Date parse = null;
        try {
            parse = sdf.parse(yearMonth);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String format = sdfs.format(parse);
        return format;
    }

    public static String stampToDate(String s) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long lt = new Long(s);
        Date date = new Date(lt * 1000L);
        String res = simpleDateFormat.format(date);
        return res;
    }

    public static String stampToDate(Long s) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long lt = s;
        Date date = new Date(lt * 1000L);
        String res = simpleDateFormat.format(date);
        return res;
    }

    /**
     * 获取上月1号0点0分0秒
     *
     * @return
     */
    public static Date getLastOneDay() {
        Calendar currentDate = new GregorianCalendar();
        currentDate.add(Calendar.MONTH, -1);
        //设置为1号,当前日期既为本月第一天
        currentDate.set(Calendar.DAY_OF_MONTH, 1);
        currentDate.set(Calendar.HOUR_OF_DAY, 0);
        currentDate.set(Calendar.MINUTE, 0);
        currentDate.set(Calendar.SECOND, 0);
        return currentDate.getTime();
    }

    /**
     * 获取当月有多少天
     */
    public static int getMonthSum(String yearMonth) {
        Calendar currentDate = new GregorianCalendar();
        currentDate.set(Integer.parseInt(yearMonth.substring(0, 4)), Integer.parseInt(yearMonth.substring(5, yearMonth.length())), 0);
        return currentDate.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 获取当天0点0分0秒
     *
     * @return
     */
    public static String getNowDay() {
        //创建当前时间格式化为年月日
        Date date = new Date();
        String s = formatDate(date, "yyyy-MM-dd");
        //拼接字符串
        s = s + " 00:00:00";
        return s;
    }

    /**
     * 获取当年1号0点0分0秒
     *
     * @return
     */
    public static Date getYearDay() {
        Calendar currentDate = new GregorianCalendar();
        currentDate.add(Calendar.YEAR, 0);
        currentDate.set(Calendar.DAY_OF_YEAR, 1);
        currentDate.set(Calendar.HOUR_OF_DAY, 0);
        currentDate.set(Calendar.MINUTE, 0);
        currentDate.set(Calendar.SECOND, 0);
        return currentDate.getTime();
    }

    /**
     * 获取本月1号0点0分0秒
     *
     * @return
     */
    public static Date getOneDay() {
        Calendar currentDate = new GregorianCalendar();
        currentDate.add(Calendar.MONTH, 0);
        //设置为1号,当前日期既为本月第一天
        currentDate.set(Calendar.DAY_OF_MONTH, 1);
        currentDate.set(Calendar.HOUR_OF_DAY, 0);
        currentDate.set(Calendar.MINUTE, 0);
        currentDate.set(Calendar.SECOND, 0);
        return currentDate.getTime();
    }

    /**
     * 获取本月1号0点0分0秒
     *
     * @return
     */
    public static String getMonthOneTime() {
        Calendar currentDate = new GregorianCalendar();
        currentDate.add(Calendar.MONTH, 0);
        //设置为1号,当前日期既为本月第一天
        currentDate.set(Calendar.DAY_OF_MONTH, 1);
        currentDate.set(Calendar.HOUR_OF_DAY, 0);
        currentDate.set(Calendar.MINUTE, 0);
        currentDate.set(Calendar.SECOND, 0);
        return formatDateTime(currentDate.getTime());
    }

    /**
     * 获取本月1号
     */
    public static String getMouthOneDay() {
        Calendar currentDate = new GregorianCalendar();
        currentDate.add(Calendar.MONTH, 0);
        //设置为1号,当前日期既为本月第一天
        currentDate.set(Calendar.DAY_OF_MONTH, 1);
        return DateFormatUtils.format(currentDate, "yyyy-MM-dd");
    }

    /**
     * 根据输入日期的当月1号0点0分0秒
     */
    public static String getTimeOneDay(String time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parseTime(time));
        calendar.add(Calendar.MONTH, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
		/*calendar.set(Calendar.HOUR_OF_DAY,0);
		calendar.set(Calendar.MINUTE,0);
		calendar.set(Calendar.SECOND,0);*/
        return formatDateTime(calendar.getTime());
    }

    /***
     * 根据输入日期获取下月1号0点0分0秒
     */
    public static String getTimeNextDay(String time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parseTime(time));
        calendar.add(Calendar.MONTH, 1);
        //设置为1号,当前日期既为本月第一天
        calendar.set(Calendar.DAY_OF_MONTH, 1);
		/*calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);*/
        return formatDateTime(calendar.getTime());
    }

    /**
     * 获取下月1号0点0分0秒
     *
     * @return
     */
    public static Date getMonthOne() {
        Calendar currentDate = new GregorianCalendar();
        currentDate.add(Calendar.MONTH, 1);
        currentDate.set(Calendar.DAY_OF_MONTH, 1);//设置为1号,当前日期既为本月第一天
        currentDate.set(Calendar.HOUR_OF_DAY, 0);
        currentDate.set(Calendar.MINUTE, 0);
        currentDate.set(Calendar.SECOND, 0);
        return currentDate.getTime();
    }

    /**
     * 给时间减去一月
     */
    public static String getLastMonth(String time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parseTime(time));
        calendar.add(2, -1);//给时间减上一月
        return formatDateTime(calendar.getTime());
    }


    /**
     * 给时间做加减操作
     *
     * @param time      根据本时间做加减操作
     * @param dateType  操作维度，分钟，小时，日，月......  Calendar.MINUTE
     * @param timeLenth 操作长度，也就是，时间变化的长度，几分钟，几小时，几天，几个月
     * @return
     */
    public static String getAnyTime(String time, int dateType, int timeLenth) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parseTime(time));
        calendar.add(dateType, timeLenth);
        return formatDateTime(calendar.getTime());
    }


    /**
     * 给时间加上一月
     */
    public static Date getLastMonth(Date time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        calendar.add(2, -1);//给时间加上一月
        return calendar.getTime();
    }

    /**
     * 给时间加上一月
     */
    public static Date getNextMonth(Date time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        calendar.add(2, 1);//给时间加上一月
        return calendar.getTime();
    }

    /**
     * 给时间减去一月
     */
    public static String getLastMonthForDay(String time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parseTime(time));
        calendar.add(2, -1);//给时间减上一月
        return format(calendar.getTime(), "yyyy-MM-dd");
    }

    /**
     * 给时间加上一个月
     */
    public static String getNextMonth(String time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parseTime(time));
        calendar.add(2, 1);
        return formatDateTime(calendar.getTime());
    }

    /**
     * 给时间月份加减操作
     */
    public static String getNextMonth(String time, Integer amount) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parseTime(time));
        calendar.add(Calendar.MONTH, amount);
        return formatDateTime(calendar.getTime());
    }

    /**
     * 给时间月份加减操作
     */
    public static String getMonthAddDate(String time, Integer amount) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parseDate(time));
        calendar.add(Calendar.MONTH, amount);
        return formatDateTime(calendar.getTime());
    }


    /**
     * 得到日期时间字符串，转换格式（yyyy-MM-dd HH:mm:ss）
     */
    public static String formatDateTime(Date date) {
        return formatDate(date, "yyyy-MM-dd HH:mm:ss");
    }

    public static String formatDateTime(Date date, String format) {
        return formatDate(date, format);
    }

    /**
     * 给时间减去一年
     */
    public static String getLastYear(String time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parseTime(time));
        calendar.add(1, -1);//给时间减上一月
        return formatDateTime(calendar.getTime());
    }

    /**
     * 给时间减去一年
     */
    public static String getLastYearForYear(String time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parseTime(time));
        calendar.add(1, -1);//给时间减上一月
        return format(calendar.getTime(), "yyyy-MM");
    }

    /**
     * 给时间加减N年
     */
    public static String getNYear(String time, Integer n) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parse(time, "yyyy-MM"));
        calendar.add(Calendar.YEAR, n);
        return format(calendar.getTime(), "yyyy-MM");
    }

    public static void main(String[] args) {
        System.out.println(getNYear("2019-04", -1));
    }

    /**
     * 给时间减去一天
     */
    public static String getLastDay(String time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parseTime(time));
        calendar.add(Calendar.DAY_OF_MONTH, -1);//给时间减上一天
        return formatDateTime(calendar.getTime());
    }

    /**
     * 给时间减去  n 天
     */
    public static String getBeforeNDay(String time, int n) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parseTime(time));
        calendar.add(Calendar.DAY_OF_MONTH, -n);//给时间减上一天
        return formatDateTime(calendar.getTime());
    }


    /**
     * 给时间 加上  n 天
     */
    public static String getNextNDay(String time, int n) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parseTime(time));
        calendar.add(5, +n);//给时间加上一天
        return formatDateTime(calendar.getTime());
    }

    /**
     * 给时间  加减年 操作
     */
    public static String getNextNYear(String time, int n) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parseTime(time));
        calendar.add(Calendar.YEAR, n);
        return formatDateTime(calendar.getTime());
    }


    /**
     * 给时间  加减月 操作
     */
    public static String offerMonth(String time, int n) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parse(time, "yyyy-MM-dd"));
        calendar.add(Calendar.MONTH, n);
        return formatDateTime(calendar.getTime(), "yyyy-MM-dd");
    }

    /**
     * 给时间加一天
     */
    public static String getAddDay(String time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parseTime(time));
        calendar.add(Calendar.DAY_OF_MONTH, 1);//
        return formatDateTime(calendar.getTime());
    }

    /**
     * 给时间加一天
     */
    public static String getAddDate(String date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parseTime(date + " 00:00:00"));
        calendar.add(Calendar.DAY_OF_MONTH, 1);//
        return format(calendar.getTime(), "yyyy-MM-dd");
    }

    /**
     * 给时间加一天
     */
    public static String getAddDateTime(String dateTime, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parseTime(dateTime));
        calendar.add(Calendar.DAY_OF_MONTH, day);//
        return format(calendar.getTime(), "yyyy-MM-dd HH:mm:ss");
    }

    /**
     * 给时间加一天
     */
    public static String getAddDate(String date, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parseTime(date + " 00:00:00"));
        calendar.add(Calendar.DAY_OF_MONTH, day);//
        return format(calendar.getTime(), "yyyy-MM-dd");
    }

    /**
     * 给时间减一天
     */
    public static String getLastDate(String date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parseTime(date + " 00:00:00"));
        calendar.add(Calendar.DAY_OF_MONTH, -1);//
        return format(calendar.getTime(), "yyyy-MM-dd");
    }

    /**
     * 给时间加一天
     */
    public static Date getAddDay(Date time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        calendar.add(5, 1);//
        return calendar.getTime();
    }

    /**
     * 给时间减一天
     */
    public static Date getSubDay(Date time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        calendar.add(5, -1);//
        return calendar.getTime();
    }

    /**
     * 获取昨天
     */
    public static String getLastDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        return formatDate(calendar.getTime(), "yyyy-MM-dd");
    }

    /**
     * 获取明天
     */
    public static String getNextDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, +1);
        return formatDate(calendar.getTime(), "yyyy-MM-dd");
    }

    /**
     * 获取日期
     */
    public static String getDay(Date date, Integer amount) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, amount);
        return formatDate(calendar.getTime(), "yyyy-MM-dd");
    }

    /**
     * 给该时间 计算  小时级别  加减都可以，count可以正负控制加减
     *
     * @param time
     * @param count
     * @return
     */
    public static String computingTime(String time, int count) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parseTime(time));
        calendar.add(Calendar.HOUR_OF_DAY, count);
        return formatDateTime(calendar.getTime());
    }

    /**
     * 给该时间 计算  小时级别  加减都可以，count可以正负控制加减（24小时制）
     *
     * @param time
     * @param count
     * @return
     */
    public static String addHour(String time, int field, int count) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parseTime(time));
        calendar.add(field, count);
        return formatDateTime(calendar.getTime());
    }

    /**
     * 一分钟级
     *
     * @param start
     * @param end
     * @return
     */
    public static List<String> getMinuteList(String start, String end) {
        List<String> timeList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        String kk = start;
        for (int i = 0; i <= getMinuteDistance(start, end); i++) {
            timeList.add(kk);
            calendar.setTime(parseTime(kk));
            calendar.add(Calendar.MINUTE, 1);
            kk = formatDateTime(calendar.getTime());
        }
        return timeList;
    }

    /**
     * amount 分钟级
     *
     * @param start
     * @param end
     * @return
     */
    public static List<String> getMinuteList(String start, String end, int amount) {
        List<String> timeList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        String kk = start;
        while (kk.compareTo(end) <= 0) {
            timeList.add(kk);
            calendar.setTime(parseTime(kk));
            calendar.add(Calendar.MINUTE, amount);
            kk = formatDateTime(calendar.getTime());
        }
        return timeList;
    }

    /**
     * amount 天级
     *
     * @param start
     * @param end
     * @param amount 步长
     * @return
     */
    public static List<String> getDayList(String start, String end, int amount) {
        List<String> timeList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        String kk = start;
        while (kk.compareTo(end) <= 0) {
            timeList.add(kk);
            calendar.setTime(parseTime(kk));
            calendar.add(Calendar.DAY_OF_MONTH, amount);
            kk = formatDateTime(calendar.getTime());
        }
        return timeList;
    }

    /**
     * 日期
     *
     * @param start
     * @param end
     * @return
     */
    public static List<String> getDayList(String start, String end) {
        List<String> timeList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        String kk = start;
        while (kk.compareTo(end) <= 0) {
            timeList.add(kk);
            calendar.setTime(parseTime(kk + " 00:00:00"));
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            kk = format(calendar.getTime(), "yyyy-MM-dd");
        }
        return timeList;
    }

    /**
     * 日期
     *
     * @param start
     * @param end
     * @param sort(ASC,DESC)
     * @return
     */
    public static List<String> getDayList(String start, String end, String sort) {
        List<String> timeList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        String kk = start;
        while (kk.compareTo(end) <= 0) {
            timeList.add(kk);
            calendar.setTime(parseTime(kk + " 00:00:00"));
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            kk = format(calendar.getTime(), "yyyy-MM-dd");
        }
        if ("DESC".equals(sort) && null != timeList && timeList.size() > 0) {
            Collections.reverse(timeList);
        }
        return timeList;
    }

    /**
     * 一天 15分钟级
     *
     * @param start
     * @return
     */
    public static List<DataResp> getDataResp(String start) {
        List<DataResp> dataResps = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        String kk = start;
        for (int i = 0; i < 97; i++) {
            DataResp dr = new DataResp();
            dr.setTime(kk);
            calendar.setTime(parseTime(kk));
            calendar.add(Calendar.MINUTE, 15);
            kk = formatDateTime(calendar.getTime());
            dataResps.add(dr);
        }
        return dataResps;
    }

    /**
     * 一天 小时级
     *
     * @param start
     * @return
     */
    public static List<DataResp> getDataRespForHour(String start) {
        List<DataResp> dataResps = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        String kk = start;
        for (int i = 0; i < 24; i++) {
            DataResp dr = new DataResp();
            dr.setTime(kk);
            calendar.setTime(parseTime(kk));
            calendar.add(Calendar.HOUR_OF_DAY, 1);
            kk = formatDateTime(calendar.getTime());
            dataResps.add(dr);
        }
        return dataResps;
    }

    /**
     * 小时级
     *
     * @param start
     * @return
     */
    public static List<DataResp> getDataRespForHour(String start, String end) {
        List<DataResp> dataResps = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        String kk = start;
        for (int i = 0; i < getHours(start, end); i++) {
            DataResp dr = new DataResp();
            dr.setTime(kk);
            calendar.setTime(parseTime(kk));
            calendar.add(Calendar.HOUR_OF_DAY, 1);
            kk = formatDateTime(calendar.getTime());
            dataResps.add(dr);
        }
        return dataResps;
    }

    /**
     * 天级
     *
     * @param start
     * @return
     */
    public static List<DataResp> getDataRespForDay(String start, String end) {
        List<DataResp> dataResps = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        long num = getDaySub(start, end);
        for (int i = 0; i <= num; i++) {
            DataResp dr = new DataResp();

            calendar.setTime(parseTime(start));
//            calendar.setTime(parse(start, "yyyy-MM-dd"));
            calendar.add(Calendar.DAY_OF_MONTH, i);
            dr.setTime(formatDate(calendar.getTime(), "yyyy-MM-dd"));
            dataResps.add(dr);
        }
        return dataResps;
    }

    /**
     * 天级坐标
     */
    public static List<DataResp> getDayCoord(String start, String end) {
        List<DataResp> dataResps = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        long num = getDaySub(start, end);
        for (int i = 0; i <= num; i++) {
            DataResp dr = new DataResp();
            calendar.setTime(parse(start, "yyyy-MM-dd"));
            calendar.add(Calendar.DAY_OF_MONTH, i);
            dr.setTime(formatDate(calendar.getTime(), "yyyy-MM-dd"));
            dataResps.add(dr);
        }
        return dataResps;
    }

    /**
     * 月级坐标
     */
    public static List<DataResp> getMonthCoord(String start, String end) {
        List<DataResp> dataResps = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        long num = getMonthDiff(end, start);
        for (int i = 0; i <= num; i++) {
            DataResp dr = new DataResp();
            calendar.setTime(parse(start, "yyyy-MM"));
            calendar.add(Calendar.MONTH, i);
            dr.setTime(formatDate(calendar.getTime(), "yyyy-MM"));
            dataResps.add(dr);
        }
        return dataResps;
    }

    /**
     * 年
     */
    public static List<String> getYearList(String startYear, String endYear) {
        List<String> yearList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        String kk = startYear;
        while (kk.compareTo(endYear) <= 0) {
            yearList.add(kk);
            calendar.setTime(parseTime(kk + "-01-01 00:00:00"));
            calendar.add(Calendar.YEAR, 1);
            kk = format(calendar.getTime(), "yyyy");
        }
        return yearList;
    }

    /**
     * 对日期、时间进行加、减操作。
     * <pre>
     *     DateUtil.add(date, Calendar.YEAR, -1); //date减一年
     *     DateUtil.add(date, Calendar.HOUR, -4); //date减4个小时
     *     DateUtil.add(date, Calendar.MONTH, 3); //date加3个月
     * </pre>
     *
     * @param date   日期时间。
     * @param field  执行加减操作的属性，参考{@link Calendar#YEAR}、{@link Calendar#MONTH}、{@link Calendar#HOUR}等。
     * @param amount 加减数量。
     * @return 执行加减操作后的日期、时间。
     */
    public static Date add(Date date, int field, int amount) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date.getTime());
        calendar.add(field, amount);
        return calendar.getTime();
    }

    /**
     * 获取当前时间的前后时间的值
     */
    public static String getAroundDate(int year, int month, int day, int hour, int minute, int second) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.YEAR, year);
        calendar.add(Calendar.MONTH, month);
        calendar.add(Calendar.DATE, day);
        calendar.add(Calendar.HOUR_OF_DAY, hour);
        calendar.add(Calendar.MINUTE, minute);
        calendar.add(Calendar.SECOND, second);
        Date date = calendar.getTime();
        return getTime(date);
    }

    public static String getBeforeHourTime(Calendar calendar, int ihour) {
        String returnstr = "";
        calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) - ihour);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        returnstr = df.format(calendar.getTime());
        return returnstr;
    }

    /**
     * 取到 hours 以前时间
     *
     * @param hours
     * @return
     */
    public static String getBeforeHourTime(Date date, int hours, String formate) {
        String returnstr = "";
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.HOUR_OF_DAY, hours);
            SimpleDateFormat df = new SimpleDateFormat(formate);
            returnstr = df.format(cal.getTime());
        } catch (Exception e) {
            System.out.println("获取时间之前n" + hours + "小时的时间异常！");
        }
        return returnstr;
    }

    /**
     * 校验日期是否合法
     *
     * @param date
     * @return
     */
    public static Boolean isValidDate(String date) {
        return isValidTime(date, "yyyy-MM-dd");
    }

    /**
     * 校验日期是否合法
     *
     * @param yearMonth
     * @return
     */
    public static Boolean yearMonth(String yearMonth) {
        return isValidTime(yearMonth, "yyyy-MM");
    }

    /**
     * 判断日期是否是当前月
     */
    public static boolean isThisMonth(String time) {
        boolean is;
        boolean year = time.substring(0, 4).equals(getMonth().substring(0, 4));
        boolean month = time.substring(5, 7).equals(getMonth().substring(5, 7));
        if (year && month) {
            is = true;
        } else {
            is = false;
        }
        return is;
    }

    /**
     * 判断日期是否是当前月
     */
    public static boolean isThisYear(String time) {
        boolean is;
        boolean year = time.substring(0, 4).equals(getMonth().substring(0, 4));
        if (year) {
            is = true;
        } else {
            is = false;
        }
        return is;
    }

    /**
     * 判断日期是否大于等于当前月
     */
    public static boolean isGtThisMonth(String time) {
        boolean is;
        boolean year = Integer.parseInt(time.substring(0, 4)) >= Integer.parseInt(getMonth().substring(0, 4));
        boolean month = Integer.parseInt(time.substring(5, 7)) >= Integer.parseInt(getMonth().substring(5, 7));
        if (year && month) {
            is = true;
        } else {
            is = false;
        }
        return is;
    }

    /**
     * 去时间的上个月
     */
    public static String getLastMonthOfYear(String time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parse(time, "yyyy-MM"));
        calendar.add(2, -1);
        return format(calendar.getTime(), "yyyy-MM");
    }

    /**
     * 获取当前时间的前天的数据
     */
    public static String getLastOneday() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, -1);
        Date date = calendar.getTime();
        return getDay(date);
    }

    /**
     * 获取当前时间的前天的数据
     */
    public static String getLastTwoday() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, -2);
        Date date = calendar.getTime();
        return getDay(date);
    }

    /**
     * @param date1 <String>
     * @param date2 <String>
     * @return int
     * @throws ParseException
     */
    public static int getMonthSpace(String date1, String date2) {

        int result = 0;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");

        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();

        c1.setTime(parseTime(date1));
        c2.setTime(parseTime(date2));

        result = c2.get(Calendar.MONTH) - c1.get(Calendar.MONTH);

        return result == 0 ? 1 : Math.abs(result);

    }

    /**
     * @param date1 <String>
     * @param date2 <String>
     * @return int
     * @throws ParseException
     */
    public static int getMonthSpaceExt(String date1, String date2) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
        try {
            from.setTime(sdf.parse(date1));
            to.setTime(sdf.parse(date2));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //只要年月
        int fromYear = from.get(Calendar.YEAR);
        int fromMonth = from.get(Calendar.MONTH);
        int toYear = to.get(Calendar.YEAR);
        int toMonth = to.get(Calendar.MONTH);
        int month = toYear * 12 + toMonth - (fromYear * 12 + fromMonth);

        return month;
    }

    public static int getYearSpaceExt(String date1, String date2) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
        try {
            from.setTime(sdf.parse(date1));
            to.setTime(sdf.parse(date2));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //只要年月
        int fromYear = from.get(Calendar.YEAR);
        int toYear = to.get(Calendar.YEAR);
        int year = toYear - fromYear;

        return year;
    }

    /**
     * @param date1 <String>
     * @param date2 <String>
     * @return int
     * @throws ParseException
     */
    public static int getDateSpace(String date1, String date2) {

        int result = 0;

        Calendar calst = Calendar.getInstance();
        Calendar caled = Calendar.getInstance();

        calst.setTime(parseDate(date1));
        caled.setTime(parseDate(date2));

        //设置时间为0时
        calst.set(Calendar.HOUR_OF_DAY, 0);
        calst.set(Calendar.MINUTE, 0);
        calst.set(Calendar.SECOND, 0);
        caled.set(Calendar.HOUR_OF_DAY, 0);
        caled.set(Calendar.MINUTE, 0);
        caled.set(Calendar.SECOND, 0);
        //得到两个日期相差的天数
        int days = ((int) (caled.getTime().getTime() / 1000) - (int) (calst.getTime().getTime() / 1000)) / 3600 / 24;

        return days;
    }

    /**
     * 计算两个时间相差多少小时
     *
     * @param startDate
     * @param endDate
     * @return
     */
    public static long getDatePoor(String startDate, String endDate) {
        Date startTime = parse(startDate, "yyyy-MM-dd HH:mm");
        Date endTime = parse(endDate, "yyyy-MM-dd HH:mm");
        long nd = 1000 * 24 * 60 * 60;
        long nh = 1000 * 60 * 60;
        //long nm = 1000 * 60;
        long diff = endTime.getTime() - startTime.getTime();
        // 计算差多少天
        long day = diff / nd;
        // 计算差多少小时
        long hour = diff % nd / nh;
        if (day > 0) {
            hour = day * 24 + hour;
        }
		/*// 计算差多少分钟
		long min = diff % nd % nh / nm;
		// 计算差多少秒//输出结果
		// long sec = diff % nd % nh % nm / ns;
		System.out.println(day + "天" + hour + "小时" + min + "分钟");*/
        return hour;
    }

    /**
     * 到当前时间小时数
     *
     * @param time
     * @return
     */
    public static int getHourByNow(String time) {
        Date startTime = parse(time, "yyyy-MM-dd HH:00:00");
        Date endTime = new Date();
        int nh = 1000 * 60 * 60;
        Long diff = endTime.getTime() - startTime.getTime();
        Long hour = diff / nh;
        return hour.intValue();
    }

    /**
     * 计算两个时间相差多少小时
     *
     * @param startDate
     * @param endDate
     * @return
     */
    public static Double getHours(String startDate, String endDate) {
        Date startTime = parse(startDate, "yyyy-MM-dd HH:mm:ss");
        Date endTime = parse(endDate, "yyyy-MM-dd HH:mm:ss");
        Double diff = Double.parseDouble("" + (endTime.getTime() - startTime.getTime()));
        return diff / 1000 / 60 / 60;
    }

    /**
     * 计算两个时间相差多少小时
     *
     * @param startDate
     * @param endDate
     * @return
     */
    public static int getHoursWithInteger(String startDate, String endDate) {
        Date startTime = parse(startDate, "yyyy-MM-dd HH:mm:ss");
        Date endTime = parse(endDate, "yyyy-MM-dd HH:mm:ss");
        Double diff = Double.parseDouble("" + (endTime.getTime() - startTime.getTime()));
        Double hours = diff / 1000 / 60 / 60;
        return MathUtils.aDoubletwo(hours, 0).intValue();
    }

    /**
     * 计算两个时间相差多少分钟
     *
     * @param startDate
     * @param endDate
     * @return
     */
    public static long getMinuteDistance(String startDate, String endDate) {
        Date startTime = parse(startDate, "yyyy-MM-dd HH:mm:ss");
        Date endTime = parse(endDate, "yyyy-MM-dd HH:mm:ss");
        long nm = 1000 * 60;
        long diff = endTime.getTime() - startTime.getTime();
        // 计算差多少分钟
        long minute = diff / nm;
        return minute;
    }

    /**
     * 给时间加一分钟
     */
    public static String getAddHour(String time, int hour) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parseTime(time));
        calendar.add(Calendar.HOUR_OF_DAY, hour);
        return formatDateTime(calendar.getTime());
    }

    /**
     * 给时间加一分钟
     */
    public static String getAddMinute(String time, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parseTime(time));
        calendar.add(12, minute);
        return formatDateTime(calendar.getTime());
    }

    /**
     * 给时间加一分钟
     */
    public static String getAddMinute(Date time, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        calendar.add(Calendar.MINUTE, minute);
        return formatDateTime(calendar.getTime());
    }

    /**
     * 给时间加一分钟(yyyy-MM-dd HH:mm)
     */
    public static String getAddMinuteAddSecond(String time, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parseTime(time + ":00"));
        calendar.add(12, minute);
        return format(formatDateTime(calendar.getTime()), "yyyy-MM-dd HH:mm");
    }

    /**
     * 给时间加一天
     */
    public static String getAddDay(String time, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parseDate(time));
        calendar.add(5, day);
        return format(calendar.getTime(), "yyyy-MM-dd");
    }

    /**
     * 给时间加一月
     */
    public static String getAddMonth(String time, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parse(time, "yyyy-MM"));
        calendar.add(2, month);
        return format(calendar.getTime(), "yyyy-MM");
    }

    /**
     * 获取两个日期相差的月数
     *
     * @param d1 较大的日期
     * @param d2 较小的日期
     * @return 如果d1>d2返回 月数差 否则返回0
     */
    public static int getMonthDiff(String d1, String d2) {
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c1.setTime(parse(d1, "yyyy-MM"));
        c2.setTime(parse(d2, "yyyy-MM"));
        if (c1.getTimeInMillis() < c2.getTimeInMillis()) return 0;
        int year1 = c1.get(Calendar.YEAR);
        int year2 = c2.get(Calendar.YEAR);
        int month1 = c1.get(Calendar.MONTH);
        int month2 = c2.get(Calendar.MONTH);
        int day1 = c1.get(Calendar.DAY_OF_MONTH);
        int day2 = c2.get(Calendar.DAY_OF_MONTH);
        // 获取年的差值 假设 d1 = 2015-8-16  d2 = 2011-9-30
        int yearInterval = year1 - year2;
        // 如果 d1的 月-日 小于 d2的 月-日 那么 yearInterval-- 这样就得到了相差的年数
        if (month1 < month2 || month1 == month2 && day1 < day2) {
            yearInterval--;
        }
        // 获取月数差值
        int monthInterval = (month1 + 12) - month2;
        if (day1 < day2) {
            monthInterval--;
        }
        monthInterval %= 12;
        return yearInterval * 12 + monthInterval;
    }

    /**
     * 上月第一天
     *
     * @return
     */
    public static String getLastMonthFirst() {
        //获取当前日期
        Calendar cale = Calendar.getInstance();
        cale.add(Calendar.MONTH, -1);
        //设置为1号,当前日期既为本月第一天
        cale.set(Calendar.DAY_OF_MONTH, 1);
        return format(cale.getTime(), "yyyy-MM-dd");
    }

    /**
     * 上月最后一天
     *
     * @return
     */
    public static String getLastMonth() {
        //获取当前日期s
        Calendar cale = Calendar.getInstance();
        cale.set(Calendar.DAY_OF_MONTH, 0);
        return format(cale.getTime(), "yyyy-MM-dd");
    }

    /**
     * 根据月份取每日
     *
     * @param date yyyy-MM
     * @return
     */
    public static Map<String, String> getMonthDay(String date) {
        Map<String, String> map = new LinkedHashMap<>();
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, Integer.parseInt(date.substring(0, 4)));
        calendar.set(Calendar.MONTH, Integer.parseInt(date.substring(5, 7)) - 1);
        int maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int i = 1; i <= maxDay; i++) {
            if (i < 10) {
                map.put(date + "-0" + i, null);
            } else {
                map.put(date + "-" + i, null);
            }
        }
        return map;
    }
    /**
     * 得到当前时间上个小时的正点数
     */
    /**
     * 获取本月1号
     */
    public static String getLastHourOfDay() {
        int minute = 02;
        Calendar currentDate = new GregorianCalendar();
        //判断当前时间的分钟数是否大于02
        if (currentDate.get(Calendar.MINUTE) < minute) {
            currentDate.add(Calendar.HOUR_OF_DAY, -2);
            currentDate.set(Calendar.MINUTE, 0);
            currentDate.set(Calendar.SECOND, 0);
            return format(currentDate.getTime(), "yyyy-MM-dd HH:mm");
        } else {
            currentDate.add(Calendar.HOUR_OF_DAY, -1);
            currentDate.set(Calendar.MINUTE, 0);
            currentDate.set(Calendar.SECOND, 0);
            return format(currentDate.getTime(), "yyyy-MM-dd HH:mm");
        }
    }

    /**
     * 获取当前时间的昨天的数据
     */
    public static String getLastOfday() {
        Calendar currentDate = new GregorianCalendar();
        currentDate.add(Calendar.DATE, -1);
        return format(currentDate.getTime(), "yyyy-MM-dd");
    }

    /**
     * 得到时间的上个月yyyy-MM-dd
     */
    public static String getLastOneMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(2, -1);
        return format(calendar.getTime(), "yyyy-MM");
    }

    public static String getLastMonths(Integer n) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(2, n);
        return format(calendar.getTime(), "yyyy-MM");
    }


    /**
     * 比较两个日期的先后顺序
     *
     * @param first  date1
     * @param second date2
     * @return EQUAL - if equal BEFORE - if before than date2 AFTER - if over than date2
     */
    public static int compareTwoDate(Date first, Date second) {
        if ((first == null) && (second == null)) {
            return EQUAL;
        } else if (first == null) {
            return BEFORE;
        } else if (second == null) {
            return AFTER;
        } else if (first.before(second)) {
            return BEFORE;
        } else if (first.after(second)) {
            return AFTER;
        } else {
            return EQUAL;
        }
    }

    /**
     * 比较日期是否介于两者之间
     *
     * @param date  the specified date
     * @param begin date1
     * @param end   date2
     * @return true - between date1 and date2 false - not between date1 and date2
     */
    public static boolean isDateBetween(Date date, Date begin, Date end) {
        int c1 = compareTwoDate(begin, date);
        int c2 = compareTwoDate(date, end);

        return (((c1 == BEFORE) && (c2 == BEFORE)) || (c1 == EQUAL) || (c2 == EQUAL));
    }

    /***
     * 根据输入日期YYYY-MM的到输入日期的1号
     */
    public static String getOneOfMonth(String time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parse(time, "yyyy-MM"));
        //设置为1号,当前日期既为本月第一天
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return format(calendar.getTime(), "yyyy-MM-dd");
    }

    /***
     * 根据输入日期YYYY-MM的到输入日期的最后一天
     */
    public static String getLastOfMonth(String time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parse(time, "yyyy-MM"));
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return format(calendar.getTime(), "yyyy-MM-dd");
    }

    /***
     * 根据输入日期YYYY的到输入日期的1月
     */
    public static String getOneOfYear(String time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parse(time, "yyyy"));
        //设置为1号,当前日期既为本月第一天
        calendar.set(Calendar.MONTH, 0);
        return format(calendar.getTime(), "yyyy-MM");
    }

    /***
     * 根据输入日期YYYY-MM的到输入日期的最后一天
     */
    public static String getLastOfYear(String time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parse(time, "yyyy"));
        calendar.set(Calendar.MONTH, calendar.getActualMaximum(Calendar.MONTH));
        return format(calendar.getTime(), "yyyy-MM");
    }

    public static Date addDate(int number, short unit) {

        Calendar c = Calendar.getInstance();
        if (1 == unit) {
            c.add(Calendar.DAY_OF_MONTH, number);
        } else {
            c.add(Calendar.MONTH, number);
        }
        return c.getTime();
    }

    /**
     * <日期加减天数><功能具体实现>
     *
     * @param date
     * @param n
     * @return java.lang.String
     * @create：2018/9/18 上午11:19
     * @author sl
     */
    public static String dateOpDay(String date, int n) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date sDate = null;
        try {
            sDate = sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar c = Calendar.getInstance();
        c.setTime(sDate);
        c.add(Calendar.DAY_OF_MONTH, n);

        sDate = c.getTime();

        return sdf.format(sDate);
    }

    /**
     * <获取两个时间中的每一天><功能具体实现>
     *
     * @param dBegin
     * @param dEnd
     * @return java.util.List<java.util.Date>
     * @create：2018/10/9 下午5:00
     * @author sl
     */
    public static List<Date> findDates(Date dBegin, Date dEnd) {
        List<Date> lDate = new ArrayList<Date>();
        lDate.add(dBegin);
        Calendar calBegin = Calendar.getInstance();
        // 使用给定的 Date 设置此 Calendar 的时间
        calBegin.setTime(dBegin);
        Calendar calEnd = Calendar.getInstance();
        // 使用给定的 Date 设置此 Calendar 的时间
        calEnd.setTime(dEnd);
        // 测试此日期是否在指定日期之后
        while (dEnd.after(calBegin.getTime())) {
            // 根据日历的规则，为给定的日历字段添加或减去指定的时间量
            calBegin.add(Calendar.DAY_OF_MONTH, 1);
            lDate.add(calBegin.getTime());
        }
        return lDate;
    }

    public static List<String> findDateStrs(Date dBegin, Date dEnd) {
        List<String> lDate = new ArrayList<String>();
        lDate.add(format(dBegin, "yyyy-MM-dd"));
        Calendar calBegin = Calendar.getInstance();
        // 使用给定的 Date 设置此 Calendar 的时间
        calBegin.setTime(dBegin);
        Calendar calEnd = Calendar.getInstance();
        // 使用给定的 Date 设置此 Calendar 的时间
        calEnd.setTime(dEnd);
        // 测试此日期是否在指定日期之后
        while (dEnd.after(calBegin.getTime())) {
            // 根据日历的规则，为给定的日历字段添加或减去指定的时间量
            calBegin.add(Calendar.DAY_OF_MONTH, 1);
            lDate.add(format(calBegin.getTime(), "yyyy-MM-dd"));
        }
        return lDate;
    }

    /**
     * <获取某段日期内每一个月份><功能具体实现>
     *
     * @param minDate
     * @param maxDate
     * @return java.util.List<java.lang.String>
     * @create：2018/10/9 下午5:07
     * @author sl
     */
    public static List<String> getMonths(String minDate, String maxDate) {
        ArrayList<String> result = new ArrayList<String>();

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");//格式化为年月

            Calendar min = Calendar.getInstance();
            Calendar max = Calendar.getInstance();

            min.setTime(sdf.parse(minDate));
            min.set(min.get(Calendar.YEAR), min.get(Calendar.MONTH), 1);

            max.setTime(sdf.parse(maxDate));
            max.set(max.get(Calendar.YEAR), max.get(Calendar.MONTH), 2);

            Calendar curr = min;
            while (curr.before(max)) {
                result.add(sdf.format(curr.getTime()));
                curr.add(Calendar.MONTH, 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * <判断是否是当天><功能具体实现>
     *
     * @param str
     * @param formatStr
     * @return boolean
     * @create：2018/12/10 11:06 AM
     * @author sl
     */
    public static boolean isToday(String str, String formatStr) {
        SimpleDateFormat format = new SimpleDateFormat(formatStr);
        Date date = null;
        try {
            date = format.parse(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Calendar c1 = Calendar.getInstance();
        c1.setTime(date);
        int year1 = c1.get(Calendar.YEAR);
        int month1 = c1.get(Calendar.MONTH) + 1;
        int day1 = c1.get(Calendar.DAY_OF_MONTH);
        Calendar c2 = Calendar.getInstance();
        c2.setTime(new Date());
        int year2 = c2.get(Calendar.YEAR);
        int month2 = c2.get(Calendar.MONTH) + 1;
        int day2 = c2.get(Calendar.DAY_OF_MONTH);
        if (year1 == year2 && month1 == month2 && day1 == day2) {
            return true;
        }
        return false;
    }

    //获取某个日期的开始时间
    public static Timestamp getDayStartTime(Date d) {
        Calendar calendar = Calendar.getInstance();
        if (null != d) {
            calendar.setTime(d);
        }
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return new Timestamp(calendar.getTimeInMillis());
    }


    //获取本周的开始时间
    public static Date getBeginDayOfWeek() {
        Date date = new Date();
        if (date == null) {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int dayofweek = cal.get(Calendar.DAY_OF_WEEK);
        if (dayofweek == 1) {
            dayofweek += 7;
        }
        cal.add(Calendar.DATE, 2 - dayofweek);
        return getDayStartTime(cal.getTime());
    }


    /**
     * 判断 时间 是否是本日，本周，本月
     */
    public static Boolean verificationTime(String startTime, String type) {

        switch (type) {
            case "day":
                if (startTime.equals(getDay())) {
                    return true;
                }
            case "week":
                String weekStart = format(getBeginDayOfWeek(), "yyyy-MM-dd");
                if (startTime.compareTo(weekStart) >= 0) {
                    return true;
                }
            case "month":
                if (startTime.equals(format(new Date(), "yyyy-MM"))) {
                    return true;
                }

        }
        return false;
    }

    /**
     * 获取上周的开始时间
     */
    @SuppressWarnings("unused")
    public static Date getBeginDayOfLastWeek() {
        Date date = new Date();
        if (date == null) {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int dayofweek = cal.get(Calendar.DAY_OF_WEEK);
        if (dayofweek == 1) {
            dayofweek += 7;
        }
        cal.add(Calendar.DATE, 2 - dayofweek - 7);
        return getDayStartTime(cal.getTime());
    }

    /**
     * 获取上周的结束时间
     */
    public static Date getEndDayOfLastWeek() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(getBeginDayOfLastWeek());
        cal.add(Calendar.DAY_OF_WEEK, 6);
        Date weekEndSta = cal.getTime();
        return getDayEndTime(weekEndSta);
    }

    /**
     * 获取某个日期的结束时间
     */
    public static Timestamp getDayEndTime(Date d) {
        Calendar calendar = Calendar.getInstance();
        if (null != d) {
            calendar.setTime(d);
        }
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH), 23, 59, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return new Timestamp(calendar.getTimeInMillis());
    }

    public static List<String> getDate(String start, String end) { //
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        List<String> list = new ArrayList<String>(); //保存日期集合
        try {
            Date date_start = sdf.parse(start);
            Date date_end = sdf.parse(end);
            Date date = date_start;
            Calendar cd = Calendar.getInstance();//用Calendar 进行日期比较判断
            while (date.getTime() <= date_end.getTime()) {
                list.add(sdf.format(date));
                cd.setTime(date);
                cd.add(Calendar.DATE, 1);//增加一天 放入集合
                date = cd.getTime();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return list;
    }


    public static String transDateFormat(String time, String sourceFormat, String targetFormat) {

        try {
            SimpleDateFormat format = new SimpleDateFormat(sourceFormat);
            Date date = format.parse(time);
            format = new SimpleDateFormat(targetFormat);
            String newD = format.format(date);
            return newD;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 判断指定时间是否相等，精确到：日
     * kai.guo
     *
     * @param currentDate
     * @param assignedDate
     * @return
     */
    public static boolean isTheSameDay(String currentDate, String assignedDate) {

        if (parseDate(currentDate) == null || parseDate(assignedDate) == null) {
            return false;
        }
        return parseDate(currentDate).getTime() == parseDate(assignedDate).getTime();
    }


    /**
     * 判断指定时间是否相等，精确到：月
     * kai.guo
     *
     * @param currentDate
     * @param assignedDate
     * @return
     */
    public static boolean isTheSameMonth(String currentDate, String assignedDate) {

        if (parseMonth(currentDate) == null || parseMonth(assignedDate) == null) {
            return false;
        }
        return parseMonth(currentDate).getTime() == parseMonth(assignedDate).getTime();
    }

    /* 格式化日期
     *
     * @return
     */
    public static Date parseMonth(String date) {
        return parse(date, "yyyy-MM");
    }

    /**
     *  
     *  得到指定月份的第一天 
     *   kai.guo  
     *    * @return   
     *   
     */
    public static String getMonthFirstDay(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, calendar
                .getActualMinimum(Calendar.DAY_OF_MONTH));
        return sdf.format(calendar.getTime());
    }

    /**
     *  
     *  得到指定月份的最后一天 
     *   kai.guo
     *    * @return   
     *   
     */
    public static String getMonthLastDay(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, calendar
                .getActualMaximum(Calendar.DAY_OF_MONTH));
        return sdf.format(calendar.getTime());
    }

    /**
     * 给时间加一年
     */
    public static String getAddYear(String time, int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parse(time, "yyyy"));
        calendar.add(1, year);
        return format(calendar.getTime(), "yyyy");
    }

    /**
     * 给时间减去一年
     */
    public static String getLastYearWithString(String time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parse(time, "yyyy"));
        calendar.add(1, -1);
        return format(calendar.getTime(), "yyyy");
    }


    /**
     * 给时间加一天
     */
    public static String getLastDayWithString(String time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parseDate(time));
        calendar.add(5, -1);
        return format(calendar.getTime(), "yyyy-MM-dd");
    }


}
