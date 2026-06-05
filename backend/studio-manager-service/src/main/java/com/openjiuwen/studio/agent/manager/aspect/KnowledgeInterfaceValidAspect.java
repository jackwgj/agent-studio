/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.aspect;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.I18nUtil;

import lombok.extern.slf4j.Slf4j;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class KnowledgeInterfaceValidAspect {

    @Autowired
    private I18nUtil i18nUtil;

    @Value("${knowledge.internal.enabled}")
    private boolean knowledgeSelfEnabled;

    @Around("@annotation(com.openjiuwen.studio.agent.manager.aspect.KnowledgeInterfaceValid)")
    public Object checkAccess(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!knowledgeSelfEnabled) {
            log.error("The current environment knowledgeBase is not support to access this interface!Please contact the administrator to configure and try again!");
            throw new AgentStudioException(StudioError.NO_PERMISSION_ACCESS_INTERFACE);
        }
        return joinPoint.proceed();
    }
}
