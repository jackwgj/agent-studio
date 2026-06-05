/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.client;

import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.manager.service.mcp.model.apig.ApigApiGroupDetailResp;
import com.openjiuwen.studio.agent.manager.service.mcp.model.apig.ApigBindDomainCertificateReq;
import com.openjiuwen.studio.agent.manager.service.mcp.model.apig.ApigBindDomainsReq;
import com.openjiuwen.studio.agent.manager.service.mcp.model.apig.ApigBindDomainsResp;
import com.openjiuwen.studio.agent.manager.service.mcp.model.apig.ApigGroupResp;
import com.huaweicloud.sdk.apig.v2.model.ApiActionInfo;
import com.huaweicloud.sdk.apig.v2.model.ApiAuthCreate;
import com.huaweicloud.sdk.apig.v2.model.ApiCreate;
import com.huaweicloud.sdk.apig.v2.model.ApiGroupCreate;
import com.huaweicloud.sdk.apig.v2.model.CreateApiGroupV2Response;
import com.huaweicloud.sdk.apig.v2.model.CreateApiV2Response;
import com.huaweicloud.sdk.apig.v2.model.DeleteApiV2Response;
import com.huaweicloud.sdk.apig.v2.model.ListInstancesV2Response;
import com.huaweicloud.sdk.apig.v2.model.ShowDetailsOfInstanceV2Response;

import jakarta.validation.Valid;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * APIG接口调用类
 *
 */
@FeignClient(name = "apigClient", url = "${feign.client.config.apigClient.url:}")
public interface ApigClient {
    @GetMapping(value = "/v2/{project_id}/apigw/instances", name = "查询专享版实例列表")
    ResponseEntity<ListInstancesV2Response> listInstancesV2(@RequestHeader(CommonConstant.X_AUTH_TOKEN) String authToken,
        @PathVariable(name = "project_id") String projectId,
        @RequestParam(value = "status", required = false) String status,
        @RequestParam(value = "offset", required = false) int offset,
        @RequestParam(value = "limit", required = false) int limit);

    @PostMapping(value = "/v2/{project_id}/apigw/instances/{instance_id}/api-groups/{group_id}/domains",
        name = "绑定域名")
    ResponseEntity<ApigBindDomainsResp> bindDomains(@RequestHeader(CommonConstant.X_AUTH_TOKEN) String authToken,
        @PathVariable(name = "project_id") String projectId, @PathVariable(name = "instance_id") String instanceId,
        @PathVariable(name = "group_id") String groupId, @RequestBody ApigBindDomainsReq apigBindDomainsReq);

    @GetMapping(value = "/v2/{project_id}/apigw/instances/{instance_id}/api-groups/{group_id}",
        name = "查询指定分组的详细信息")
    ResponseEntity<ApigApiGroupDetailResp> apigApiGroupDetail(
        @RequestHeader(CommonConstant.X_AUTH_TOKEN) String authToken,
        @PathVariable(name = "project_id") String projectId, @PathVariable(name = "instance_id") String instanceId,
        @PathVariable(name = "group_id") String groupId);

    @PostMapping(
        value = "/v2/{project_id}/apigw/instances/{instance_id}/api-groups/{group_id}/domains/{domain_id}/certificates/attach",
        name = "绑定域名证书")
    ResponseEntity<ApigBindDomainsResp> bindDomainsCertificate(
        @RequestHeader(CommonConstant.X_AUTH_TOKEN) String authToken,
        @PathVariable(name = "project_id") String projectId, @PathVariable(name = "instance_id") String instanceId,
        @PathVariable(name = "group_id") String groupId, @PathVariable(name = "domain_id") String domainId,
        @Valid @RequestBody ApigBindDomainCertificateReq apigBindDomainCertificateReq);

    @PostMapping(value = "/v2/{project_id}/apigw/instances/{instance_id}/apis")
    ResponseEntity<CreateApiV2Response> createApi(@RequestHeader(CommonConstant.X_AUTH_TOKEN) String authToken,
        @PathVariable(name = "project_id") String projectId, @PathVariable(name = "instance_id") String instanceId,
        @Valid @RequestBody ApiCreate request);

    @PostMapping(value = "/v2/{project_id}/apigw/instances/{instance_id}/app-auths")
    ResponseEntity<String> bindCredentials(@RequestHeader(CommonConstant.X_AUTH_TOKEN) String authToken,
        @PathVariable(name = "project_id") String projectId, @PathVariable(name = "instance_id") String instanceId,
        @Valid @RequestBody ApiAuthCreate apiAuthCreate);

    @DeleteMapping(value = "/v2/{project_id}/apigw/instances/{instance_id}/apis/{api_id}")
    ResponseEntity<DeleteApiV2Response> deleteApi(@RequestHeader(CommonConstant.X_AUTH_TOKEN) String authToken,
        @PathVariable(name = "project_id") String projectId, @PathVariable(name = "instance_id") String instanceId,
        @PathVariable(name = "api_id") String apiId);

    @PostMapping(value = "/v2/{project_id}/apigw/instances/{instance_id}/api-groups")
    ResponseEntity<CreateApiGroupV2Response> createApiGroup(
        @RequestHeader(CommonConstant.X_AUTH_TOKEN) String authToken,
        @PathVariable(value = "project_id") String projectId, @PathVariable(value = "instance_id") String instanceId,
        @RequestBody ApiGroupCreate request);

    @GetMapping(value = "/v2/{project_id}/apigw/instances/{instance_id}/api-groups",
            name = "查询指定分组列表")
    ResponseEntity<ApigGroupResp> listApiGroups(
            @RequestHeader(CommonConstant.X_AUTH_TOKEN) String authToken,
            @PathVariable(name = "project_id") String projectId, @PathVariable(name = "instance_id") String instanceId,
            @RequestParam(name = "name") String name);


    @PostMapping(value = "/v2/{project_id}/apigw/instances/{instance_id}/apis/action")
    ResponseEntity<String> publishOrOfflineApi(@RequestHeader(CommonConstant.X_AUTH_TOKEN) String authToken,
        @PathVariable(value = "project_id") String projectId, @PathVariable(value = "instance_id") String instanceId,
        @RequestBody ApiActionInfo request);

    @GetMapping(value = "/v2/{project_id}/apigw/instances/{instance_id}")
    ResponseEntity<ShowDetailsOfInstanceV2Response> showDetailsOfInstanceV2(@RequestHeader(CommonConstant.X_AUTH_TOKEN) String authToken,
        @PathVariable(value = "project_id") String projectId, @PathVariable(value = "instance_id") String instanceId);
}