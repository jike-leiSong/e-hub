package cn.sl.ehub.console.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 提前读取 JSON 请求体中的数据范围字段，供认证拦截器校验。
 * 读取后用可重复读取的 request wrapper 继续向 Controller 传递，避免请求体被消费。
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@Component
public class ScopeRequestBodyFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    public ScopeRequestBodyFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    javax.servlet.FilterChain filterChain) throws IOException, javax.servlet.ServletException {
        if (!isJsonRequest(request) || request.getContentLengthLong() == 0) {
            filterChain.doFilter(request, response);
            return;
        }

        byte[] body = StreamUtils.copyToByteArray(request.getInputStream());
        if (body.length == 0) {
            filterChain.doFilter(request, response);
            return;
        }

        CachedBodyRequest wrapped = new CachedBodyRequest(request, body);
        try {
            JsonNode root = objectMapper.readTree(body);
            Set<String> aggregatorIds = new LinkedHashSet<>();
            Set<String> entIds = new LinkedHashSet<>();
            collectScopeValues(root, aggregatorIds, entIds);
            wrapped.setAttribute(AuthInterceptor.SCOPE_AGGREGATOR_ATTRIBUTE, aggregatorIds.toArray(new String[0]));
            wrapped.setAttribute(AuthInterceptor.SCOPE_ENT_ATTRIBUTE, entIds.toArray(new String[0]));
        } catch (Exception ignored) {
            // JSON 格式由 Controller 参数绑定负责返回错误；这里不改变原有错误处理路径。
        }
        filterChain.doFilter(wrapped, response);
    }

    private boolean isJsonRequest(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.toLowerCase().startsWith("application/json");
    }

    private void collectScopeValues(JsonNode node, Set<String> aggregatorIds, Set<String> entIds) {
        if (node == null) {
            return;
        }
        if (node.isObject()) {
            node.fields().forEachRemaining(entry -> {
                String field = entry.getKey();
                JsonNode value = entry.getValue();
                if ("aggregatorId".equals(field) || "aggregator_id".equals(field)) {
                    addValues(value, aggregatorIds);
                } else if ("entId".equals(field) || "ent_id".equals(field)
                        || "entIds".equals(field) || "ent_ids".equals(field)) {
                    addValues(value, entIds);
                }
                collectScopeValues(value, aggregatorIds, entIds);
            });
        } else if (node.isArray()) {
            node.forEach(item -> collectScopeValues(item, aggregatorIds, entIds));
        }
    }

    private void addValues(JsonNode value, Set<String> target) {
        if (value == null || value.isNull()) {
            return;
        }
        if (value.isArray()) {
            value.forEach(item -> addValues(item, target));
            return;
        }
        String text = value.asText(null);
        if (text == null || text.trim().isEmpty()) {
            return;
        }
        for (String item : text.split(",")) {
            if (!item.trim().isEmpty()) {
                target.add(item.trim());
            }
        }
    }

    private static class CachedBodyRequest extends HttpServletRequestWrapper {
        private final byte[] body;

        private CachedBodyRequest(HttpServletRequest request, byte[] body) {
            super(request);
            this.body = body;
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(body);
            return new ServletInputStream() {
                @Override
                public int read() {
                    return inputStream.read();
                }

                @Override
                public int read(byte[] bytes, int offset, int length) {
                    return inputStream.read(bytes, offset, length);
                }

                @Override
                public boolean isFinished() {
                    return inputStream.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener readListener) {
                    // 同步读取即可，不需要异步回调。
                }
            };
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
        }

        @Override
        public int getContentLength() {
            return body.length;
        }

        @Override
        public long getContentLengthLong() {
            return body.length;
        }
    }
}
