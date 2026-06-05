/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.task.log;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class LogContext {
    /**
     * traceID.
     */
    @Getter
    private String traceID;

    /**
     * requestId.
     */
    @Getter
    private String requestId;

    /**
     * innerRequestId.
     */
    @Getter
    private String innerRequestId;

    /**
     * 前缀.
     */
    @Getter
    private String prefix;

    /**
     * 扩展参数.
     */
    private final Map<String, Object> extInfos = new HashMap<>();

    /**
     * 清空数据
     */
    public void clear() {
        traceID = null;
        requestId = null;
        innerRequestId = null;
        prefix = null;
        extInfos.clear();
    }

    /**
     * 设置traceID
     *
     * @param traceID traceID
     */
    public void setTraceID(String traceID) {
        this.traceID = traceID;
    }

}

