/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.filter;

import com.google.common.collect.ImmutableSet;
import com.openjiuwen.studio.agent.common.constant.Constants;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

/**
 * ResetSystemHeaderFilter
 *
 */
@Slf4j
@Component
public class ResetSystemHeaderInterceptor implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
        throws IOException, ServletException {
        filterChain.doFilter(new ResetSystemHeaderRequestWrapper((HttpServletRequest) servletRequest), servletResponse);
    }

    private static final class ResetSystemHeaderRequestWrapper extends HttpServletRequestWrapper {
        private static final Set<String> SYSTEM_HEADERS = ImmutableSet.of(
            Constants.Header.X_ASSET_APP_INVOKE_IN_FREE_TRIAL, Constants.Header.X_ASSET_APP_ID,
            Constants.Header.X_ASSET_APP_CONVERSATION_ID);

        public ResetSystemHeaderRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String getHeader(String name) {
            if (SYSTEM_HEADERS.contains(name)) {
                return null;
            }
            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            // 过滤Header名称列表
            List<String> names = Collections.list(super.getHeaderNames());
            names.removeIf(SYSTEM_HEADERS::contains);
            return Collections.enumeration(names);
        }
    }
}
