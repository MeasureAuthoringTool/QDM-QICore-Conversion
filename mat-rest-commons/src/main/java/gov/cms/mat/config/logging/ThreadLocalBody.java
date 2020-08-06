package gov.cms.mat.config.logging;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ThreadLocalBody {
    private static final ThreadLocal<String> threadLocalValue = new ThreadLocal<>();

    public static String getBody() {
        return threadLocalValue.get();
    }

    public static void setBody(Object body) {
        if (body != null) {
            threadLocalValue.set(body.toString());
        } else {
            log.debug("Body is null, not set in ThreadLocal");
        }
    }
}
