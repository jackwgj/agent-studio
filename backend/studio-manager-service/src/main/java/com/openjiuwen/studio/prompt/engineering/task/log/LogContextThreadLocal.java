/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.task.log;

/**
 * 日志上下文的本地线程.
 *
 */
public class LogContextThreadLocal extends ThreadLocal<LogContext> {
    /**
     * {@inheritDoc}
     */
    protected LogContext initialValue() {
        return new LogContext();
    }
}
