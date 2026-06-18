package cn.sl.ehub.console.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthUser {

    private String userId;

    private String username;

    private String displayName;

    private String userType;

    private String aggregatorId;

    private String entId;
}
