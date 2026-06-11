package cn.sl.ehub.common.config;

import cn.sl.ehub.common.vo.ResultVO;
import cn.sl.ehub.common.vo.ResultWithPageVO;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.ExtendedServletRequestDataBinder;

import javax.servlet.http.HttpServletResponseWrapper;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author sl
 * @Description:
 * @Date 2026-05-28
 */
@Aspect
@Component
@Order(1)
public class HandlerControllerLoggerAspect {
    private static final Logger log = LoggerFactory.getLogger(HandlerControllerLoggerAspect.class);

    /**
     * 创建Pointcut表示式，表示所有controller请求
     */
    @Pointcut("execution(* cn..*.rest..*(..))|| execution(* cn..*.controller..*(..))")
    private void controllerAspect() {
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
            String[] argNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames(); // 参数名
            Map<String, Object> paramMap = new HashMap<String, Object>();
            for (int i = 0; i < objs.length; i++) {
                if (!(objs[i] instanceof ExtendedServletRequestDataBinder) && !(objs[i] instanceof HttpServletResponseWrapper)) {
                    paramMap.put(argNames[i], objs[i]);
                }
            }
            if (paramMap.size() > 0) {
                paramMap.remove("request");
                paramMap.remove("file");
                paramMap.remove("multipartFile");
                paramMap.remove("response");
                paramMap.remove("results");
                log.info("\nRequestId：{}\n方法：{}\n参数：{}", requestId, joinPoint.getSignature(), JSONObject.toJSONString(paramMap));
            }
        } catch (Exception e) {
            log.info("RequestId：{}\nAOP methodBefore：{}", requestId, e);
        }
    }

    /**
     * <请求返回内容><功能具体实现>
     *
     * @param res
     * @param requestId
     * @return void
     * @author sl
     * @since 2020-04-16 19:31:43
     */
    public ResultVO methodAfterReturing(Object res, String requestId, Long beginTime) {
        ResultVO r;
        if (res instanceof ResultWithPageVO) {
            r = new ResultWithPageVO();
        } else {
            r = new ResultVO();
        }
        try {
            if (res != null) {
                Long endTime = System.currentTimeMillis();
                log.info("RequestId：{}\n请求时间：{}\nResponse内容：{}", requestId, (float) (endTime - beginTime) / 1000 + "s", JSONObject.toJSON(res));
                if (r instanceof ResultWithPageVO) {
                    r = JSONObject.parseObject(JSON.toJSONString(res, SerializerFeature.WriteMapNullValue), ResultWithPageVO.class);
                } else if (r instanceof ResultVO) {
                    r = JSONObject.parseObject(JSON.toJSONString(res, SerializerFeature.WriteMapNullValue), ResultVO.class);
                }
                r.setRequestId(requestId);
                return r;
            }
        } catch (Exception e) {
            log.info("RequestId：{}\nAOP methodAfterReturing：{}", requestId, e);
        }
        return r;
    }

    /**
     * <请求返回内容-object><功能具体实现>
     *
     * @param res
     * @param requestId
     * @param beginTime
     * @return java.lang.Object
     * @author sl
     * @since 2020-04-28 15:27:58
     */
    private Object methodAfterReturingObject(Object res, String requestId, Long beginTime) {
        try {
            if (res != null) {
                Long endTime = System.currentTimeMillis();
                log.info("RequestId：{}\n请求时间：{}\nResponse内容：{}", requestId, (float) (endTime - beginTime) / 1000 + "s", JSONObject.toJSON(res));
                return res;
            }
        } catch (Exception e) {
            log.info("RequestId：{}\nAOP methodAfterReturing：{}", requestId, e);
        }
        return res;
    }

}

