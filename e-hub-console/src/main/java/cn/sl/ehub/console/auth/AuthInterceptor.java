package cn.sl.ehub.console.auth;

import cn.sl.ehub.common.enums.StatusCode;
import cn.sl.ehub.common.exception.BaseException;
import cn.sl.ehub.common.vo.ResultVO;
import cn.sl.ehub.service.mapper.AggregatorEntMapper;
import cn.sl.ehub.service.vo.AggregatorEnt;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final String USER_TYPE_PLATFORM = "PLATFORM";
    private static final String USER_TYPE_AGGREGATOR = "AGGREGATOR";
    private static final String USER_TYPE_ENT = "ENT";

    private final AuthService authService;
    private final ObjectMapper objectMapper;
    private final AggregatorEntMapper aggregatorEntMapper;

    public AuthInterceptor(AuthService authService, ObjectMapper objectMapper, AggregatorEntMapper aggregatorEntMapper) {
        this.authService = authService;
        this.objectMapper = objectMapper;
        this.aggregatorEntMapper = aggregatorEntMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        try {
            String token = extractToken(request);
            AuthUser user = authService.verify(token);
            validateDataScope(request, user);
            AuthContext.set(user);
            return true;
        } catch (BaseException e) {
            AuthContext.clear();
            writeFailure(response, e.getCode(), e.getMessage());
            return false;
        } catch (Exception e) {
            AuthContext.clear();
            writeFailure(response, StatusCode.D.getCode(), StatusCode.D.getMsg());
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthContext.clear();
    }

    public String extractToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (StringUtils.startsWithIgnoreCase(authorization, "Bearer ")) {
            return StringUtils.trim(StringUtils.substring(authorization, 7));
        }
        String token = request.getHeader("token");
        if (StringUtils.isNotBlank(token)) {
            return StringUtils.trim(token);
        }
        return StringUtils.trim(request.getParameter("token"));
    }

    private void validateDataScope(HttpServletRequest request, AuthUser user) {
        if (user == null || isPlatform(user)) {
            return;
        }
        if (isAggregator(user)) {
            validateEqualIfPresent(user.getAggregatorId(), getValues(request, "aggregatorId", "aggregator_id"));
            validateAggregatorEntScope(user.getAggregatorId(), getValues(request, "entId", "ent_id", "entIds", "ent_ids"));
            return;
        }
        if (isEnt(user)) {
            validateEqualIfPresent(user.getAggregatorId(), getValues(request, "aggregatorId", "aggregator_id"));
            validateEqualIfPresent(user.getEntId(), getValues(request, "entId", "ent_id", "entIds", "ent_ids"));
            return;
        }
        throwNoPermission();
    }

    private void validateAggregatorEntScope(String aggregatorId, String[] entIds) {
        for (String entId : entIds) {
            if (StringUtils.isBlank(entId)) {
                continue;
            }
            if (StringUtils.isBlank(aggregatorId)) {
                throwNoPermission();
            }
            AggregatorEnt query = new AggregatorEnt();
            query.setAggregatorId(aggregatorId);
            query.setEntId(entId);
            if (aggregatorEntMapper.selectCount(query) <= 0) {
                throwNoPermission();
            }
        }
    }

    private void validateEqualIfPresent(String expected, String[] actualValues) {
        for (String actual : actualValues) {
            if (StringUtils.isBlank(actual)) {
                continue;
            }
            if (StringUtils.isBlank(expected) || !StringUtils.equals(expected, actual)) {
                throwNoPermission();
            }
        }
    }

    private String[] getValues(HttpServletRequest request, String... names) {
        StringBuilder values = new StringBuilder();
        for (String name : names) {
            appendValue(values, request.getHeader(name));
            String[] parameterValues = request.getParameterValues(name);
            if (parameterValues != null) {
                for (String parameterValue : parameterValues) {
                    appendValue(values, parameterValue);
                }
            }
        }
        if (values.length() == 0) {
            return new String[0];
        }
        String[] splitValues = StringUtils.split(values.toString(), ',');
        for (int i = 0; i < splitValues.length; i++) {
            splitValues[i] = StringUtils.trim(splitValues[i]);
        }
        return splitValues;
    }

    private void appendValue(StringBuilder values, String value) {
        if (StringUtils.isBlank(value)) {
            return;
        }
        if (values.length() > 0) {
            values.append(',');
        }
        values.append(StringUtils.trim(value));
    }

    private boolean isPlatform(AuthUser user) {
        return StringUtils.equalsIgnoreCase(USER_TYPE_PLATFORM, user.getUserType());
    }

    private boolean isAggregator(AuthUser user) {
        return StringUtils.equalsIgnoreCase(USER_TYPE_AGGREGATOR, user.getUserType());
    }

    private boolean isEnt(AuthUser user) {
        return StringUtils.equalsIgnoreCase(USER_TYPE_ENT, user.getUserType());
    }

    private void throwNoPermission() {
        throw new BaseException(StatusCode.U.getCode(), StatusCode.U.getMsg());
    }

    private void writeFailure(HttpServletResponse response, Integer code, String msg) throws Exception {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(ResultVO.fail(code, msg)));
    }
}
