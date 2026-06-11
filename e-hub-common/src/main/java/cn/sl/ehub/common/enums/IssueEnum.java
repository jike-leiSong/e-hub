package cn.sl.ehub.common.enums;

import lombok.Getter;

/**
 * @Description:
 * @Author sl
 * @Date 2026-05-28
 */
public enum IssueEnum {

    CP("CP","京津唐市场出清价格"),
    PR("PR","调峰贡献率"),
    FEE("FEE","京津唐市场调峰收益"),
    DAP("DAP","日前计划"),
    DACP("DACP","省间日前出清价格"),
    DAFEE("DAFEE","省间日前调峰收益"),
    AVGRT("AVGRT","平均负荷率"),
    DACE("DACE","省间市场中标量");

    private String code;

    private String desc;

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    IssueEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
