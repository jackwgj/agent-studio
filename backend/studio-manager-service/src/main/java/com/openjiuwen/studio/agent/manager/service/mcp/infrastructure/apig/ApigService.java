/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service.mcp.infrastructure.apig;

import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.rce.service.ClientService;
import com.openjiuwen.studio.agent.manager.service.mcp.model.apig.ApigApiGroupDetailResp;
import com.openjiuwen.studio.agent.manager.service.mcp.model.apig.ApigBindDomainCertificateReq;
import com.openjiuwen.studio.agent.manager.service.mcp.model.apig.ApigBindDomainsReq;
import com.openjiuwen.studio.agent.manager.service.mcp.model.apig.ApigBindDomainsResp;
import com.openjiuwen.studio.agent.manager.service.mcp.properties.IamProperties;
import com.openjiuwen.studio.agent.manager.utils.IamServiceUtils;
import com.openjiuwen.studio.agent.manager.utils.McpServiceExceptionUtils;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * APIG service
 *
 */
@Slf4j
@Service
public class ApigService {
    @Resource
    private ClientService clientService;

    @Resource
    private IamProperties iamProperties;

    @Resource
    private IamServiceUtils iamServiceUtils;

    @Value("${agent.builder.functionGraph.template.mcp.apigwInstance:}")
    private String apigwInstanceId;

    @Value("${iam.hisAccount.rotate.enable:true}")
    private Boolean hisAccountRotate;

    public ApigBindDomainsResp bindDomains(String groupId, ApigBindDomainsReq apigBindDomainsReq) {
        HttpHeaders headers = new HttpHeaders();
        String token;
        String projectId = iamProperties.getProjectId();
        ResponseEntity<ApigBindDomainsResp> responseEntity;
        token = RequestContextUtils.getRequestAuthToken();
        headers.add("x-auth-token", token);
        responseEntity = clientService.bindDomains(RequestContextUtils.getRequestAuthToken(), projectId,
            apigwInstanceId, groupId, apigBindDomainsReq);

        if (null == responseEntity) {
            return null;
        }
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            if (ObjectUtils.isNotEmpty(responseEntity.getBody())) {
                log.info("apig bind domains success.");
                return responseEntity.getBody();
            } else {
                log.error("apig bind domains failed! body is empty.");
                McpServiceExceptionUtils.throwRemoteCallException("apig bind domains",
                    "apig bind domains failed! body is empty.");
            }
        } else {
            log.error("apig bind domains failed! code is {}", responseEntity.getStatusCode());
            McpServiceExceptionUtils.throwRemoteCallException("apig bind domains", "apig bind domains failed!");
        }
        return null;
    }

    public ApigApiGroupDetailResp apigApiGroupDetail(String groupId) {
        HttpHeaders headers = new HttpHeaders();
        String token;
        String projectId = iamProperties.getProjectId();
        ResponseEntity<ApigApiGroupDetailResp> responseEntity;

        token = RequestContextUtils.getRequestAuthToken();
        headers.add("x-auth-token", token);
        responseEntity = clientService.apigApiGroupDetail(token, projectId, apigwInstanceId, groupId);

        if (null == responseEntity) {
            return null;
        }
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            if (ObjectUtils.isNotEmpty(responseEntity.getBody())) {
                log.info("apig group detail success.");
                return responseEntity.getBody();
            } else {
                log.error("apig group detail failed! body is empty.");
                McpServiceExceptionUtils.throwRemoteCallException("apig group detail",
                    "apig bind domains failed! body is empty.");
            }
        } else {
            log.error("apig group detail failed! code is {}", responseEntity.getStatusCode());
            McpServiceExceptionUtils.throwRemoteCallException("apig group detail", "apig group detail failed!");
        }
        return null;
    }

    public void bindDomainsCertificate(String groupId, String domainId,
        ApigBindDomainCertificateReq apigBindDomainCertificateReq) {
        HttpHeaders headers = new HttpHeaders();
        String token;
        String projectId = iamProperties.getProjectId();
        token = RequestContextUtils.getRequestAuthToken();
        headers.add("x-auth-token", token);
        clientService.bindDomainsCertificate(token, projectId, apigwInstanceId, groupId, domainId,
            apigBindDomainCertificateReq);
    }
}
