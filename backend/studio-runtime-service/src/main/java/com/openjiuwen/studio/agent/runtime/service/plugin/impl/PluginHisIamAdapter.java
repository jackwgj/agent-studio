/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.service.plugin.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.CryptoUtils;
import com.openjiuwen.studio.agent.common.utils.JsonUtils;
import com.openjiuwen.studio.agent.runtime.dto.md.HisIAMAuthInfo;
import com.openjiuwen.studio.agent.runtime.model.RequestResult;
import com.openjiuwen.studio.agent.runtime.utils.OkHttpUtils;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.shaded.com.google.common.cache.Cache;
import org.apache.curator.shaded.com.google.common.cache.CacheBuilder;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class PluginHisIamAdapter {

    @Autowired
    private OkHttpUtils okHttpUtils;

    private final Cache<String, String> tokenCache = CacheBuilder.newBuilder()
        .maximumSize(50)
        .expireAfterWrite(1, TimeUnit.HOURS)
        .build();

    public Map<String, String> requestHeaderHandler(HisIAMAuthInfo hisAuth, Map<String, String> headers) {
        log.info("Start to execute His iam auth!");
        String token;
        if (StringUtils.isEmpty(hisAuth.getAuthId())) {
            token = queryIamTokenFromHis(hisAuth);
        } else {
            try {
                token = tokenCache.get(hisAuth.getAuthId(), () -> queryIamTokenFromHis(hisAuth));
            } catch (Exception e) {
                log.warn("Fail query token from cache. {}", e.getMessage());
                token = queryIamTokenFromHis(hisAuth);
            }
        }

        headers.put("Authorization", token);

        return headers;
    }

    private String getRequestBody(HisIAMAuthInfo hisAuth) {
        log.info("Start to get request body!");
        return String.format(Locale.ROOT, "{\"data\":{\"type\":\"token\",\"attributes\":{\"account\":\"%s\","
                + "\"secret\":\"%s\",\"project\":\"%s\",\"enterprise\":\"%s\"}}}", hisAuth.getIamAccount(),
            CryptoUtils.decrypt(hisAuth.getIamSecret()), hisAuth.getIamProject(), hisAuth.getIamEnterprise());
    }

    private String queryIamTokenFromHis(HisIAMAuthInfo hisAuth) {
        log.info("Start to execute!");
        String bodyStr = getRequestBody(hisAuth);
        HttpPost httpPost = new HttpPost(hisAuth.getIamUrl());
        httpPost.setEntity(new StringEntity(bodyStr, ContentType.APPLICATION_JSON));

        RequestResult response = null;
        long start = System.currentTimeMillis();
        try {
            log.info("Start to request his iam token. url: {}", hisAuth.getIamUrl());
            response = okHttpUtils.call(hisAuth.getIamUrl(), OkHttpUtils.Method.POST, null, bodyStr);
            if (!response.getCode().equals(201)) {
                throw new AgentStudioException(StudioError.PLUGIN_AUTH_DATA_INVALID);
            }
            log.info("Success query his iam auth token.");

            return (String) JsonUtils.decode(response.getResponse(), new TypeReference<Map<String, Object>>() { })
                .get("access_token");
        } catch (Exception e) {
            log.error("Request: {} timeout. {}", hisAuth.getIamUrl(), e.getMessage());
            throw new AgentStudioException(StudioError.PLUGIN_AUTH_DATA_INVALID);
        }

    }
}
