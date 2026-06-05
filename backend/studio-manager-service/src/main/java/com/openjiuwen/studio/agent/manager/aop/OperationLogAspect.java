/* * Copyright (c) Huawei Technologies Co., Ltd. 2021-2025. All rights reserved. */

package com.openjiuwen.studio.agent.manager.aop;

import com.openjiuwen.studio.agent.common.annotation.OperationLog;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;

import lombok.extern.slf4j.Slf4j;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * 添加类的描述
 *
 */
@Aspect
@Component
@Slf4j
public class OperationLogAspect {

    @Value("${studio.operationLog.switch:true}")
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
            recordUserOperation(joinPoint, throwable);
        }
    }

    /**
     * 记录日志入库，修改成异步方法记录
     * @param joinPoint
     * @param throwable
     */
    private void recordUserOperation(JoinPoint joinPoint, Throwable throwable) {
        String requestUserId = RequestContextUtils.getRequestUserId();
        String requestWorkspaceId = RequestContextUtils.getRequestWorkspaceId();
        String requestProjectId = RequestContextUtils.getRequestProjectId();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        ServletRequestAttributes attributes =
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String requestUrl = attributes != null ? attributes.getRequest().getRequestURI() : "UNKNOWN_URL";

        log.info("operation log projectId [{}]: user [{}], workspace [{}], method [{}], url [{}], success [{}]",
            requestProjectId, requestUserId, requestWorkspaceId, method.getName(), requestUrl, throwable == null);
    }
}

