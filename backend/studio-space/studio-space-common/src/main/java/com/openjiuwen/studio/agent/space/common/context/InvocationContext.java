/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.common.context;

import com.openjiuwen.studio.agent.space.common.model.ResponseStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * 功能描述
 */
public class InvocationContext {
    protected ResponseStatus httpStatus;

    protected final Map<String, String> context = new HashMap<>();

    protected final Map<String, Object> localContext = new HashMap<>();

    public InvocationContext() {
        this.httpStatus = ResponseStatus.OK;
    }

    public Map<String, String> getContext() {
        return this.context;
    }

    public void setContext(Map<String, String> context) {
        this.context.clear();
        this.addContext(context);
    }

    public void addContext(String key, String value) {
        this.context.put(key, value);
    }

    public void addLocalContext(String key, Object value) {
        this.localContext.put(key, value);
    }

    public Object getLocalContext(String key) {
        return this.localContext.get(key);
    }

    public String getContext(String key) {
        return this.context.get(key);
    }

    public void addContext(InvocationContext otherContext) {
        this.addContext(otherContext.getContext());
    }

    public void addContext(Map<String, String> otherContext) {
        if (otherContext != null) {
            this.context.putAll(otherContext);
        }
    }

    public void mergeContext(InvocationContext otherContext) {
        this.mergeContext(otherContext.getContext());
    }

    public void mergeContext(Map<String, String> otherContext) {
        if (otherContext != null) {
            this.context.putAll(otherContext);
        }
    }
}
