/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.openjiuwen.studio.agent.common.constant.Constants;
import com.openjiuwen.studio.agent.common.entity.AkSkCredential;
import com.openjiuwen.studio.agent.common.entity.AssumeRoleCredential;
import com.openjiuwen.studio.agent.common.entity.AuthRequestWrapper;
import com.openjiuwen.studio.agent.common.entity.PasswordCredential;
import com.openjiuwen.studio.agent.common.entity.TokenResponse;
import com.openjiuwen.studio.agent.common.rce.client.IamClient;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * iam认证相关服务
 * 获取iam的op token, 租户对应的token
 *
 */
@Service
public class IamAuthService {

    private final IamClient iamClient;

    @Value("${iam.user.name}")
    private String userName;

    @Value("${iam.user.domain}")
    private String userDomain;

    @Value("${iam.project.name}")
    private String projectName;

    @Value("${iam.assumeRole.agencyName}")
    private String agencyName;

    public IamAuthService(IamClient iamClient) {
        this.iamClient = iamClient;
    }

    /**
     * 通过OP账号以及密码获取OP账号的token
     *
     * @param password
     * @return
     * @throws JsonProcessingException
     */
    public String authenticateWithPassword(String password) throws JsonProcessingException {
        AuthRequestWrapper request = AuthRequestWrapper.builder()
            .auth(AuthRequestWrapper.Auth.builder()
                .identity(AuthRequestWrapper.Identity.builder()
                    .methods(Collections.singletonList("password"))
                    .password(PasswordCredential.builder()
                        .user(PasswordCredential.User.builder()
                            .name(userName)
                            .password(password)
                            .domain(PasswordCredential.Domain.builder().name(userDomain).build())
                            .build())
                        .build())
                    .build())
                .scope(AuthRequestWrapper.Scope.builder()
                    .project(AuthRequestWrapper.Project.builder().name(projectName).build())
                    .build())
                .build())
            .build();
        ResponseEntity<TokenResponse> response = iamClient.getTokenByPassword(request);
        return response.getHeaders().getFirst("X-Subject-Token");

    }

    /**
     * hcs环境获取账号密码需要是Domain级别
     *
     * @param password
     * @return
     */
    public String authenticateWithPasswordDomain(String password) throws JsonProcessingException {
        AuthRequestWrapper request = AuthRequestWrapper.builder()
            .auth(AuthRequestWrapper.Auth.builder()
                .identity(AuthRequestWrapper.Identity.builder()
                    .methods(Collections.singletonList("password"))
                    .password(PasswordCredential.builder()
                        .user(PasswordCredential.User.builder()
                            .name(userName)
                            .password(password)
                            .domain(PasswordCredential.Domain.builder().name(userDomain).build())
                            .build())
                        .build())
                    .build())
                .scope(AuthRequestWrapper.Scope.builder()
                    .domain(AuthRequestWrapper.Domain.builder().name(userDomain).build())
                    .build())
                .build())
            .build();
        ResponseEntity<TokenResponse> response = iamClient.getTokenByPassword(request);
        return response.getHeaders().getFirst("X-Subject-Token");

    }

    public String authenticateWithAssumeRole(String authToken, String domainId) {
        AuthRequestWrapper request = buildAssumeRoleRequest(domainId);
        return iamClient.getTokenByAssumeRole(authToken, request).getHeaders().getFirst("X-Subject-Token");

    }

    /**
     * 通过用户建立的委托获取用户的token，但是需要租户建立的委托名字以及对应的op的token,租户对应的domainId
     *
     * @param domainId
     * @return
     */
    public AuthRequestWrapper buildAssumeRoleRequest(String domainId) {
        AuthRequestWrapper request = AuthRequestWrapper.builder()
            .auth(AuthRequestWrapper.Auth.builder()
                .identity(AuthRequestWrapper.Identity.builder()
                    .methods(Collections.singletonList("assume_role"))
                    .assume_role(AssumeRoleCredential.builder().domain_id(domainId).agency_name(agencyName).build())
                    .build())
                .scope(AuthRequestWrapper.Scope.builder()
                    .project(AuthRequestWrapper.Project.builder().name(projectName).build())
                    .build())
                .build())
            .build();

        return request;
    }

    /**
     * 通过账号密码获取 Project Token
     *
     * @param user user name
     * @param domainName domain name
     * @param userPwd user password
     * @param project project name
     * @param endpoint iam endpoint，可选
     * @return 用户 token
     */
    public String getProjectTokenByPassword(String user, String domainName, String userPwd, String project,
                                            String endpoint) {
        AuthRequestWrapper request = AuthRequestWrapper.builder()
                .auth(AuthRequestWrapper.Auth.builder()
                        .identity(AuthRequestWrapper.Identity.builder()
                                .methods(Collections.singletonList("password"))
                                .password(PasswordCredential.builder()
                                        .user(PasswordCredential.User.builder()
                                                .name(user)
                                                .password(userPwd)
                                                .domain(PasswordCredential.Domain.builder().name(domainName).build())
                                                .build())
                                        .build())
                                .build())
                        .scope(AuthRequestWrapper.Scope.builder()
                                .project(AuthRequestWrapper.Project.builder().name(project).build())
                                .build())
                        .build())
                .build();
        return getTokenFromRequest(request, endpoint);
    }

    /**
     * 通过 ak/sk 获取 token
     *
     * @param ak access key
     * @param sk secret key
     * @param project project name
     * @return 用户 token
     */
    public String getProjectTokenByAkSk(String ak, String sk, String project, String endpoint) {
        AuthRequestWrapper request = AuthRequestWrapper.builder()
                .auth(AuthRequestWrapper.Auth.builder()
                        .identity(AuthRequestWrapper.Identity.builder()
                                .methods(Collections.singletonList("hw_ak_sk"))
                                .hw_ak_sk(new AkSkCredential(ak, sk))
                                .build())
                        .scope(AuthRequestWrapper.Scope.builder()
                                .project(AuthRequestWrapper.Project.builder().name(project).build())
                                .build())
                        .build())
                .build();
        return getTokenFromRequest(request, endpoint);
    }

    private String getTokenFromRequest(AuthRequestWrapper request, String endpoint) {
        ResponseEntity<TokenResponse> response;
        if (StringUtils.isBlank(endpoint)) {
            response = iamClient.getTokenByPassword(request);
        } else {
            response = iamClient.getTokenWithInvocation(request);
        }
        return response.getHeaders().getFirst(Constants.Header.X_SUBJECT_TOKEN);
    }
}
