package cn.sl.ehub.common.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * @Author sl
 * @Date 2026-05-28
 **/
public enum TemplateNameNewEnum {
//工业负荷VPP
    MEAS_INDUSTRIAL_LOAD("MEAS", "VPP", "singleMeasIndustrialLoad.ftl","SingleMeasIndustrialLoadDTO"),
//    电采暖EH
    MEAS_ELECTRIC_HEATING("MEAS", "EH", "singleMeasElectricHeating.ftl","SingleMeasElectricHeatingDTO"),
//    分布式储能DES
    MEAS_DISTRIBUTED_ENERGY("MEAS", "DES", "singleMeasDistributedEnergyStorage.ftl","SingleMeasDistributedEnergyStorageDTO"),
    MEAS_CHARGING_PILE("MEAS", "CP", "singleMeasElectricVehicle.ftl",""),
    MODEL_INDUSTRIAL_LOAD("MODEL", "VPP", "singleModeIndustrialLoad.ftl","SingleModeIndustrialLoadDTO"),
    MODEL_ELECTRIC_HEATING("MODEL", "EH", "singleModeElectricHeating.ftl","SingleModeElectricHeatingDTO"),
    MODEL_DISTRIBUTED_ENERGY("MODEL", "DES", "singleModeDistributedEnergyStorage.ftl","SingleModeDistributedEnergyStorageDTO"),
    MODEL_CHARGING_PILE("MODEL", "CP", "singleModeElectricVehicle.ftl","");

    private String type;
    private String resourcesCode;
    private String name;
    private String dtoName;

    public String getType() {
        return type;
    }

    public String getResourcesCode() {
        return resourcesCode;
    }

    public String getName() {
        return name;
    }

    public String getDtoName() {
        return dtoName;
    }

    TemplateNameNewEnum(String type, String channelNo, String name, String dtoName) {
        this.type = type;
        this.resourcesCode = channelNo;
        this.name = name;
        this.dtoName = dtoName;
    }

    public static TemplateNameNewEnum getByTypeAndNo(String type, String resourcesCode) {
        TemplateNameNewEnum[] values = TemplateNameNewEnum.values();
        for (TemplateNameNewEnum value : values) {
            if (
                    StringUtils.equals(resourcesCode, value.getResourcesCode())
                            && StringUtils.equals(type, value.getType())) {
                return value;
            }
        }
        return null;
    }
}
