package cn.sl.ehub.console.grid.config;

import cn.sl.ehub.common.vo.ResultVO;
import cn.sl.ehub.service.vo.ResultWithPageVO;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.fanneng.requestlog.common.RequestHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.ExtendedServletRequestDataBinder;

import javax.servlet.http.HttpServletResponseWrapper;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @Description:
 * @Author sl
 * @Date 2026-05-28
 */
@Aspect
@Component
@Order(0)
@Slf4j
public class WsLoggerAspect {

    /**
     * 创建Pointcut表示式，表示所有service下的请求请求
     */
    @Pointcut("execution(* cn.sl.ehub.upstream.issue.service..*(..))")
    public void loggerPoint() {

    }

    /**
     * <请求method前打印内容><功能具体实现>
     *
     * @param pjp
     * @return
     * @throws Throwable
     */
    @Around(value = "loggerPoint()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        ResultVO r = null;
        // 开始时间
        Instant begin = Instant.now();
        // requestId
        String requestId = RequestHolder.request().getRequestId();
        if (StringUtils.isBlank(requestId)){
            requestId = UUID.randomUUID().toString();
            RequestHolder.request().setRequestId(requestId);
        }
        requestId = RequestHolder.request().getRequestId();

        // method输入参数
        methodBefore(pjp, requestId);
        try {
            Object proceed = pjp.proceed();
            if (proceed instanceof ResultVO) {
                r = methodAfterReturing(proceed, requestId, begin);
            } else {
                return methodAfterReturingObject(proceed, requestId, begin);
            }
        } catch (Exception e) {
            log.warn("RequestId：{}\nResponse异常内容：{}", requestId, e);
            throw e;
        }
        return r;
    }

    /**
     * <method输入参数><功能具体实现>
     *
     * @param joinPoint
     * @param requestId
     * @return void
     * @author sl
     * @since 2020-04-16 19:31:02
     */
    public void methodBefore(JoinPoint joinPoint, String requestId) {
        // 打印请求内容
        try {
            // 下面两个数组中，参数值和参数名的个数和位置是一一对应的。
            Object[] objs = joinPoint.getArgs();
            String[] argNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
            Map<String, Object> paramMap = new HashMap<String, Object>();
            for (int i = 0; i < objs.length; i++) {
                if (!(objs[i] instanceof ExtendedServletRequestDataBinder) && !(objs[i] instanceof HttpServletResponseWrapper)) {
                    paramMap.put(argNames[i], objs[i]);
                }
            }
            if (paramMap.size() > 0) {
                paramMap.remove("request");
                paramMap.remove("file");
                paramMap.remove("response");
                log.info("\nRequestId：{}\n方法：{}\n参数：{}", requestId, joinPoint.getSignature(), JSONObject.toJSONString(paramMap));
            }
        } catch (Exception e) {
            log.warn("RequestId：{}\nAOP methodBefore：{}", requestId, e);
        }
    }

    /**
     * <请求返回内容><功能具体实现>
     *
     * @param res
     * @param requestId
     * @param begin
     * @return
     */
    public ResultVO methodAfterReturing(Object res, String requestId, Instant begin) {
        ResultVO r;
        if (res instanceof ResultWithPageVO) {
            ResultWithPageVO pageRes = (ResultWithPageVO) res;
            r = new ResultVO();
            r.setCode(pageRes.getCode());
            r.setMsg(pageRes.getMsg());
            r.setData(pageRes.getData());
        } else if (res instanceof ResultVO) {
            r = (ResultVO) res;
        } else {
            r = new ResultVO();
        }
        try {
            if (res != null) {
                Instant end = Instant.now();
                log.info("RequestId：{}\n请求时间：{}\nResponse内容：{}", requestId, Duration.between(begin, end).toMillis() + "ms", JSONObject.toJSON(res));
                r.setRequestId(requestId);
                return r;
            }
        } catch (Exception e) {
            log.warn("RequestId：{}\nAOP methodAfterReturing：{}", requestId, e);
        }
        return r;
    }

    /**
     * <请求返回内容-object><功能具体实现>
     *
     * @param res
     * @param requestId
     * @param begin
     * @return
     */
    private Object methodAfterReturingObject(Object res, String requestId, Instant begin) {
        try {
            if (res != null) {
                Instant end = Instant.now();
                log.info("RequestId：{}\n请求时间：{}\nResponse内容：{}", requestId, Duration.between(begin, end).toMillis() + "ms", JSONObject.toJSON(res));
                return res;
            }
        } catch (Exception e) {
            log.warn("RequestId：{}\nAOP methodAfterReturing：{}", requestId, e);
        }
        return res;
    }
}
