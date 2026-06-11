package cn.sl.ehub.common.enums.guangzhou;

/**
 * @Description:
 * @Author sl
 * @Date 2026-05-28
 */
public enum InviteReplyeGuangZhou {

    AGREE("agree", "同意"),
    REFUSE("refuse", "拒绝");

    private final String code;
    private final String desc;

    InviteReplyeGuangZhou(String code, String desc) {
        this.desc = desc;
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public String getCode() {
        return code;
    }
}
