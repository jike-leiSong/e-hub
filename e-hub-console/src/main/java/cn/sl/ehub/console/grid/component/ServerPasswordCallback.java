package cn.sl.ehub.console.grid.component;

import org.springframework.stereotype.Component;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;


/**
 * @Description: 设置回调密码
 * @Author sl
 * @Date 2026-05-28
 */
@Component
public class ServerPasswordCallback implements CallbackHandler {

    @Override
    public void handle(Callback[] callbacks) {
//        WSPasswordCallback callback = (WSPasswordCallback) callbacks[0];
//        callback.setPassword("Huabei.zdh2019");
    }
}