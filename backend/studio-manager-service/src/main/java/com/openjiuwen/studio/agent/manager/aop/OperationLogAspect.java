/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.aop;

import com.alibaba.fastjson2.JSON;
import com.openjiuwen.studio.agent.common.annotation.OperationLog;
import com.openjiuwen.studio.agent.common.dto.AuditLogEntry;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * 操作审计日志切面。
 *
 * <p>拦截所有标注了 {@link OperationLog} 的方法，将审计信息以 JSON 格式
 * 写入独立的审计日志文件（由 log4j2.xml 中的 AUDIT_LOGGER 配置）。
 *
 * <p>支持三种 resourceId 配置格式：
 * 1. 参数名字：如 "agentId" 表示从 agentId 参数获取
 * 2. 对象属性路径：如 "body.id" 表示从 body 参数的 id 属性获取
 * 3. 数组下标访问：如 "body.ids[0]" 表示从 body 参数的 ids 数组获取第一个元素
 */
@Aspect
@Component
public class OperationLogAspect {

    private static final String AUDIT_LOGGER_NAME = "AUDIT_LOGGER";

    private static final Logger AUDIT_LOGGER = LogManager.getLogger(AUDIT_LOGGER_NAME);

    private static final Logger LOGGER = LogManager.getLogger(OperationLogAspect.class);

    @Value("${studio.operationLog.switch:false}")
    private boolean operationLogSwitch;

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        if (!operationLogSwitch) {
            return joinPoint.proceed();
        }

