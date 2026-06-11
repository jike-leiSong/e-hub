package cn.sl.ehub.console.enums;

import lombok.Getter;

/**
 * 资源类型枚举
 *
 * @author sl
 * @date 2026-05-28
 */
@Getter
public enum ResourceTypeEnum {

    /**
     * 电采暖
     */
    ELECTRIC_HEATING(15, "电采暖"),

    /**
     * 工业负荷
     */
    INDUSTRIAL_LOAD(44, "工业负荷");

    private final Integer code;
    private final String name;

    ResourceTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 根据code获取名称
     */
    public static String getNameByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ResourceTypeEnum type : values()) {
            if (type.code.equals(code)) {
                return type.name;
            }
        }
        return null;
    }

    /**
     * 校验code是否有效
     */
    public static boolean isValid(Integer code) {
        if (code == null) {
            return false;
        }
        for (ResourceTypeEnum type : values()) {
            if (type.code.equals(code)) {
                return true;
            }
        }
        return false;
    }
}
