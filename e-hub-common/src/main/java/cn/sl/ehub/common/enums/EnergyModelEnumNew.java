package cn.sl.ehub.common.enums;

import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author sl
 * @Date 2026-05-28
 **/
public enum EnergyModelEnumNew {

    INDUSTRIAL_LOAD("工业负荷","VPP"),
    ELECTRIC_HEATING("电采暖","EH"),
    DISTRIBUTED_ENERGY("储能","DES"),
    CHARGING_PILE("充电桩","CP");

    private String name;
    private String code;


    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    EnergyModelEnumNew( String name, String code) {
        this.name = name;
        this.code = code;
    }

    public static EnergyModelEnumNew getByName(String name) {
        EnergyModelEnumNew[] values = EnergyModelEnumNew.values();
        for (EnergyModelEnumNew value : values) {
            if (StringUtils.equals(value.getName(), name)) {
                return value;
            }
        }
        return null;
    }

//    public static List<String> getChannelNoList() {
//        List<String> channelNoList = Lists.newArrayList();
//        EnergyModelEnumNew[] values = EnergyModelEnumNew.values();
//        for (EnergyModelEnumNew value : values) {
//            channelNoList.add(value.getChannelNo());
//        }
//        return channelNoList;
//    }

    public static Map<String,String>  getEnergyMap() {
        Map<String, String> resultMap = new HashMap<>();
        EnergyModelEnumNew[] values = EnergyModelEnumNew.values();
        for (EnergyModelEnumNew value : values) {
            resultMap.put(value.getName(),value.getCode());
        }
        return resultMap;
    }
}
