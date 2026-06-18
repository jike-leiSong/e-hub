package cn.sl.ehub.console.auth;

import cn.sl.ehub.console.auth.model.AuthLoginReq;
import cn.sl.ehub.console.auth.model.AuthLoginResp;

public interface AuthService {

    AuthLoginResp login(AuthLoginReq req);

    AuthUser verify(String token);

    void logout(String token);
}
