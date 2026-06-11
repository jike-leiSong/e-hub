package cn.sl.ehub.common.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * @Description:
 * @Author sl
 * @Date 2026-05-28
 */
public enum TemplateNameEnum {

    MEAS_INDUSTRIAL_LOAD("MEAS", "25", "singleMeasIndustrialLoad.ftl","SingleMeasIndustrialLoadDTO"),
    MEAS_ELECTRIC_HEATING("MEAS", "26", "singleMeasElectricHeating.ftl","SingleMeasElectricHeatingDTO"),
    MEAS_DISTRIBUTED_ENERGY("MEAS", "27", "singleMeasDistributedEnergyStorage.ftl","SingleMeasDistributedEnergyStorageDTO"),

    MODEL_INDUSTRIAL_LOAD("MODEL", "25", "singleModeIndustrialLoad.ftl","SingleModeIndustrialLoadDTO"),
    MODEL_ELECTRIC_HEATING("MODEL", "26", "singleModeElectricHeating.ftl","SingleModeElectricHeatingDTO"),
    MODEL_DISTRIBUTED_ENERGY("MODEL", "27", "singleModeDistributedEnergyStorage.ftl","SingleModeDistributedEnergyStorageDTO");

    private String type;
    private String channelNo;
    private String name;
    private String dtoName;

    public String getType() {
        return type;
    }

    public String getChannelNo() {
        return channelNo;
    }

    public String getName() {
        return name;
    }

    public String getDtoName() {
        return dtoName;
    }

    TemplateNameEnum(String type, String channelNo, String name, String dtoName) {
        this.type = type;
        this.channelNo = channelNo;
        this.name = name;
        this.dtoName = dtoName;
    }

    public static TemplateNameEnum getByTypeAndNo(String type, String channelNo) {
        TemplateNameEnum[] values = TemplateNameEnum.values();
        for (TemplateNameEnum value : values) {
            if (
                    StringUtils.equals(channelNo, value.channelNo)
                            && StringUtils.equals(type, value.getType())) {
                return value;
            }
        }
        return null;
    }
}
