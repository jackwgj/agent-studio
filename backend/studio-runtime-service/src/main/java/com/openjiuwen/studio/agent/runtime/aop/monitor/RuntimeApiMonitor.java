/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.aop.monitor;

import com.openjiuwen.studio.agent.runtime.alarm.app.AppApiRecoder;
import com.openjiuwen.studio.agent.runtime.dto.WorkflowRunRsp;
import com.openjiuwen.studio.agent.runtime.enums.WorkflowRunStatus;
import com.openjiuwen.studio.agent.runtime.utils.SseEmitterUtils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 应用运行接口报错监控
 *
 */
@Aspect
@Component
@Slf4j
@ConditionalOnProperty(name = "omservice_endpoint")
public class RuntimeApiMonitor {
    private static final Pattern PATTERN_WORKFLOW = Pattern.compile("/v1/[^/]+/workflows/[^/]+/conversations.*");

    private static final Pattern PATTERN_AGENT = Pattern.compile("/v1/[^/]+/agents/[^/]+/conversations.*");

    private static final String DEBUG_TAG = "DEBUG";

    private static final String PUBLISH_TAG = "PUBLISHED";

    private static final String DEBUG_HEADER_KEY = "X-INVOKE-MODE";

    private static final String ID_UNKNOWN = "unknown";

    private static final int APP_ID_INDEX = 4;

    @Pointcut("execution(* com.openjiuwen.studio.agent.runtime.controller.*.*(..))")
    public void apiCall() {
    }

    @AfterReturning(value = "apiCall()", returning = "object")
    public Object after(JoinPoint joinPoint, Object object) {
        log.info("RuntimeApiMonitor start...");
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null || isDebugRequest(attributes.getRequest())) {
            return object;
        }

        HttpServletRequest request = attributes.getRequest();
        String url = request.getRequestURI();
        String appType = appTypeFromUrl(url);
        if (StringUtils.isEmpty(appType)) {
            return object;
        }
        Pair<String, String> ids = idsFromUrl(url);
        String projectId = ids.getLeft();
        String appId = ids.getRight();
        if (object instanceof SseEmitter sseEmitter) {
            sseEmitter.onCompletion(() -> {
                Throwable error = SseEmitterUtils.getFailureOnSseEmitter(sseEmitter);
                log.info("Call stream api completion with error: {}", !Objects.isNull(error));
                if (error == null) {
                    AppApiRecoder.recordSuccessApp(appId);
                } else {
                    AppApiRecoder.recordFailureApp(projectId, appId, appType, error.getMessage());
                }
            });
            sseEmitter.onTimeout(() -> {
                SseEmitterUtils.setFailureOnSseEmitter(sseEmitter, new Throwable("time out"));
            });
        } else {
            if ("workflow".equals(appType) && object instanceof ResponseEntity<?> entity
                && entity.getBody() instanceof WorkflowRunRsp) {
                WorkflowRunRsp rsp = (WorkflowRunRsp) entity.getBody();
                if (rsp != null) {
                    if (Objects.equals(rsp.getStatus().getCode(), WorkflowRunStatus.FAILED.getStatus().getCode())) {
                        AppApiRecoder.recordFailureApp(projectId, appId, appType, rsp.getErrorMessage());
                    } else {
                        AppApiRecoder.recordSuccessApp(appId);
                    }
                }
            } else {
                AppApiRecoder.recordSuccessApp(appId);
            }
        }

        return object;
    }

    @AfterThrowing(value = "apiCall()", throwing = "ex")
    public void afterThrowing(JoinPoint joinPoint, Exception ex) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null || isDebugRequest(attributes.getRequest())) {
            return;
        }

        HttpServletRequest request = attributes.getRequest();
        String url = request.getRequestURI();
        String projectId = "";
        String appType = appTypeFromUrl(url);
        String appId = "";
        Pair<String, String> ids = idsFromUrl(url);
        if (!StringUtils.isEmpty(appType)) {
            projectId = ids.getLeft();
            appId = ids.getRight();
        } else {
            return;
        }
        String errorMsg = ex.getMessage();
        AppApiRecoder.recordFailureApp(projectId, appId, appType, errorMsg);
    }

    private boolean isDebugRequest(HttpServletRequest request) {
        String tag = request.getHeader(DEBUG_HEADER_KEY);
        return Objects.nonNull(tag) && (Objects.equals(DEBUG_TAG, tag.toUpperCase(Locale.ROOT)) || Objects.equals(
            PUBLISH_TAG, tag.toUpperCase(Locale.ROOT)));
    }

    private String appTypeFromUrl(String url) {
        if (PATTERN_WORKFLOW.matcher(url).matches()) {
            return "workflow";
        } else if (PATTERN_AGENT.matcher(url).matches()) {
            return "agent";
        }
        return "";
    }

    private Pair<String, String> idsFromUrl(String url) {
        if (StringUtils.isBlank(url)) {
            return Pair.of(ID_UNKNOWN, ID_UNKNOWN);
        }
        String[] pieces = url.split("/");
        if (pieces.length > APP_ID_INDEX) {
            return Pair.of(pieces[2], pieces[APP_ID_INDEX]);
        }
        return Pair.of(ID_UNKNOWN, ID_UNKNOWN);
    }
}
