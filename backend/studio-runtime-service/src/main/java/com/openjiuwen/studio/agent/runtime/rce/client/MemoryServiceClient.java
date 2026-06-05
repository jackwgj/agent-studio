/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.rce.client;

import com.openjiuwen.studio.agent.runtime.dto.DeleteLongTermMemoriesRequestBody;
import com.openjiuwen.studio.agent.runtime.dto.ListLongTermMemoriesResponseBody;
import com.openjiuwen.studio.agent.runtime.dto.UpdateLongTermMemoriesRequestBody;
import com.openjiuwen.studio.agent.runtime.rce.model.memory.BatchDeleteUserMemoriesResponseBody;
import com.openjiuwen.studio.agent.runtime.rce.model.memory.MemoryServiceOperationResponse;
import com.openjiuwen.studio.agent.runtime.rce.model.memory.UpdateUserProfileValuesResponseBody;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "memoryService", url = "${feign.client.config.memoryService.url:}")
public interface MemoryServiceClient {
    @GetMapping("/private/v1/{project_id}/long-term-memories")
    ResponseEntity<ListLongTermMemoriesResponseBody> listLongTermMemories(
        @RequestHeader("X-Auth-Token") String authToken,
        @PathVariable(value = "project_id") String projectId,
        @RequestParam(value = "memory_repo_id") String memoryRepoId,
        @RequestParam(value = "mem_type") String memoryType, @RequestParam(value = "offset") Integer offset,
        @RequestParam(value = "limit") Integer limit);

    @PutMapping("/private/v1/{project_id}/long-term-memories")
    ResponseEntity<UpdateUserProfileValuesResponseBody> updateLongTermMemories(
        @RequestHeader("X-Auth-Token") String authToken,
        @PathVariable(value = "project_id") String projectId, @RequestParam(value = "memory_repo_id") String memoryRepoId,
        @RequestBody UpdateLongTermMemoriesRequestBody body);

    @DeleteMapping("/private/v1/{project_id}/long-term-memories/batch-delete")
    ResponseEntity<BatchDeleteUserMemoriesResponseBody> deleteLongTermMemories(
        @RequestHeader("X-Auth-Token") String authToken,
        @PathVariable(value = "project_id") String projectId,
        @RequestParam(value = "memory_repo_id") String memoryRepoId,
        @RequestBody DeleteLongTermMemoriesRequestBody body);

    @DeleteMapping("/private/v1/{project_id}/long-term-memories")
    ResponseEntity<MemoryServiceOperationResponse> clearLongTermMemories(
        @RequestHeader("X-Auth-Token") String authToken,
        @PathVariable(value = "project_id") String projectId,
        @RequestParam(value = "memory_repo_id") String memoryRepoId, @RequestParam(value = "user_id") String userId);
}
