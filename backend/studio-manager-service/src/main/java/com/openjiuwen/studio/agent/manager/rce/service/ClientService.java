/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.service;

import com.alibaba.fastjson.JSONObject;
import com.openjiuwen.studio.agent.manager.rce.client.ApigClient;
import com.openjiuwen.studio.agent.manager.rce.client.CesClient;
import com.openjiuwen.studio.agent.manager.rce.client.IamClient;
import com.openjiuwen.studio.agent.manager.rce.client.LtsClient;
import com.openjiuwen.studio.agent.manager.service.mcp.model.apig.ApigApiGroupDetailResp;
import com.openjiuwen.studio.agent.manager.service.mcp.model.apig.ApigBindDomainCertificateReq;
import com.openjiuwen.studio.agent.manager.service.mcp.model.apig.ApigBindDomainsReq;
import com.openjiuwen.studio.agent.manager.service.mcp.model.apig.ApigBindDomainsResp;
import com.openjiuwen.studio.agent.manager.utils.McpServiceExceptionUtils;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 *
 * @Date: 2024/12/4 15:55
 * @Description: client服务
 */

@Slf4j
@Service
public class ClientService {

    private final ApigClient apigClient;

    private final CesClient cesClient;

    private final LtsClient ltsClient;

    private final IamClient iamClient;

    @Autowired
    public ClientService(ApigClient apigClient, CesClient cesClient,
        LtsClient ltsClient, IamClient iamClient) {
        this.apigClient = apigClient;
        this.cesClient = cesClient;
        this.ltsClient = ltsClient;
        this.iamClient = iamClient;
    }


    public ResponseEntity<JSONObject> queryMetricData(HttpHeaders headers, String projectId, String namespace,
        String metricName, String from, String to, int period, String filter, String dim) {
        try {
            return cesClient.queryMetricData(headers, projectId, namespace, metricName, Long.parseLong(from),
                Long.parseLong(to), period, filter, dim);
        } catch (Exception feignException) {
            McpServiceExceptionUtils.printException(log, feignException, "query ces metric data fail");
            return null;
        }
    }


    public ResponseEntity<ApigBindDomainsResp> bindDomains(String token, String projectId, String instanceId,
        String groupId, ApigBindDomainsReq apigBindDomainsReq) {
        try {
            return apigClient.bindDomains(token, projectId, instanceId, groupId, apigBindDomainsReq);
        } catch (Exception feignException) {
            McpServiceExceptionUtils.printException(log, feignException, "bind domains fail");
            return null;
        }
    }

    public ResponseEntity<ApigApiGroupDetailResp> apigApiGroupDetail(String token, String projectId,
        String instanceId, String groupId) {
        try {
            return apigClient.apigApiGroupDetail(token, projectId, instanceId, groupId);
        } catch (Exception feignException) {
            McpServiceExceptionUtils.printException(log, feignException, "bind domains fail");
            return null;
        }
    }

    public ResponseEntity<ApigBindDomainsResp> bindDomainsCertificate(String token, String projectId,
        String instanceId, String groupId, String domainId, ApigBindDomainCertificateReq apigBindDomainCertificateReq) {
        try {
            return apigClient.bindDomainsCertificate(token, projectId, instanceId, groupId, domainId,
                apigBindDomainCertificateReq);
        } catch (Exception feignException) {
            McpServiceExceptionUtils.printException(log, feignException, "bind domains fail");
            return null;
        }
    }


    /**
     * 查询委托
     *
     */
    public ResponseEntity<JSONObject> queryAgencies(String token, String domainId, String name, String trustDomainId) {
        try {
            return iamClient.queryAgencies(token, domainId, name, trustDomainId);
        } catch (Exception feignException) {
            McpServiceExceptionUtils.printException(log, feignException, "iam query agencies fail");
            McpServiceExceptionUtils.throwRemoteCallException("iam", "iam query agencies fail!");
        }
        return ResponseEntity.internalServerError().build();
    }

    /**
     * 查询角色
     *
     */
    public ResponseEntity<JSONObject> queryRoles(String token, String permissionType, String displayName) {
        try {
            return iamClient.queryRoles(token, permissionType, displayName);
        } catch (Exception feignException) {
            McpServiceExceptionUtils.printException(log, feignException, "iam query role fail");
            McpServiceExceptionUtils.throwRemoteCallException("iam", "iam query role fail!");
        }
        return ResponseEntity.internalServerError().build();
    }

    /**
     * 委托授权
     *
     */
    public ResponseEntity<JSONObject> agencyAuthorization(String token, String projectId, String roleId,
        String agencyId) {
        try {
            return iamClient.agencyAuthorization(token, projectId, roleId, agencyId);
        } catch (Exception feignException) {
            McpServiceExceptionUtils.printException(log, feignException, "iam role authorization fail");
            McpServiceExceptionUtils.throwRemoteCallException("iam", "iam role authorization fail!");
        }
        return ResponseEntity.internalServerError().build();
    }

    public ResponseEntity<com.alibaba.fastjson2.JSONObject> queryIamUserDetail(String token, String userId) {
        try {
            return iamClient.queryIamUserDetail(token, userId);
        } catch (Exception feignException) {
            McpServiceExceptionUtils.printException(log, feignException, "iam user fail");
            McpServiceExceptionUtils.throwRemoteCallException("iam", "iam user fail!");
        }
        return ResponseEntity.internalServerError().build();
    }

    /**
     * 查询委托授权
     *
     */
    public ResponseEntity<JSONObject> hasAgencyAuthorization(String token, String projectId, String roleId,
        String agencyId) {
        try {
            return iamClient.hasAgencyAuthorization(token, projectId, roleId, agencyId);
        } catch (Exception feignException) {
            McpServiceExceptionUtils.printException(log, feignException, "iam query role authorization fail");
            McpServiceExceptionUtils.throwRemoteCallException("iam", "iam query role authorization fail!");
        }
        return ResponseEntity.internalServerError().build();
    }

}

