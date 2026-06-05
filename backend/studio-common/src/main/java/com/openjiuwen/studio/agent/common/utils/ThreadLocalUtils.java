/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.utils;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

/**
 * 线程变量工具
 *
 */
@Slf4j
public class ThreadLocalUtils {
    private static final ThreadLocal<String> THREAD_LOCAL_WORKSPACE_ID = new ThreadLocal<>();

    private static final ThreadLocal<String> THREAD_LOCAL_WORKSPACE_NAME = new ThreadLocal<>();

    public static void setWorkspaceId(String workspaceId) {
        THREAD_LOCAL_WORKSPACE_ID.set(workspaceId);
    }

    public static void setWorkspaceName(String workspaceName) {
        THREAD_LOCAL_WORKSPACE_NAME.set(workspaceName);
    }

    public static String getWorkspaceId() {
        if (StringUtils.isEmpty(THREAD_LOCAL_WORKSPACE_ID.get())) {
            log.warn("workspaceId is null, use default");
            return "default";
        }
        return THREAD_LOCAL_WORKSPACE_ID.get();
    }

    public static String getWorkspaceName() {
        if (StringUtils.isEmpty(THREAD_LOCAL_WORKSPACE_NAME.get())) {
            log.warn("workspaceName is null, use defaultName");
            return "defaultName";
        }
        return THREAD_LOCAL_WORKSPACE_NAME.get();
    }

    public static void clearWorkspaceId() {
        THREAD_LOCAL_WORKSPACE_ID.remove();
    }
}
