/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2023-2023. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.app.model.web;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URI;

/**
 * 前台请求的上下文，包括请求头和cookie的解析
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebContextInfo {
    private String requestUri;

    private CookieInfo cookieInfo;

    public static WebContextInfo buildWebContextInfo(HttpServletRequest request) {
        return WebContextInfo.builder()
            .requestUri(URI.create(request.getRequestURI()).normalize().toString())
            .cookieInfo(CookieInfo.buildCookieInfo(request))
            .build();
    }

    @Override
    public String toString() {
        return "WebContextInfo{" + "requestUri='" + requestUri + '\'' + ", cookieInfo=" + cookieInfo + '}';
    }
}
