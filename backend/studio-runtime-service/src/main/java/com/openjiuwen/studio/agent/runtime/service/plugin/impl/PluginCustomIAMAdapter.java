/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service.plugin.impl;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.CryptoUtils;
import com.openjiuwen.studio.agent.common.utils.SpringBeanUtils;
import com.openjiuwen.studio.agent.runtime.dto.md.CustomIAMAuthInfo;
import com.openjiuwen.studio.agent.runtime.service.md.RuntimeModelHttpClientService;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.ConnectTimeoutException;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
public class PluginCustomIAMAdapter{

    public Map<String, String> requestHeaderHandler(CustomIAMAuthInfo iamAuth, Map<String, String> headers) {
        String token;

        token = queryIamTokenFromIam(iamAuth);

        headers.put("X-Auth-Token", token);
        return headers;
    }

    private String getRequestBody(CustomIAMAuthInfo iamAuth) {
        if (StringUtils.isAnyEmpty(iamAuth.getIamAK(), iamAuth.getIamSK())) {
            return String.format(Locale.ROOT, "{\"auth\":{\"identity\":{\"password\":{\"user\":{\"name\":\"%s\""
                    + ",\"password\":\"%s\",\"domain\":{\"name\":\"%s\"}}},\"methods\":[\"password\"]},"
                    + "\"scope\":{\"project\":{\"name\":\"%s\"}}}}", iamAuth.getIamUser(),
                CryptoUtils.decrypt(iamAuth.getIamPassword()), iamAuth.getIamDomain(), iamAuth.getIamProject());
        } else {
            return String.format(Locale.ROOT, "{\"auth\": {\"identity\": {\"hw_ak_sk\": {\"access\": "
                    + "{\"key\": \"%s\"}, \"secret\": {\"key\": \"%s\"}}, \"methods\": [\"hw_ak_sk\"]}, \"scope\": "
                    + "{\"project\": {\"name\": \"%s\"}}}}", iamAuth.getIamAK(), CryptoUtils.decrypt(iamAuth.getIamSK()),
                iamAuth.getIamProject());
        }
    }

    private String queryIamTokenFromIam(CustomIAMAuthInfo iamAuth) {
        String bodyStr = getRequestBody(iamAuth);

        HttpPost httpPost = new HttpPost(iamAuth.getIamUrl());
        httpPost.setEntity(new StringEntity(bodyStr, ContentType.APPLICATION_JSON));

        RuntimeModelHttpClientService httpClientService = SpringBeanUtils.getBean(RuntimeModelHttpClientService.class);

        long start = System.currentTimeMillis();
        try {
            CloseableHttpClient client = httpClientService.getModelRequestHttpClient(httpPost.getUri().getHost());
            return client.execute(httpPost, res -> {
                if (res.getCode() >= 300) {
                    String responseString =
                        res.getEntity() == null ? "" : EntityUtils.toString(res.getEntity(), "UTF-8");
                    log.error("Fail query iam user token. domain:{} project:{} user:{} url:{} rsp:{}",
                        iamAuth.getIamDomain(), iamAuth.getIamProject(), iamAuth.getIamUser(), iamAuth.getIamUrl(),
                        responseString);
                    throw new AgentStudioException(StudioError.PLUGIN_AUTH_DATA_INVALID);
                }

                Header header = res.getFirstHeader("X-Subject-Token");
                return header.getValue();
            });
        } catch (ConnectTimeoutException e) {
            log.error("Request {} timeout. {}", iamAuth.getIamUrl(), e.getMessage());
            throw new AgentStudioException(StudioError.PLUGIN_AUTH_DATA_INVALID);
        } catch (Exception e) {
            log.error("Fail request {}, cost: {} ms", iamAuth.getIamUrl(), System.currentTimeMillis() - start, e);
            throw new AgentStudioException(StudioError.PLUGIN_AUTH_DATA_INVALID);
        }
    }
}
