/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.utils;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

/**
 * store, acquire, and clear http response header parameter util
 */

public class ResponseUtils {
    private static ThreadLocal<HttpHeaders> THREAD_LOCAL_HEADER = new InheritableThreadLocal<>();

    private static ThreadLocal<HttpStatus> THREAD_LOCAL_STATUS = new InheritableThreadLocal<>();

    /**
     * stores a header parameter.
     *
     * @param key header key
     * @param value header value
     */
    public static void addHeader(String key, String value) {
        if (THREAD_LOCAL_HEADER.get() == null) {
            THREAD_LOCAL_HEADER.set(new HttpHeaders());
        }

        THREAD_LOCAL_HEADER.get().add(key, value);
    }

    /**
     * acquire response header parameter.
     *
     * @return header
     */
    public static HttpHeaders getHeaders() {
        return THREAD_LOCAL_HEADER.get();
    }

    /**
     * acquire response http status.
     *
     * @return status
     */
    public static HttpStatus getResponseCode() {
        return THREAD_LOCAL_STATUS.get();
    }

    /**
     * set http response status
     *
     * @param httpStatus status code
     */
    public static void setResponseCode(HttpStatus httpStatus) {
        THREAD_LOCAL_STATUS.set(httpStatus);
    }

    /**
     * clear current thread header parameter
     */
    public static void clear() {
        if (THREAD_LOCAL_HEADER.get() != null) {
            THREAD_LOCAL_HEADER.remove();
        }

        if (THREAD_LOCAL_STATUS.get() != null) {
            THREAD_LOCAL_STATUS.remove();
        }
    }
}
