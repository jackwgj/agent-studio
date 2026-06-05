/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.service;

import com.alibaba.fastjson2.JSONObject;
import com.openjiuwen.studio.agent.common.constant.Constants;
import com.openjiuwen.studio.agent.common.entity.AgencyTokenReqEntity;
import com.openjiuwen.studio.agent.common.entity.AssumeRoleEntity;
import com.openjiuwen.studio.agent.common.entity.AuthEntity;
import com.openjiuwen.studio.agent.common.entity.IdentityEntity;
import com.openjiuwen.studio.agent.common.entity.ProjectEntity;
import com.openjiuwen.studio.agent.common.entity.ScopeEntity;
import com.openjiuwen.studio.agent.common.utils.NameConvertUtils;
import com.openjiuwen.studio.agent.common.utils.OkHttpClientUtils;

import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 功能描述
 *
 */
@Service
@Slf4j
public class DelegateExchangeTokenService {
    private static final String CONTENT_TYPE = "Content-Type";

    private static final String APPLICATION_JSON = "application/json";

    private static final String X_SUBJECT_TOKEN = "X-Subject-Token";

    private static final String ASSUME_ROLE = "assume_role";

    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

    @Autowired
    private OkHttpClientUtils okHttpClientUtils;

    @Value("${op.svc.project-id}")
    private String opSvcProjectId;

    @Value("${delegate.token.itep-dispatcher-endpoint}")
    private String itepDispatcherEndpoint;

    @Value("${delegate.token.agency-token-url}")
    private String agencyTokenUrl;

    /**
     * 委托换取token
     *
     * @param projectId 最终租户
     * @param domainId 最终租户
     * @param opAuthToken 承载租户token
     * @return 最终租户token
     * @throws IOException IOException
     */
    public String delegateExchangeToken(String projectId, String domainId, String opAuthToken) throws IOException {
        RequestBody requestBody = RequestBody.create(
            NameConvertUtils.humpToSerpentine(JSONObject.toJSONString(buildAgencyTokenReqEntity(projectId, domainId))),
            MEDIA_TYPE_JSON);
        Request.Builder builder = new Request.Builder();
        builder.addHeader(CONTENT_TYPE, APPLICATION_JSON);
        builder.addHeader(Constants.Header.X_AUTH_TOKEN, opAuthToken);
        Request request = builder.url(itepDispatcherEndpoint + String.format(agencyTokenUrl, opSvcProjectId))
            .post(requestBody)
            .build();
        OkHttpClient httpClient = okHttpClientUtils.getHttpClient();
        try (Response response = httpClient.newCall(request).execute()) {
            return response.header(X_SUBJECT_TOKEN);
        }
    }

    /**
     * 构建接口请求体
     *
     * @param projectId 最终租户projectID
     * @param domainId 最终租户domainId
     * @return AgencyTokenReqEntity
     */
    private AgencyTokenReqEntity buildAgencyTokenReqEntity(String projectId, String domainId) {
        List<String> methods = new ArrayList<>();
        methods.add(ASSUME_ROLE);
        AssumeRoleEntity assumeRole = new AssumeRoleEntity();
        assumeRole.setDomainId(domainId);
        IdentityEntity identity = new IdentityEntity();
        identity.setMethods(methods);
        identity.setAssumeRole(assumeRole);
        ProjectEntity project = new ProjectEntity();
        project.setId(projectId);
        ScopeEntity scope = new ScopeEntity();
        scope.setProject(project);
        AuthEntity auth = new AuthEntity();
        auth.setIdentity(identity);
        auth.setScope(scope);
        AgencyTokenReqEntity agencyTokenReq = new AgencyTokenReqEntity();
        agencyTokenReq.setAuth(auth);
        return agencyTokenReq;
    }
}
