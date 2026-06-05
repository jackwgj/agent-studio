/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2018-2020. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.app.annotation.quota;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Quota注解的具体逻辑
 */
@Aspect
@Component
@Slf4j
public class QuotaAspect {

    @Resource
    QuotaService quotaService;

    @Pointcut("@annotation(com.openjiuwen.studio.agent.space.app.annotation.quota.Quota)")
    public void quotaPointCut() {
    }

    @Before("quotaPointCut()")
    public void beforeExec(JoinPoint joinPoint) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Quota quota = method.getAnnotation(Quota.class);
        quotaService.verifyQuota(quota.type(), quota.offset());
    }

    @AfterReturning("quotaPointCut()")
    public void afterReturnExec(JoinPoint joinPoint) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Quota quota = method.getAnnotation(Quota.class);
        quotaService.operateQuota(quota.type(), quota.offset());
    }
}
