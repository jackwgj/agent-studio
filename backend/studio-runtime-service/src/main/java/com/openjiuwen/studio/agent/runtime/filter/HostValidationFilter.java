/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.filter;

import com.openjiuwen.studio.agent.common.dto.ErrorRsp;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.ErrorInfo;
import com.openjiuwen.studio.agent.common.utils.I18nUtil;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.runtime.utils.JsonUtils;

import inet.ipaddr.IPAddressString;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 校验客户端域名
 *
 */
@Component
@Order(1)
@Slf4j
@ConditionalOnProperty(name = "env.type", havingValue = "hc")
public class HostValidationFilter extends OncePerRequestFilter {
    private static final int SPLIT_COUNT = 2;

    private final I18nUtil i18nUtil;

    @Value("${host-validation.enabled}")
    private boolean isHostValidatorEnabled;

    @Value("${host-validation.pattern}")
    private String hostPattern;

    @Value("${host-validation.pattern-group}")
    private int patternGroup;

    private Pattern pattern;

    public HostValidationFilter(I18nUtil i18nUtil) {
        this.i18nUtil = i18nUtil;
    }

    @PostConstruct
    public void init() {
        this.pattern = Pattern.compile(hostPattern);
    }

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response,
        @NotNull FilterChain filterChain) throws ServletException, IOException {
        // 域名校验开关未开->放行
        if (!isHostValidatorEnabled) {
            filterChain.doFilter(request, response);
            return;
        }
        String host = getClientHost(request);
        // 没有host->放行
        if (StringUtils.isBlank(host)) {
            log.info("[hostValidator]: header host is empty");
            filterChain.doFilter(request, response);
            return;
        }
        host = host.trim();
        // host是ip地址->放行
        if (isIpAddress(host)) {
            log.info("[hostValidator]: ip address");
            filterChain.doFilter(request, response);
            return;
        }

        Matcher matcher = pattern.matcher(host);
        if (matcher.find() && matcher.groupCount() >= patternGroup) {
            String domainId = matcher.group(patternGroup);
            if (!Objects.equals(domainId, RequestContextUtils.getRequestUserDomainId())) {
                log.error("[hostValidator]: domain id not match, value in host is {}", domainId);
                sendErrorWithStudioError(StudioError.HOST_VALIDATION_DOMAIN_FAILED, response);
                return;
            }
        } else {
            log.info("[hostValidator]: host not matched pattern");
            filterChain.doFilter(request, response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    String getClientHost(HttpServletRequest request) {
        String forwardedHost = getFirstValue(request.getHeader("X-Forwarded-Host"));
        if (forwardedHost != null) {
            return removePort(forwardedHost);
        }

        String hostHeader = getFirstValue(request.getHeader("Host"));
        if (hostHeader != null) {
            return removePort(hostHeader);
        }
        return "";
    }

    boolean isIpAddress(@NotNull String host) {
        String trimHost = host.trim();
        // 处理IPv6的方括号
        if (trimHost.startsWith("[") && trimHost.endsWith("]")) {
            trimHost = trimHost.substring(1, host.length() - 1);
        }
        // 移除区域索引
        int percentIdx = trimHost.indexOf('%');
        if (percentIdx > 0) {
            trimHost = trimHost.substring(0, percentIdx);
        }
        IPAddressString addrString = new IPAddressString(trimHost);
        return addrString.isIPAddress();
    }

    String removePort(String host) {
        if (host != null) {
            int colonIndex = host.indexOf(':');
            return colonIndex > 0 ? host.substring(0, colonIndex) : host;
        }
        return "";
    }

    static String getFirstValue(String value) {
        if (value != null && value.contains(",")) {
            return value.split(",", SPLIT_COUNT)[0];
        }
        return value;
    }

    void sendErrorWithStudioError(StudioError error, HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        ErrorInfo errorInfo = i18nUtil.getMessage(new AgentStudioException(error));
        String code = "AgentBuilder." + error.getModule().getSubCode() + error.getCode();
        ErrorRsp rsp = new ErrorRsp();
        rsp.setErrorCode(code)
            .setErrorMsg(errorInfo.getMessage())
            .setErrorReason(errorInfo.getReason())
            .setErrorSuggestion(errorInfo.getSuggestion());
        response.getWriter().write(Objects.requireNonNull(JsonUtils.toJson(rsp)));
        response.getWriter().flush();
    }
}
