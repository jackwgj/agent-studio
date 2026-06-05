/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.filter.simple;

import com.openjiuwen.studio.agent.common.constant.SimpleConstants;
import com.openjiuwen.studio.agent.common.utils.SpringBeanUtils;
import com.openjiuwen.studio.agent.common.utils.simple.ServletUtils;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * cookie agent_sid 设置到 header的x-auth-token
 *
 */
public class SidToTokenFilter implements Filter {
    private String contextPath;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        if (contextPath == null) {
            contextPath = SpringBeanUtils.getProperty(SimpleConstants.SERVLET_CONTEXT_PATH, "");
        }
        String path = ((HttpServletRequest) request).getRequestURI();
        String quoteContextPath = Pattern.quote(contextPath);
        if (!path.matches(quoteContextPath + "/health|" + quoteContextPath + "/v1/.*|" + quoteContextPath + "/v2/.*|"
            + quoteContextPath + "/v3/.*")) {
            chain.doFilter(request, response);
            return;
        }
        String agentSid = ServletUtils.getAgentSid((HttpServletRequest) request);
        String uri = ((HttpServletRequest) request).getRequestURI();
        HttpServletRequestWrapper requestWrapper = new HttpServletRequestWrapper((HttpServletRequest) request) {
            @Override
            public String getHeader(String name) {
                String superHeader = super.getHeader(name);
                // 调用/v3/auth/tokens不能传x-auth-token
                if (SimpleConstants.X_AUTH_TOKEN.equalsIgnoreCase(name)) {
                    if (uri.equals(contextPath + SimpleConstants.AUTH_TOKEN_URI)) {
                        return "";
                    }
                    if (StringUtils.isEmpty(superHeader) && !StringUtils.isEmpty(agentSid)) {
                        return agentSid;
                    }
                }
                return superHeader;
            }
        };
        chain.doFilter(requestWrapper, response);
    }

    @Override
    public void destroy() {
    }
}
