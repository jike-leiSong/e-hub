package cn.sl.ehub.console.auth;

import cn.sl.ehub.common.enums.StatusCode;
import cn.sl.ehub.common.exception.BaseException;
import cn.sl.ehub.common.vo.ResultVO;
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

    static final String SCOPE_AGGREGATOR_ATTRIBUTE = "ehub.scope.aggregatorIds";
    static final String SCOPE_ENT_ATTRIBUTE = "ehub.scope.entIds";

    private final AuthService authService;
    private final ConsolePermissionService permissionService;
    private final LoadAggregationScopeService loadScopeService;
    private final ObjectMapper objectMapper;

    public AuthInterceptor(AuthService authService,
                           ConsolePermissionService permissionService,
                           LoadAggregationScopeService loadScopeService,
                           ObjectMapper objectMapper) {
        this.authService = authService;
        this.permissionService = permissionService;
        this.loadScopeService = loadScopeService;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        try {
            String token = extractToken(request);
            AuthUser user = authService.verify(token);
            validateApiPermission(request, user);
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
        String ticket = request.getHeader("ticket");
        if (StringUtils.isNotBlank(ticket)) {
            return StringUtils.trim(ticket);
        }
        token = request.getParameter("token");
        if (StringUtils.isNotBlank(token)) {
            return StringUtils.trim(token);
        }
        return StringUtils.trim(request.getParameter("ticket"));
    }

    private void validateDataScope(HttpServletRequest request, AuthUser user) {
        loadScopeService.validateRequestScope(
                user,
                getValues(request, "aggregatorId", "aggregator_id"),
                getValues(request, "entId", "ent_id", "entIds", "ent_ids")
        );
    }

    private void validateApiPermission(HttpServletRequest request, AuthUser user) {
        if (!permissionService.hasRequestPermission(request, user)) {
            throwNoPermission();
        }
    }

    private String[] getValues(HttpServletRequest request, String... names) {
        StringBuilder values = new StringBuilder();
        if (containsName(names, "aggregatorId", "aggregator_id")) {
            appendValue(values, request.getAttribute(SCOPE_AGGREGATOR_ATTRIBUTE));
        }
        if (containsName(names, "entId", "ent_id", "entIds", "ent_ids")) {
            appendValue(values, request.getAttribute(SCOPE_ENT_ATTRIBUTE));
        }
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

    private void appendValue(StringBuilder values, Object value) {
        if (value == null) {
            return;
        }
        if (value instanceof String[]) {
            for (String item : (String[]) value) {
                appendValue(values, item);
            }
            return;
        }
        appendValue(values, String.valueOf(value));
    }

    private boolean containsName(String[] names, String... candidates) {
        for (String name : names) {
            for (String candidate : candidates) {
                if (StringUtils.equals(name, candidate)) {
                    return true;
                }
            }
        }
        return false;
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
