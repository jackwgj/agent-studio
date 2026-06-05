/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.task.log;

/**
 * 本地线程缓存的日志上下文管理类.
 *
 */
public class LogContextLocalMgr {
    /**
     * log context.
     */
    public static final LogContextThreadLocal LOGCONTEXT = new LogContextThreadLocal();

    /**
     * 清空数据
     */
    public static void clear() {
        LOGCONTEXT.get().clear();
    }

    /**
     * 设置traceID
     *
     * @param traceID traceID
     */
    public static void setTraceID(String traceID) {
        LogContext logContextThreadLocal = LOGCONTEXT.get();
        logContextThreadLocal.setTraceID(traceID);
    }

}

