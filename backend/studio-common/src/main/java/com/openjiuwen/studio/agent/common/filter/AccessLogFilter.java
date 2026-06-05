/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.filter;

import com.alibaba.fastjson2.JSON;
import com.openjiuwen.studio.agent.common.utils.UriUtils;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class AccessLogFilter implements Filter {


    @Override
    public void doFilter(ServletRequest request,
        ServletResponse response,
        FilterChain chain) throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest) ||
            !(response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        long start = System.currentTimeMillis();

        try {
            chain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - start;
            int status = res.getStatus();

            // 提取关键字段（避免 null）
            final String method = req.getMethod();
            final String uri = Objects.toString(req.getRequestURI(), "");
            String normalizedUri;
            try {
                normalizedUri = UriUtils.normalizeURI(uri);
            } catch (Exception e) {
                normalizedUri = "<Invalid>";
            }
            final String query = Objects.toString(req.getQueryString(), "");
            final String clientIps = Objects.toString(getClientIps(req), "");
            final String userAgent = Objects.toString(req.getHeader("User-Agent"), "");
            final Map<String, Object> logData = Map.of(
                "time", Instant.now(),
                "client_ips", clientIps,
                "method", method,
                "uri", uri,
                "normalized_uri", normalizedUri,
                "query", query,
                "status", status,
                "duration_ms", duration,
                "user_agent", userAgent
            );
            final String logMsg = JSON.toJSONString(logData);
            log.info("[AccessLog]{}", logMsg);
        }
    }

    private String getClientIps(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip != null ? ip : "unknown";
    }

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void destroy() {
    }
}
