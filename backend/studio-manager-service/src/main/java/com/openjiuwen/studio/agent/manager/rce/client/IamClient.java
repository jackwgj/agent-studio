/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.client;

import com.alibaba.fastjson.JSONObject;
import com.openjiuwen.studio.agent.common.entity.AuthRequestWrapper;
import com.openjiuwen.studio.agent.common.entity.TokenResponse;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.manager.rce.models.iam.ListProjectsResp;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * iam接口调用类
 *
 */
@FeignClient(name = "iamClient", url = "${feign.client.config.iamClient.url:}")
public interface IamClient {
    String GEN_TOKEN_METHOD_TOKEN = "token";

    /**
     * 查询指定条件下的委托列表
     *
     */
    @GetMapping(value = "/v3.0/OS-AGENCY/agencies", name = "查询指定条件下的委托列表")
    ResponseEntity<JSONObject> queryAgencies(@RequestHeader(CommonConstant.X_AUTH_TOKEN) String authToken,
        @RequestParam(value = "domain_id") String domainId, @RequestParam(value = "name") String name,
        @RequestParam(value = "trust_domain_id") String trustDomainId);

    /**
     * 查询角色列表
     *
     */
    @GetMapping(value = "/v3/roles", name = "查询角色列表")
    ResponseEntity<JSONObject> queryRoles(@RequestHeader(CommonConstant.X_AUTH_TOKEN) String authToken,
        @RequestParam(value = "permission_type") String permissionType,
        @RequestParam(value = "display_name") String displayName);

    /**
     * getToken
     *
     * @param request request
     * @return ResponseEntity<TokenResponse>
     */
    @PostMapping(value = "/v3/auth/tokens", consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<TokenResponse> getToken(@RequestBody AuthRequestWrapper request);

    /**
     * queryProjects
     *
     * @param authToken authToken
     * @return ResponseEntity<ListProjectsResp>
     */
    @GetMapping(value = "/v3/projects", consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ListProjectsResp> listProjects(@RequestHeader(CommonConstant.X_AUTH_TOKEN) String authToken);

    /**
     * 委托授权
     *
     */
    @PutMapping(value = "/v3.0/OS-AGENCY/projects/{project_id}/agencies/{agency_id}/roles/{role_id}", name = "委托授权")
    ResponseEntity<JSONObject> agencyAuthorization(@RequestHeader(CommonConstant.X_AUTH_TOKEN) String authToken,
        @PathVariable(value = "project_id") String projectId, @PathVariable(value = "role_id") String roleId,
        @PathVariable(value = "agency_id") String agencyId);

    /**
     * 查询委托授权
     *
     */
    @RequestMapping(value = "/v3.0/OS-AGENCY/projects/{project_id}/agencies/{agency_id}/roles/{role_id}",
        method = RequestMethod.HEAD, name = "查询授权")
    ResponseEntity<JSONObject> hasAgencyAuthorization(@RequestHeader(CommonConstant.X_AUTH_TOKEN) String authToken,
        @PathVariable(value = "project_id") String projectId, @PathVariable(value = "role_id") String roleId,
        @PathVariable(value = "agency_id") String agencyId);

    /**
     * 查询IAM用户详情
     *
     */
    @GetMapping(value = "/v3.0/OS-USER/users/{user_id}", name = "查询IAM用户详情")
    ResponseEntity<com.alibaba.fastjson2.JSONObject> queryIamUserDetail(
        @RequestHeader(CommonConstant.X_AUTH_TOKEN) String authToken, @PathVariable(value = "user_id") String userId);
}