package cn.sl.ehub.console.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 日枚举
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Getter
@AllArgsConstructor
public enum DayTypeEnum {

    YESTERDAY("yesterday", "昨日"),
    TODAY("today", "今日"),
    TOMORROW("tomorrow", "明日");

    private String code;
    private String desc;
}
