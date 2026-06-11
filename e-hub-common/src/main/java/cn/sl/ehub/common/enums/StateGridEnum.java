package cn.sl.ehub.common.enums;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;

/**
 * @Description:
 * @Author sl
 * @Date 2026-05-28
 */
public enum StateGridEnum {

    HUABEI("HUABEI", "华北电网"),
    HUABEI_DELIVERY("HUABEI_DELIVERY", "华北电网申报"),
    GUANGZHOU_DELIVERY("GUANGZHOU_DELIVERY", "广州电网申报"),
    GUANGZHOU("GUANGZHOU", "广州电网");

    private String code;
    private String name;


    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    StateGridEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static StateGridEnum getEnumByCode(String code) {
        StateGridEnum[] values = StateGridEnum.values();
        for (StateGridEnum value : values) {
            if (StringUtils.equals(code, value.code)) {
                return value;
            }
        }
        return null;
    }

    public static Set<String> getCodes(){
        StateGridEnum[] values = StateGridEnum.values();
        Set<String> codes = Sets.newHashSet();
        for (StateGridEnum value : values) {
            codes.add(value.code);
        }
        return codes;
    }
}