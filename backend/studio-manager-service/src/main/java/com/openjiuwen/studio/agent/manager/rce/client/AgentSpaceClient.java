/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.client;

import com.alibaba.fastjson2.JSONObject;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.manager.entity.ReleaseChannel;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * AgentBuilder空间服务client
 */
@FeignClient(name = "agent-builder", url = "${feign.client.config.agent-builder.url:}")
public interface AgentSpaceClient {
    /**
     * 查询发布状态
     *
     * @param authToken 认证token
     * @param workspaceId 工作空间id
     * @param applicationId 应用id
     * @return JSONObject
     */
    @GetMapping("/agentspace/internal-openapi/v1/agent-builder/agent/publish-info/{workspace_id}/{application_id}")
    ResponseEntity<JSONObject> publish(@RequestHeader(CommonConstant.X_AUTH_TOKEN) String authToken, @PathVariable(
        value = "workspace_id") String workspaceId, @PathVariable(value = "application_id") String applicationId);

    /**
     * 发布
     *
     * @param authToken 认证token
     * @param body 发布请求body体
     * @return JSONObject
     */
    @PostMapping("/agentspace/internal-openapi/v1/agent-builder/agent/publish")
    ResponseEntity<JSONObject> publish(@RequestHeader(CommonConstant.X_AUTH_TOKEN) String authToken,
        @RequestBody ReleaseChannel body);

    /**
     * 取消发布
     *
     * @param authToken 认证token
     * @param workspaceId 工作空间id
     * @param applicationId 应用id
     * @return JSONObject
     */
    @PostMapping("/agentspace/internal-openapi/v1/agent-builder/agent/unpublish/{workspace_id}/{application_id}")
    ResponseEntity<JSONObject> unpublish(@RequestHeader(CommonConstant.X_AUTH_TOKEN) String authToken, @PathVariable(
        value = "workspace_id") String workspaceId, @PathVariable(value = "application_id") String applicationId);

}