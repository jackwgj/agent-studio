/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.service.plugin.impl;

import static com.openjiuwen.studio.agent.runtime.service.md.MdConstant.HIS_CUST_SCENARIOUUID;
import static com.openjiuwen.studio.agent.runtime.service.md.MdConstant.HIS_CUST_USERID;

import com.fasterxml.jackson.core.type.TypeReference;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.CryptoUtils;
import com.openjiuwen.studio.agent.common.utils.JsonUtils;
import com.openjiuwen.studio.agent.common.utils.SpringBeanUtils;
import com.openjiuwen.studio.agent.runtime.dto.md.SgovAuthInfo;
import com.openjiuwen.studio.agent.runtime.service.md.RuntimeModelHttpClientService;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.shaded.com.google.common.cache.Cache;
import org.apache.curator.shaded.com.google.common.cache.CacheBuilder;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class PluginHisSgovAdapter {

    private final Cache<String, String> tokenCache = CacheBuilder.newBuilder()
            .maximumSize(50)
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build();

    public Map<String, String> requestHeaderHandler(SgovAuthInfo sgovAuth, Map<String, String> headers) {
        log.info("Start to execute sogv auth!");
        String token;
        if (StringUtils.isEmpty(sgovAuth.getAuthId())) {
            token = querySgovTokenFromIam(sgovAuth);
        } else {
            try {
                token = tokenCache.get(sgovAuth.getAuthId(), () -> querySgovTokenFromIam(sgovAuth));
            } catch (Exception e) {
                log.warn("Fail query token from cache. {}", e.getMessage());
                token = querySgovTokenFromIam(sgovAuth);
            }
        }

        headers.put("Authorization", token);
        if (!StringUtils.isEmpty(sgovAuth.getUserId())) {
            headers.put(HIS_CUST_USERID, sgovAuth.getUserId());
        }
        if (!StringUtils.isEmpty(sgovAuth.getScenarioUuid())) {
            headers.put(HIS_CUST_SCENARIOUUID, sgovAuth.getScenarioUuid());
        }
        return headers;
    }

    private String getRequestBody(SgovAuthInfo sgovAuth) {
        log.info("Start to get request body.");
        return String.format(Locale.ROOT, "{\"appId\":\"%s\",\"credential\":\"%s\"}", sgovAuth.getAppId(),
                CryptoUtils.decrypt(sgovAuth.getCredential()));
    }

    private String querySgovTokenFromIam(SgovAuthInfo sgovAuth) {
        log.info("Start query sgov auth token.");
        String bodyStr = getRequestBody(sgovAuth);
        HttpPost httpPost = new HttpPost(sgovAuth.getSgovUrl());
        httpPost.setEntity(new StringEntity(bodyStr, ContentType.APPLICATION_JSON));

        RuntimeModelHttpClientService httpClientService = SpringBeanUtils.getBean(RuntimeModelHttpClientService.class);
        long start = System.currentTimeMillis();
        try {
            CloseableHttpClient client = httpClientService.getModelRequestHttpClient(httpPost.getUri().getHost());
            return client.execute(httpPost, res -> {
                if (res.getCode() >= 300) {
                    String responseStr = res.getEntity() == null
                        ? ""
                        : EntityUtils.toString(res.getEntity(), "UTF-8");
                    log.error("Fail query his Sgov auth token. appId:{} url:{}", sgovAuth.getAppId(),
                        sgovAuth.getSgovUrl());
                    throw new AgentStudioException(StudioError.PLUGIN_AUTH_DATA_INVALID);
                }
                String rspStr = EntityUtils.toString(res.getEntity(), "UTF-8");
                if (rspStr.contains("EXCEPTION")) {
                    throw new AgentStudioException(StudioError.PLUGIN_AUTH_DATA_INVALID);
                }
                log.info("Success query sgov auth token. appId:{} url:{}", sgovAuth.getAppId(), sgovAuth.getSgovUrl());
                return (String) JsonUtils.decode(rspStr, new TypeReference<Map<String, Object>>() {}).get("result");
            });
        } catch (Exception e) {
            log.error("Fail request {}, cost: {} ms", sgovAuth.getSgovUrl(), System.currentTimeMillis() - start, e);
            throw new AgentStudioException(StudioError.PLUGIN_AUTH_DATA_INVALID);
        }
    }
}
