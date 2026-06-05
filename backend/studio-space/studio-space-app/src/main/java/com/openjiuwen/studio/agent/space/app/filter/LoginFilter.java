/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.app.filter;

import com.openjiuwen.studio.agent.space.app.constant.LoginConstant;
import com.openjiuwen.studio.agent.space.app.util.CookieUtil;
import com.openjiuwen.studio.agent.space.app.util.EncodingUtil;
import com.openjiuwen.studio.agent.space.app.util.WebUtil;
import com.openjiuwen.studio.agent.space.common.constant.ContextConstants;
import com.openjiuwen.studio.agent.space.common.context.ContextUtils;
import com.openjiuwen.studio.agent.space.common.exception.AgentSpaceErrorCodes;
import com.openjiuwen.studio.agent.space.common.utils.StringUtil;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import org.apache.logging.log4j.util.Strings;
import org.springframework.core.annotation.Order;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

/**
 * 登录功能过滤器
 */
@Order(-1)
@Slf4j
@WebFilter(filterName = "loginFilter", urlPatterns = {"/agentspace/webapi/v1/login"}, asyncSupported = true)
public class LoginFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        try {
            buildLoginContext(request);
        } catch (ServletException e) {
            log.error("buildLoginContext failed!", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    AgentSpaceErrorCodes.SYSTEM_ERROR.errorMsg());
            return;
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }


    private void buildLoginContext(HttpServletRequest request) throws ServletException {
        ContextUtils.addContext(ContextConstants.AGENT_SPACE_SID, CookieUtil.getCookieValueByName(request,
                ContextConstants.AGENT_SPACE_SID));
        ContextUtils.addContext(ContextConstants.AGENT_SPACE_CLIENT_IP, WebUtil.getRealIp(request));
        ContextUtils.addContext(ContextConstants.AGENT_SPACE_USER_ID, CookieUtil.getCookieValueByName(request,
                ContextConstants.AGENT_SPACE_USER_ID));
        ContextUtils.addContext(ContextConstants.AGENT_SPACE_DOMAIN_ID, CookieUtil.getCookieValueByName(request,
                ContextConstants.AGENT_SPACE_DOMAIN_ID));
        ContextUtils.addContext("requestURI", URI.create(request.getRequestURI()).normalize().toString());
        ContextUtils.addContext("realIP", WebUtil.getRealIp(request));
        String loginRedirectUrl = Optional.ofNullable(request.getParameter("redirect"))
                .map(Object::toString).orElse(StringUtil.EMPTY_STRING);
        String iamRedirectUrl = getIamRedirectUrl(request, loginRedirectUrl);
        ContextUtils.addContext("iamRedirectUrl", iamRedirectUrl);
        log.info("iamRedirectUrl is {}", iamRedirectUrl);

        String serverName = getCurrentRequestDomain(request);
        log.error("LoginFilter buildLoginContext: getCurrentRequestDomain res is {} ", serverName);
        ContextUtils.addContext("domainName", serverName);

        // 根据开关设置不同的上下文
        ContextUtils.addContext("ticket",
                Optional.ofNullable(request.getParameter("ticket")).map(Object::toString).orElse(Strings.EMPTY));
        ContextUtils.addContext("loginRedirectUrl", EncodingUtil.decodeURIComponent(loginRedirectUrl));
    }

    private String getIamRedirectUrl(final HttpServletRequest request, final String loginRedirectUrl) {
        String scheme = request.getScheme();
        String logonUrl = scheme + "://" + getCurrentRequestDomain(request) + request.getRequestURI();
        return logonUrl + LoginConstant.REDIRECT_URL_SIGN + EncodingUtil.encodeURIComponent(loginRedirectUrl);
    }

    private String getCurrentRequestDomain(HttpServletRequest request) {
        String domainUrl = request.getHeader("x-forwarded-host");
        log.info("getCurrentRequestDomain domainUrl={}", domainUrl);
        if (!StringUtil.isEmptyString(domainUrl)) {
            return domainUrl.contains(":") ? domainUrl.split(":")[0] : domainUrl;
        }
        log.info("getCurrentRequestDomain host={}", request.getHeader("Host"));
        if (!StringUtil.isEmptyString(request.getHeader("Host"))) {
            return request.getHeader("Host");
        }
        return null;
    }
}
