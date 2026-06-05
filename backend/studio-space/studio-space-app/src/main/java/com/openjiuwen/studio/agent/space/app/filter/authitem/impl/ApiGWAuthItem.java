/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.app.filter.authitem.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openjiuwen.studio.agent.space.api.auth.ApiGWAuth;
import com.openjiuwen.studio.agent.space.app.config.ApiGWSignatureConfigInfo;
import com.openjiuwen.studio.agent.space.app.filter.authitem.AuthItem;
import com.openjiuwen.studio.agent.space.app.util.BuildResponseUtil;
import com.openjiuwen.studio.agent.space.common.exception.AgentSpaceErrorCodes;
import com.openjiuwen.studio.agent.space.common.exception.AgentSpaceException;
import com.openjiuwen.studio.agent.space.common.utils.StringUtil;
import com.openjiuwen.studio.agent.space.common.utils.httpUtils.SignRequest;
import com.openjiuwen.studio.agent.space.common.utils.httpUtils.Signer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serial;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * apigw鉴权校验
 */
@Component
@Slf4j
public class ApiGWAuthItem implements AuthItem {
    private static final Map<String, String> SECRETS = Collections.unmodifiableMap(new HashMap<>() {
        @Serial
        private static final long serialVersionUID = 9111115326555845389L;

        {
            this.put(ApiGWSignatureConfigInfo.getInstance().getKey(),
                ApiGWSignatureConfigInfo.getInstance().getSecret());
        }
    });

    private static final Pattern ACCESS_PATTERN = Pattern.compile("Access=([^,]+)");

    private static final Pattern HEAD_PATTERN = Pattern.compile("SignedHeaders=([^,]+)");

    private static final String DATE_PATTERN = "yyyyMMdd'T'HHmmss'Z'";

    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Override
    public boolean isMatch(HttpServletRequest request, String urlPath) {
        log.info("Start OpenApiAccessKeyAuthItem isMatch urlPath={}", urlPath);
        try {
            // 获取当前请求对应的处理器信息。
            HandlerExecutionChain handler = requestMappingHandlerMapping.getHandler(request);

            // 确保处理器是一个方法处理器。
            if (handler != null && handler.getHandler() instanceof HandlerMethod handlerMethod) {
                Method method = handlerMethod.getMethod();
                return method.isAnnotationPresent(ApiGWAuth.class);
            } else {
                log.error("OpenApiAccessKeyAuthItem::isMatch Not Found handlerMethod!");
                throw new AgentSpaceException(AgentSpaceErrorCodes.SYSTEM_ERROR);
            }
        } catch (Exception e) {
            log.error("OpenApiAccessKeyAuthItem::isMatch Get AccessKeyAuth Annotation failed!", e);
            throw new AgentSpaceException(AgentSpaceErrorCodes.SYSTEM_ERROR);
        }
    }

    @Override
    public boolean auth(HttpServletRequest request, HttpServletResponse response) {
        log.info("Enter afterReceiveRequest.");
        try {
            String authorization = request.getHeader("Authorization");
            if (StringUtil.isEmptyString(authorization)) {
                BuildResponseUtil.createOpenApiErrResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Authorization not found.");
                return false;
            }

            Matcher accessMatcher = ACCESS_PATTERN.matcher(authorization);
            if (!accessMatcher.find()) {
                BuildResponseUtil.createOpenApiErrResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Authorization access format incorrect.");
                return false;
            }
            String signingKey = accessMatcher.group(1);
            String signingSecret = SECRETS.get(signingKey);
            if (signingSecret == null) {
                BuildResponseUtil.createOpenApiErrResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Signing key not found.");
                return false;
            }
            Matcher headMatcher = HEAD_PATTERN.matcher(authorization);
            if (!headMatcher.find()) {
                BuildResponseUtil.createOpenApiErrResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Authorization head format incorrect.");
                return false;
            }
            String[] signedHeaders = headMatcher.group(1).split(";");

            SignRequest apiRequest = new SignRequest();
            apiRequest.setMethod(request.getMethod());
            String url = request.getRequestURI();
            log.info("ApigatewaySignatureFilter getRequestURI, url = {}", url);
            String queryString = request.getQueryString();
            if (StringUtils.isNotBlank(queryString)) {
                url = url + "?" + queryString;
            }
            apiRequest.setUrl(url);

            boolean needbody = true;
            String dateHeader = null;
            for (String signedHeader : signedHeaders) {
                String headerValue = request.getHeader(signedHeader);
                if (StringUtils.isBlank(headerValue)) {
                    BuildResponseUtil.createOpenApiErrResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                        "Signed header " + signedHeader + " not found.");
                    return false;
                } else {
                    apiRequest.addHeader(signedHeader, headerValue);
                    if (signedHeader.equalsIgnoreCase("x-sdk-content-sha256") && headerValue.equals(
                        "UNSIGNED-PAYLOAD")) {
                        needbody = false;
                    }
                    if (signedHeader.equalsIgnoreCase("x-sdk-date")) {
                        dateHeader = headerValue;
                    }
                }
            }
            if (dateHeader == null) {
                BuildResponseUtil.createOpenApiErrResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Header x-sdk-date not found.");
                return false;
            }
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = sdf.parse(dateHeader);
            long duration = Math.abs(new Date().getTime() - date.getTime());
            if (duration > 15 * 60 * 1000) {
                BuildResponseUtil.createOpenApiErrResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Signature expired.");
                return false;
            }

            if (needbody) {
                apiRequest.setBody(getBody(request));
            } else {
                apiRequest.setBody("");
            }

            apiRequest.addHeader("authorization", authorization);
            apiRequest.setKey(signingKey);
            apiRequest.setSecret(signingSecret);
            Signer signer = new Signer();
            boolean verify = signer.verify(apiRequest);
            log.info("afterReceiveRequest, verify = {}", verify);
            if (!verify) {
                BuildResponseUtil.createOpenApiErrResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Verify authorization failed.");
                return false;
            }
            return true;
        } catch (Exception e) {
            log.info("afterReceiveRequest, Exception Occurred: ", e);
            BuildResponseUtil.createOpenApiErrResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                "Verify authorization failed.");
            return false;
        }
    }

    private String getBody(HttpServletRequest request) {
        try (InputStream inputStream = request.getInputStream()) {
            Object bodyBuffer = new ObjectMapper().readValue(inputStream, Object.class);
            if (bodyBuffer == null) {
                return StringUtils.EMPTY;
            }
            return bodyBuffer.toString();
        } catch (IOException e) {
            log.error("Read Request Body Failed", e);
            throw new AgentSpaceException(AgentSpaceErrorCodes.SYSTEM_ERROR);
        }
    }
}
