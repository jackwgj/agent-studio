/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.utils;

import com.openjiuwen.studio.agent.common.constant.Constants;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.Optional;

/**
 * 从header中X-Language获取语言信息
 *
 */
public class RequestHeaderHolderUtils implements Filter {

    private static final ThreadLocal<String> THREAD_LOCAL_LANGUAGE_HOLDER = new ThreadLocal<>();

    public static String getRequestLanguage() {
        return Optional.ofNullable(THREAD_LOCAL_LANGUAGE_HOLDER.get()).orElse(Constants.ZH_CN);
    }

    public static void setRequestLanguage(String language) {
        THREAD_LOCAL_LANGUAGE_HOLDER.set(language);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String language =
            Optional.ofNullable(httpRequest.getHeader(Constants.Header.X_LANGUAGE)).orElse(Constants.ZH_CN);
        THREAD_LOCAL_LANGUAGE_HOLDER.set(language);
        chain.doFilter(request, response);
        THREAD_LOCAL_LANGUAGE_HOLDER.remove();
    }


    public static void clear() {
        THREAD_LOCAL_LANGUAGE_HOLDER.remove();
    }

    @Override
    public void destroy() {
        THREAD_LOCAL_LANGUAGE_HOLDER.remove();
    }
}
