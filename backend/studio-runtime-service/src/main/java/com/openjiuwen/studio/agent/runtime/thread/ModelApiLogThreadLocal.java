/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.thread;

import com.openjiuwen.studio.agent.runtime.model.ModelApiLog;

public class ModelApiLogThreadLocal {
    private static final ThreadLocal<ModelApiLog> WISE_API_LOGS_THREAD_LOCAL = new ThreadLocal<>();

    public static ModelApiLog getApiLog() {
        return WISE_API_LOGS_THREAD_LOCAL.get();
    }

    public static void setApiLog(ModelApiLog wiseApiLogs) {
        WISE_API_LOGS_THREAD_LOCAL.set(wiseApiLogs);
    }

    public static void clear() {
        WISE_API_LOGS_THREAD_LOCAL.remove();
    }
}
