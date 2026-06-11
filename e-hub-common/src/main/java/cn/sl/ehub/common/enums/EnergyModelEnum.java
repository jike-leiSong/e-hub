package cn.sl.ehub.common.enums;

import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @Description: 资源渠道号编码
 * @Author sl
 * @Date 2026-05-28
 */
public enum EnergyModelEnum {

    INDUSTRIAL_LOAD("25", "工业负荷","VPP"),
    ELECTRIC_HEATING("26", "电采暖","EH"),
    DISTRIBUTED_ENERGY("27", "储能","DES"),

    XIN_TAI_INDUSTRIAL_LOAD("44", "工业负荷","VPP"),
    XIN_TAI_ELECTRIC_HEATING("15", "电采暖","EH"),
    XIN_TAI_CHARGING_PILE("17","充电桩","CP");

    private String channelNo;
    private String name;
    private String code;

    public String getChannelNo() {
        return channelNo;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    EnergyModelEnum(String channelNo, String name, String code) {
        this.channelNo = channelNo;
        this.name = name;
        this.code = code;
    }

    public static EnergyModelEnum getByCode(String channelNo) {
        EnergyModelEnum[] values = EnergyModelEnum.values();
        for (EnergyModelEnum value : values) {
            if (StringUtils.equals(value.channelNo, value.channelNo)) {
                return value;
            }
        }
        return null;
    }

    public static List<String> getChannelNoList() {
        List<String> channelNoList = Lists.newArrayList();
        EnergyModelEnum[] values = EnergyModelEnum.values();
        for (EnergyModelEnum value : values) {
            channelNoList.add(value.getChannelNo());
        }
        return channelNoList;
    }

    public static String getName(String channelNo) {
        EnergyModelEnum[] values = EnergyModelEnum.values();
        for (EnergyModelEnum value : values) {
            if (StringUtils.equals(value.channelNo, channelNo)) {
                return value.name;
            }
        }
        return null;
    }
}
