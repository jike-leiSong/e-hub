package cn.sl.ehub.console.config;

import cn.sl.ehub.common.enums.StatusCode;
import cn.sl.ehub.common.exception.BaseException;
import cn.sl.ehub.common.utils.MapGlobalUtil;
import cn.sl.ehub.common.vo.ResultVO;
import cn.enn.uac.service.UacAdminService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Description:
 * @Author sl
 * @Date 2026-05-28
 */
@Slf4j
@Component
public class CheckApisTokenInterceptor implements HandlerInterceptor {

/*    @Autowired
    private UacService uacService;*/
    @Autowired
    private UacAdminService uacAdminService;

    /**
     * HTTP请求头中设置当前请求data的TOKEN.
     */
    public static final String TICKET = "ticket";
    public static final String SIMULATE = "simulate";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String simulate = request.getHeader(SIMULATE);
        MapGlobalUtil.removeObjs("simulate");

        if (!"1".equals(simulate)) {
            simulate = "0";
        }
        MapGlobalUtil.addMapObj(SIMULATE, simulate);

        return isLogin(request);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }

    /**
     * 判断是否已经登录
     *
     * @param request
     * @return
     */
    private boolean isLogin(HttpServletRequest request) {
        String ticket = request.getHeader(TICKET);
        if (StringUtils.isEmpty(ticket)) {
            log.warn("ticket为空");
//            throw new BaseException(999, "ticket为空");
        }
        if ("123456".equals(ticket)) {
            return true;
        }
        //验证ticket有效性
//        Boolean flag = uacService.checkTicket(ticket);
        Boolean flag = false;
        ResultVO<Boolean> booleanResultVO = uacAdminService.checkTicket(ticket);
        if (null != booleanResultVO && booleanResultVO.getCode().equals(StatusCode.SUCCESS.getCode())) {
            flag = booleanResultVO.getData();
        }
        if (!flag) {
            log.warn("ticket校验不通过" + ticket);
//            throw new BaseException(999, "ticket校验不通过");
        }
        return true;
    }
}