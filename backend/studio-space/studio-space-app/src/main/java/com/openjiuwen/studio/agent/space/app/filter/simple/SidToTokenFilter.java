/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.app.filter.simple;

import com.openjiuwen.studio.agent.space.app.util.simple.ServletUtils;
import com.openjiuwen.studio.agent.space.common.constant.HeaderConstant;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;


/**
 * cookie agent_sid 设置到 header的x-auth-token
 */
public class SidToTokenFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        String agentSid = ServletUtils.getAgentSid((HttpServletRequest) request);
        HttpServletRequestWrapper requestWrapper = new HttpServletRequestWrapper((HttpServletRequest) request) {
            @Override
            public String getHeader(String name) {
                String superHeader = super.getHeader(name);
                if (HeaderConstant.HEADER_X_AUTH_TOKEN.equalsIgnoreCase(name)) {
                    if (StringUtils.isEmpty(superHeader) && !StringUtils.isEmpty(agentSid)) {
                        return agentSid;
                    }
                }
                return superHeader;
            }

            @Override
            public Enumeration<String> getHeaders(String name) {
                if (HeaderConstant.HEADER_X_AUTH_TOKEN.equalsIgnoreCase(name)) {
                    String superHeader = super.getHeader(name);
                    if (StringUtils.isEmpty(superHeader) && !StringUtils.isEmpty(agentSid)) {
                        return Collections.enumeration(List.of(agentSid));
                    }
                }
                return super.getHeaders(name);
            }

            @Override
            public Enumeration<String> getHeaderNames() {
                Enumeration<String> superHeaderNames = super.getHeaderNames();
                if (StringUtils.isEmpty(agentSid)) {
                    return superHeaderNames;
                }

                if (!superHeaderNames.hasMoreElements()) {
                    return Collections.enumeration(List.of(HeaderConstant.HEADER_X_AUTH_TOKEN));
                }

                List<String> headerNames = new ArrayList<>();
                boolean hasXAuthToken = false;

                while (superHeaderNames.hasMoreElements()) {
                    String headerName = superHeaderNames.nextElement();
                    headerNames.add(headerName);
                    if (HeaderConstant.HEADER_X_AUTH_TOKEN.equalsIgnoreCase(headerName)) {
                        hasXAuthToken = true;
                    }
                }

                if (!hasXAuthToken) {
                    headerNames.add(HeaderConstant.HEADER_X_AUTH_TOKEN);
                }

                return Collections.enumeration(headerNames);
            }
        };
        chain.doFilter(requestWrapper, response);
    }

    @Override
    public void destroy() {
    }
}
