/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.common.context;

/**
 * 上下文工具类
 */
public class ContextUtils {
    private static final ThreadLocal<InvocationContext> contextMgr = new ThreadLocal<>();

    private ContextUtils() {
    }

    public static InvocationContext getInvocationContext() {
        if (contextMgr.get() == null) {
            setInvocationContext(new InvocationContext());
        }
        return contextMgr.get();
    }

    public static void addContext(String key, String value) {
        getInvocationContext().addContext(key, value);
    }

    public static InvocationContext getAndRemoveInvocationContext() {
        InvocationContext context = contextMgr.get();
        if (context != null) {
            removeInvocationContext();
        }

        return context;
    }

    public static void setInvocationContext(InvocationContext invocationContext) {
        contextMgr.set(invocationContext);
    }

    public static void removeInvocationContext() {
        contextMgr.remove();
    }
}
