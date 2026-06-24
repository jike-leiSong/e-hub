package cn.sl.ehub.console.auth;

import cn.sl.ehub.common.vo.ResultVO;
import cn.sl.ehub.console.auth.model.AuthLoginReq;
import cn.sl.ehub.console.auth.model.AuthLoginResp;
import cn.sl.ehub.console.auth.model.AuthUserInfoResp;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/auth")
@Api(tags = "登录认证")
public class AuthController {

    private final AuthService authService;
    private final AuthInterceptor authInterceptor;
    private final ConsolePermissionService permissionService;

    public AuthController(AuthService authService,
                          AuthInterceptor authInterceptor,
                          ConsolePermissionService permissionService) {
        this.authService = authService;
        this.authInterceptor = authInterceptor;
        this.permissionService = permissionService;
    }

    @ApiOperation("登录")
    @PostMapping("/login")
    public ResultVO<AuthLoginResp> login(@RequestBody AuthLoginReq req) {
        return ResultVO.success(authService.login(req));
    }

    @ApiOperation("退出登录")
    @PostMapping("/logout")
    public ResultVO<Boolean> logout(HttpServletRequest request) {
        authService.logout(authInterceptor.extractToken(request));
        return ResultVO.success(true);
    }

    @ApiOperation("当前登录用户")
    @GetMapping("/me")
    public ResultVO<AuthUserInfoResp> me() {
        return ResultVO.success(permissionService.buildUserInfo(AuthContext.get()));
    }
}
