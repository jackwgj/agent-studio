/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2023. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.app.filter;

import com.openjiuwen.studio.agent.space.app.filter.authitem.OpenapiAuthItemManager;
import com.openjiuwen.studio.agent.space.common.context.ContextUtils;
import com.openjiuwen.studio.agent.space.common.exception.AgentSpaceErrorCodes;
import com.openjiuwen.studio.agent.space.common.exception.AgentSpaceException;
import com.openjiuwen.studio.agent.space.common.utils.SpringContextUtils;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;

/**
 * 外部网元身份认证filter
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@Slf4j
@WebFilter(filterName = "openApiAuthFilter", urlPatterns = {"/*"}, asyncSupported = true)
public class OpenApiAuthFilter implements Filter {
    private OpenapiAuthItemManager getAuthItemManager() {
        return SpringContextUtils.getBean(OpenapiAuthItemManager.class);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
        throws IOException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        try {
            log.info("OpenapiAuthFilter.afterReceiveRequest");

            // 1.获得URL
            String urlPath = request.getRequestURI();
            log.info("OpenapiAuthFilter.afterReceiveRequest, url = {}", urlPath);

            // 2.增加白名单控制，防止由于切nuwa导致请求校验失败
            if (getAuthItemManager().isTrustUrl(urlPath)) {
                log.warn("The request is trust url . path={}", urlPath);
                filterChain.doFilter(request, response);
                return;
            }

            // 3.根据url，appid等进行身份认证
            if (!getAuthItemManager().auth(requestWrapper, response, urlPath)) {
                return;
            }

            filterChain.doFilter(requestWrapper, response);
        } catch (Throwable e) {
            log.error("afterReceiveRequest, Exception Occurred: ", e);
            throw new AgentSpaceException(AgentSpaceErrorCodes.SYSTEM_ERROR);
        } finally {
            ContextUtils.removeInvocationContext();
        }
    }
}
