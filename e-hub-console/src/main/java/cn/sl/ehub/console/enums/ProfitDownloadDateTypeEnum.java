package cn.sl.ehub.console.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProfitDownloadDateTypeEnum {

    CURRENT_MONTH(1, "当前月"),
    PREVIOUS_MONTH(2, "上个月");

    private Integer code;
    private String desc;

}