        Object result = null;
        Throwable throwable = null;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable ex) {
            throwable = ex;
            throw ex;
        } finally {
            writeAuditLog(joinPoint, operationLog, throwable);
        }
    }

    private void writeAuditLog(ProceedingJoinPoint joinPoint, OperationLog operationLog, Throwable throwable) {
        try {
            AuditLogEntry.AuditLogEntryBuilder builder = AuditLogEntry.builder()
                .timestamp(Instant.now().toString())
                .operationType(operationLog.operationType().getValue())
                .success(throwable == null)
                .errorMessage(throwable != null ? throwable.getMessage() : null);
            builder.userId(RequestContextUtils.getRequestUserId());
            builder.userName(RequestContextUtils.getRequestUserName());

            // TraceId from MDC or ThreadContext
            String traceId = ThreadContext.get("request-id");
            if (traceId != null) {
                builder.traceId(traceId);
            }

            // 方法签名信息
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            builder.method(method.getName());

            // 资源类型（优先使用注解值，否则从类名推断）
            String resourceType = operationLog.resourceType();
            if (resourceType == null || resourceType.isEmpty()) {
                resourceType = inferResourceType(signature.getDeclaringType().getSimpleName());
            }
            builder.resourceType(resourceType);

            // 描述
            String description = operationLog.description();
            if (description != null && !description.isEmpty()) {
                builder.description(description);
            } else {
                builder.description(String.format("%s %s", operationLog.operationType().getValue(), resourceType));
            }

            // 资源ID（从方法参数中提取）
            String resourceId = extractResourceId(joinPoint, operationLog.resourceId());
            if (resourceId != null) {
                builder.resourceId(resourceId);
            }

            // 资源名称（从方法参数中提取）
            String resourceName = extractResourceId(joinPoint, operationLog.resourceName());
            builder.resourceName(resourceName != null ? resourceName : "");

            AuditLogEntry entry = builder.build();
            String json = JSON.toJSONString(entry);
            AUDIT_LOGGER.info(json);

        } catch (Exception e) {
            LOGGER.warn("Failed to write audit log, falling back to main log", e);
        }
    }

    private Map<String, Object> buildParamsMap(ProceedingJoinPoint joinPoint) {
        Map<String, Object> params = new HashMap<>();
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String[] paramNames = signature.getParameterNames();
            Object[] args = joinPoint.getArgs();

            if (paramNames != null && args != null) {
                for (int i = 0; i < paramNames.length; i++) {
                    Object arg = args[i];
                    if (arg != null) {
                        params.put(paramNames[i], sanitizeValue(arg));
                    }
                }
            }
        } catch (Exception ignored) {
            // ignore
        }
        return params;
    }

    /**
     * 根据 resourceId 提取资源ID。
     * 支持三种格式：
     * 1. 参数名字：如 "agentId" 表示从 agentId 参数获取
     * 2. 对象属性路径：如 "body.id" 表示从 body 参数的 id 属性获取
     * 3. 数组下标访问：如 "body.ids[0]" 表示从 body 参数的 ids 数组获取第一个元素
     */
    private String extractResourceId(ProceedingJoinPoint joinPoint, String resourceId) {
        if (resourceId == null || resourceId.isEmpty()) {
            return null;
        }

        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String[] paramNames = signature.getParameterNames();
            Object[] args = joinPoint.getArgs();

            if (paramNames == null || args == null) {
                return null;
            }

            // 找到参数索引
            String paramName;
            String propertyPath;

            if (resourceId.contains(".")) {
                // 对象属性路径，如 "body.id"
                String[] parts = resourceId.split("\\.", 2);
                paramName = parts[0];
                propertyPath = parts[1];
            } else {
                // 纯参数名，如 "agentId"
                paramName = resourceId;
                propertyPath = null;
            }

            // 找到匹配的参数
            int paramIndex = -1;
            for (int i = 0; i < paramNames.length; i++) {
                if (paramNames[i].equals(paramName)) {
                    paramIndex = i;
                    break;
                }
            }

            if (paramIndex < 0 || paramIndex >= args.length || args[paramIndex] == null) {
                return null;
            }

            if (propertyPath == null || propertyPath.isEmpty()) {
                // 直接返回参数值
                return String.valueOf(args[paramIndex]);
            } else {
                // 从对象属性提取
                return extractPropertyValue(args[paramIndex], propertyPath);
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to write audit log when extractResourceId, falling back to main log", e);
        }
        return null;
    }

    /**
     * 从对象中提取属性值，支持嵌套属性路径和数组下标访问。
     * 支持格式：
     * - "id" - 直接获取 id 属性
     * - "body.id" - 先获取 body，再获取其 id 属性
     * - "ids[0]" - 从 ids 数组获取第一个元素
     * - "body.ids[0]" - 从 body 的 ids 数组获取第一个元素
     */
    private String extractPropertyValue(Object obj, String propertyPath) {
        if (obj == null || propertyPath == null || propertyPath.isEmpty()) {
            return null;
        }

        try {
            // 处理数组下标访问，如 "ids[0]" 或 "body.ids[0]"
            if (propertyPath.contains("[")) {
                int bracketIndex = propertyPath.indexOf('[');
                String arrayProperty = propertyPath.substring(0, bracketIndex);
                int arrayIndex = Integer.parseInt(propertyPath.substring(bracketIndex + 1, propertyPath.indexOf(']')));
                String remainingPath = propertyPath.substring(propertyPath.indexOf(']') + 1);

                // 获取数组
                Object array = getPropertyValue(obj, arrayProperty);
                if (array == null) {
                    return null;
                }

                // 获取数组元素
                Object element = null;
                if (array instanceof java.util.List) {
                    java.util.List<?> list = (java.util.List<?>) array;
                    if (arrayIndex >= 0 && arrayIndex < list.size()) {
                        element = list.get(arrayIndex);
                    }
                } else if (array.getClass().isArray()) {
                    Object[] arr = (Object[]) array;
                    if (arrayIndex >= 0 && arrayIndex < arr.length) {
                        element = arr[arrayIndex];
                    }
                }

                // 如果还有剩余路径，继续递归
                if (remainingPath.isEmpty()) {
                    return element != null ? String.valueOf(element) : null;
                }
                return extractPropertyValue(element, remainingPath.substring(1));
            }
            // 普通属性访问
            else {
                Object value = getPropertyValue(obj, propertyPath);
                return value != null ? String.valueOf(value) : null;
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to write audit log when extractPropertyValue, falling back to main log", e);
            return null;
        }
    }

    /**
     * 获取对象的属性值
     */
    private Object getPropertyValue(Object obj, String propertyName) {
        if (obj == null || propertyName == null || propertyName.isEmpty()) {
            return null;
        }

        try {
            // 尝试获取 getter 方法
            String getterName = "get" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
            try {
                Method getter = obj.getClass().getMethod(getterName);
                return getter.invoke(obj);
            } catch (NoSuchMethodException e) {
                // 尝试直接访问字段
                try {
                    java.lang.reflect.Field field = obj.getClass().getField(propertyName);
                    return field.get(obj);
                } catch (NoSuchFieldException ex) {
                    LOGGER.warn(
                        "Failed to write audit log when getPropertyValue with reflect, falling back to main log", e);
                    return null;
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to write audit log when getPropertyValue, falling back to main log", e);
            return null;
        }
    }

    private Object sanitizeValue(Object value) {
        if (value == null) {
            return null;
        }
        String className = value.getClass().getName();
        if (className.startsWith("com.openjiuwen.studio.agent")) {
            return value.toString();
        }
        return value;
    }

    private String inferResourceType(String className) {
        if (className == null || className.isEmpty()) {
            return "Unknown";
        }
        String resource = className.replace("ManagementService", "")
            .replace("Service", "")
            .replace("ManagerService", "");
        return resource.isEmpty() ? className : resource;
    }
}