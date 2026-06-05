/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.utils;

import com.alibaba.fastjson.JSONException;
import com.fasterxml.jackson.core.JacksonException;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;

import org.slf4j.Logger;

import java.io.FileNotFoundException;
import java.net.BindException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.jar.JarException;

import javax.naming.InsufficientResourcesException;

/**
 * WorkflowAdapterExceptionUtils
 *
 * @Date: 2024/5/14 10:18
 * @Description: workflow-adapter异常处理工具
 */


public class McpServiceExceptionUtils {

    /**
     * 打印异常堆栈信息
     *
     * @param logger     logger
     * @param exception  exception
     * @param logContent logContent
     */
    public static void printException(Logger logger, Exception exception, String logContent) {
        try {
            Set<Class<? extends Exception>> sensitiveExceptions = new HashSet<>();
            sensitiveExceptions.add(FileNotFoundException.class);
            sensitiveExceptions.add(JarException.class);
            sensitiveExceptions.add(BindException.class);
            sensitiveExceptions.add(JacksonException.class);
            sensitiveExceptions.add(MissingResourceException.class);
            sensitiveExceptions.add(ConcurrentModificationException.class);
            sensitiveExceptions.add(JSONException.class);
            sensitiveExceptions.add(InsufficientResourcesException.class);
            sensitiveExceptions.add(SQLException.class);
            sensitiveExceptions.add(URISyntaxException.class);
            boolean isSensitiveException = sensitiveExceptions.stream().anyMatch(clazz -> clazz.isInstance(exception));

            if (isSensitiveException) {
                // 敏感异常，不打印堆栈
                logger.error(logContent + ", occur exception {}", exception.getClass());
            } else {
                // 非敏感异常，打印堆栈
                logger.error(logContent + ", occur exception.", exception);
            }
        } catch (Exception e) {
            // 其他未知异常，打印堆栈
            logger.error(logContent + ", occur exception.", e);
        }
    }

    /**
     * throwRemoteCallException
     *
     * @param type type
     * @throws AgentStudioException agentStudioException
     */
    public static void throwRemoteCallException(String type, String errorMsg) throws AgentStudioException {
        throw new AgentStudioException(StudioError.REMOTE_CALL_ERROR, type);
    }

    /**
     * throwUserNoPermissionError
     */
    public static void throwUserNoPermissionError() throws AgentStudioException {
        throw new AgentStudioException(StudioError.USER_HAS_NO_PERMISSION);
    }

    /**
     * 抛出缺失参数异常
     *
     * @param parameterName 参数名称
     * @throws AgentStudioException agentStudioException
     */
    public static void throwMissingParameterError(String parameterName) throws AgentStudioException {
        throw new AgentStudioException(StudioError.MISSING_PARAM, parameterName);
    }
}
