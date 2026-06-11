package cn.enn.uac.resp;

import lombok.Data;

/**
 * UAC管理员用户信息响应
 * @author sl
 * @date 2026-06-04
 */
@Data
public class UacAdminUserInfoResp {
    private String userId;
    private String userName;
    private String phone;
    private String email;
    private String entId;
    private String entName;
}
