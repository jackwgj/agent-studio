/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.filter;

import static com.openjiuwen.studio.agent.runtime.constant.Constant.REQUEST_ID;

import com.openjiuwen.studio.agent.common.constant.Constants;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.runtime.model.ModelApiLog;
import com.openjiuwen.studio.agent.runtime.thread.ModelApiLogThreadLocal;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
public class RuntimeContextFilter implements Filter {
    public RuntimeContextFilter(boolean iamAuthEnable) {
        this.iamAuthEnable = iamAuthEnable;
        log.info("IAM Auth enable is :{}", iamAuthEnable);
    }

    private final boolean iamAuthEnable;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
        throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        if (!iamAuthEnable) {
            String token = Optional.ofNullable(httpRequest.getHeader(Constants.Header.X_AUTH_TOKEN)).orElse("|");
            if (StringUtils.isEmpty(token)) {
                log.error("X-Auth-Token is empty.");
                throw new AgentStudioException(StudioError.POC_AUTH_FAILED);
            }

            String[] ary = token.split("\\|");
            if (ary.length < 2) {
                log.error("X-Auth-Token is invalid.");
                throw new AgentStudioException(StudioError.POC_AUTH_FAILED);
            }
            RequestContextUtils.setRequestAuthTokenAndUserId(token, ary[1], ary[0]);
        }

        ModelApiLogThreadLocal.clear();
        String requestId = UUID.randomUUID().toString().replace("-", "");

        ModelApiLog apiLog = new ModelApiLog().setId(requestId)
            .setRequestId(MDC.get(REQUEST_ID))
            .setStartTime(System.currentTimeMillis())
            .setIpAddress(servletRequest.getRemoteAddr());

        ModelApiLogThreadLocal.setApiLog(apiLog);

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            String value = request.getHeader(name);
            headers.put(name.toLowerCase(Locale.ROOT), value);
        }
        RequestContextUtils.setHeaders(headers);

        filterChain.doFilter(servletRequest, servletResponse);

        ModelApiLogThreadLocal.clear();
        MDC.clear();
    }
}
