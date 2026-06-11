package com.fanneng.requestlog.common;

public class RequestHolder {
    private static final ThreadLocal<RequestContext> contextHolder = new ThreadLocal<>();

    public static RequestContext request() {
        RequestContext context = contextHolder.get();
        if (context == null) {
            context = new RequestContext();
            contextHolder.set(context);
        }
        return context;
    }

    public static void setRequestId(String requestId) {
        request().setRequestId(requestId);
    }

    public static String getRequestId() {
        return request().getRequestId();
    }

    public static void clear() {
        contextHolder.remove();
    }

    public static class RequestContext {
        private String requestId;

        public String getRequestId() {
            return requestId;
        }

        public void setRequestId(String requestId) {
            this.requestId = requestId;
        }
    }
}
