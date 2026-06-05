/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.app.filter.simple;


import com.openjiuwen.studio.agent.space.app.constant.SimpleConstants;
import com.openjiuwen.studio.agent.space.app.util.simple.ServletUtils;
import com.openjiuwen.studio.agent.space.app.util.simple.SimpleAuthUtils;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

/**
 * url里面的用户信息设置到agent_sid的cookie里面
 */
public class SimpleAuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        HttpServletRequest httpRequest = ((HttpServletRequest) request);

        String existToken = ServletUtils.getAgentSid(httpRequest);
        // 如果从url参数或者header参数取到用户信息，则设置到cookie
        String token = SimpleAuthUtils.paramToToken(httpRequest, existToken);

        // 如果不为空则设置到attribute和cookie；否则不设置
        if (!StringUtils.isEmpty(token)) {
            // 首次设置到attribute，后续流程从attribute取
            httpRequest.setAttribute(SimpleConstants.AGENT_SID, token);

            // 如果token构建成功设置到cookie，前端调用时带入
            ServletUtils.setSidToCookie((HttpServletResponse) response, token);
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
