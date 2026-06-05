/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.rce.client;

import com.openjiuwen.studio.agent.common.constant.Constants;
import com.openjiuwen.studio.agent.runtime.constant.Constant;
import com.openjiuwen.studio.agent.runtime.dto.BatchDeleteUserVariableMemoryRequestBody;
import com.openjiuwen.studio.agent.common.dto.BatchDeleteUserVariableMemoryResponseBody;
import com.openjiuwen.studio.agent.runtime.dto.PromptBuilderReq;
import com.openjiuwen.studio.agent.common.dto.run.ResetUserVariableMemoryResponseBody;
import com.openjiuwen.studio.agent.runtime.rce.model.JiuWenDeleteIrReq;
import com.openjiuwen.studio.agent.runtime.rce.model.JiuWenDeleteIrRsp;
import com.openjiuwen.studio.agent.runtime.rce.model.memory.RetrieveUserVariableMemoriesRequestBody;
import com.openjiuwen.studio.agent.runtime.rce.model.memory.RetrieveUserVariableMemoriesResponseBody;

import reactor.core.publisher.Flux;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "jiuWenService", url = "${feign.client.config.jiuWenService.url:}")
public interface JiuWenClient {
    @DeleteMapping("/v1/orchestration/ir/execute")
    JiuWenDeleteIrRsp deleteIr(@RequestHeader("X-Auth-Token") String authToken,
        @RequestBody JiuWenDeleteIrReq request);

    @PostMapping("/v1/prompt/build")
    Flux<Object> promptBuildStream(@RequestHeader(Constant.Common.X_AUTH_TOKEN) String token,
        @RequestHeader(Constant.Common.X_WORKSPACE_ID) String workspaceId, @RequestBody PromptBuilderReq request);

    @PostMapping(
        "/private/v2/{project_id}/agent-base/runtime/agents/{agent_id}/users/{user_id}/memories/variables/batch-delete")
    ResponseEntity<BatchDeleteUserVariableMemoryResponseBody> batchDeleteUserVariableMemory(
        @RequestHeader(Constants.Header.X_AUTH_TOKEN) String requestAuthToken,
        @RequestHeader(Constants.Header.X_LANGUAGE) String language,
        @PathVariable(value = "project_id") String projectId, @PathVariable(value = "agent_id") String agentId,
        @PathVariable(value = "user_id") String userId, @RequestBody BatchDeleteUserVariableMemoryRequestBody body);

    @PostMapping(
        "/private/v2/{project_id}/agent-base/runtime/agents/{agent_id}/users/{user_id}/memories/variables/reset")
    ResponseEntity<ResetUserVariableMemoryResponseBody> resetUserVariableMemory(
        @RequestHeader(Constants.Header.X_AUTH_TOKEN) String requestAuthToken,
        @RequestHeader(Constants.Header.X_LANGUAGE) String language,
        @PathVariable(value = "project_id") String projectId, @PathVariable(value = "agent_id") String agentId,
        @PathVariable(value = "user_id") String userId);

    @PostMapping(
        "/private/v2/{project_id}/agent-base/runtime/agents/{agent_id}/users/{user_id}/memories/variables/retrieve")
    ResponseEntity<RetrieveUserVariableMemoriesResponseBody> retrieveUserVariableMemories(
        @RequestHeader(Constants.Header.X_AUTH_TOKEN) String requestAuthToken,
        @RequestHeader(Constants.Header.X_LANGUAGE) String language,
        @PathVariable(value = "project_id") String projectId, @PathVariable(value = "agent_id") String agentId,
        @PathVariable(value = "user_id") String userId,
        @RequestBody RetrieveUserVariableMemoriesRequestBody requestBody);
}
