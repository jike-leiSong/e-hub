package cn.sl.ehub.upstream.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * API Token校验拦截器（临时简化版）
 * 原：依赖uac-service进行token校验
 *
 * 说明：暂时只做基本校验，后续需要实现JWT认证
 * TODO: 实现JWT token校验
 *
 * @Author sl
 * @Date 2026-05-28
 */
@Slf4j
@Component
public class CheckApisTokenInterceptor implements HandlerInterceptor {

    /**
     * HTTP请求头中设置当前请求data的TOKEN.
     */
    public static final String TICKET = "ticket";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        return isLogin(request);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }

    /**
     * 判断是否已经登录（临时简化版）
     * TODO: 实现真实的JWT token校验
     *
     * @param request
     * @return
     */
    private boolean isLogin(HttpServletRequest request) {
        String ticket = request.getHeader(TICKET);
        if (StringUtils.isEmpty(ticket)) {
            log.warn("ticket为空，暂时放行");
            return true;  // 暂时放行
        }

        // 临时：固定token可以通过
        if ("123456".equals(ticket)) {
            return true;
        }

        // TODO: 实现JWT token校验
        log.warn("ticket校验暂未实现，放行 - ticket: {}", ticket);
        return true;  // 暂时放行
    }
}
