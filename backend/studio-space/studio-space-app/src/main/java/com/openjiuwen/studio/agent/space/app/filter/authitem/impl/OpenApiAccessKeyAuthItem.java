/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.app.filter.authitem.impl;

import com.openjiuwen.studio.agent.space.api.auth.AccessKeyAuth;
import com.openjiuwen.studio.agent.space.app.filter.authitem.AuthItem;
import com.openjiuwen.studio.agent.space.common.constant.ContextConstants;
import com.openjiuwen.studio.agent.space.common.context.ContextUtils;
import com.openjiuwen.studio.agent.space.common.exception.AgentSpaceErrorCodes;
import com.openjiuwen.studio.agent.space.common.exception.AgentSpaceException;
import com.openjiuwen.studio.agent.space.common.utils.HMACSHA256;
import com.openjiuwen.studio.agent.space.common.utils.JacksonUtils;
import com.openjiuwen.studio.agent.space.common.utils.StringUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * apigw鉴权校验
 */
@Component
@Slf4j
public class OpenApiAccessKeyAuthItem implements AuthItem {
    @Value("${agent.space.signature.accessKey:}")
    private String accessKey;

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
                return method.isAnnotationPresent(AccessKeyAuth.class);
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
        log.info("OpenapiAuthForOrgIdIam: Enter afterReceiveRequest.");
        if (StringUtil.isEmpty(accessKey)) {
            log.error("OpenApiAuthForOrgIdIam: accessKey is empty!");
            throw new AgentSpaceException(AgentSpaceErrorCodes.SYSTEM_ERROR);
        }

        String userId = request.getHeader("user_id");
        String domainId = request.getHeader("domain_id");

        if (StringUtils.isBlank(userId) || StringUtils.isBlank(domainId)) {
            log.error("user info is incomplete");
            try {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "user info is incomplete.");
            } catch (IOException e) {
                log.error("OpenApiAccessKeyAuth::user info is incomplete.", e);
                throw new AgentSpaceException(AgentSpaceErrorCodes.REQUEST_PARAM_IS_NULL, "user_id|domain_id");
            }
            return false;
        }
        ContextUtils.getInvocationContext().addContext(ContextConstants.AGENT_SPACE_USER_ID, userId);
        ContextUtils.getInvocationContext().addContext(ContextConstants.AGENT_SPACE_DOMAIN_ID, domainId);

        String reqSignature = request.getHeader("x-sign");
        String timestamp = request.getHeader("x-timestamp");
        String nonce = request.getHeader("x-nonce");
        if (StringUtil.isEmptyString(reqSignature) || StringUtil.isEmptyString(timestamp) || StringUtil.isEmptyString(
            nonce)) {
            try {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "x-param not found.");
            } catch (IOException e) {
                log.error("OpenApiAccessKeyAuth::auth x-param not found.", e);
                throw new AgentSpaceException(AgentSpaceErrorCodes.REQUEST_PARAM_IS_NULL, "x-param");
            }
            return false;
        }
        String reqParams = getReqParams(request);
        String calculateSignature = HMACSHA256.sha256Hmac(accessKey + nonce + timestamp + reqParams, accessKey);
        if (!calculateSignature.equals(reqSignature)) {
            try {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "signature not match.");
            } catch (IOException e) {
                log.error("OpenApiAccessKeyAuth::auth signature not match.", e);
                throw new AgentSpaceException(AgentSpaceErrorCodes.OPENAPI_AUTH_FAILED);
            }
            return false;
        }
        return true;
    }

    private String getReqParams(HttpServletRequest request) {
        String reqBody = getBody(request);
        try {
            Map<String, Object> reqParamMap = JacksonUtils.json2PojoMap(reqBody, Object.class);
            Map<String, Object> sortReqParamMap = new TreeMap<>(reqParamMap);
            sortReqParamMap.entrySet().removeIf(entry -> Objects.isNull(entry.getValue()));
            return JacksonUtils.obj2jsonUnformatted(sortReqParamMap);
        } catch (Exception e) {
            log.error("OpenApiAccessKeyAuth::getReqParams error.", e);
            return reqBody;
        }
    }

    private String getBody(HttpServletRequest request) {
        try {
            return new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("OpenApiAccessKeyAuth::getBody error.", e);
        }
        return null;
    }
}
