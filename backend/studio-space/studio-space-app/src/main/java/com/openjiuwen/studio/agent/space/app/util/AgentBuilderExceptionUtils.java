/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.app.util;

import com.alibaba.fastjson.JSONException;
import com.fasterxml.jackson.core.JacksonException;

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

public class AgentBuilderExceptionUtils {

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
}
