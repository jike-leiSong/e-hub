package cn.sl.ehub.common.enums;

/**
 * @Author sl
 * @Date 2026-05-28
 **/
public enum UserTypeEnums {
    GWZY("AVGRT","国网自营"),
    SHYY("SHYY","社会运营"),
    GYZ("GYZ","公用桩"),
    ZYZ("SHYY","专用桩");

    private String code;

    private String desc;
    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    UserTypeEnums(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
