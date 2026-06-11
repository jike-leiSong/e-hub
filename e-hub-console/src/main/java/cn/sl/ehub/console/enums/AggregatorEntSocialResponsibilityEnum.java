package cn.sl.ehub.console.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 企业社会责任计算配置枚举
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Getter
@AllArgsConstructor
public enum AggregatorEntSocialResponsibilityEnum {

    CLEAR_POWER("clearPower", "累计使用清洁电量"),
    CO2("co2", "减排CO₂"),
    TREE("tree", "相当于累计植树约"),
    COAL("coal", "节约标准煤"),
    SO2("so2", "减排SO₂"),
    NOX("nox", "减排氮氧化物"),
    CO2V2("co2V2", "减排CO₂"),
    TREEV2("treeV2", "相当于累计植树约"),
    COALV2("coalV2", "节约标准煤"),
    SO2V2("so2V2", "减排SO₂"),
    NOXV2("noxV2", "减排氮氧化物"),
    AREAV2("areaV2", "再造森林面积"),
    ;

    private String code;
    private String desc;
}
