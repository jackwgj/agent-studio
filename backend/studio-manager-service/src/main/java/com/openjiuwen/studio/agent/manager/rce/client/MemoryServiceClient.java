/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.client;

import com.openjiuwen.studio.agent.common.constant.Constants;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.manager.rce.models.memory.BatchDeleteMemoryRepositoryRequestBody;
import com.openjiuwen.studio.agent.manager.rce.models.memory.CommonResponseBody;
import com.openjiuwen.studio.agent.manager.rce.models.memory.MemoryOperationCommonResponse;
import com.openjiuwen.studio.agent.manager.rce.models.memory.SaveOrUpdateMemoryRepositoryRequestBody;
import com.openjiuwen.studio.agent.manager.rce.models.memory.UserProfileDeleteRequest;
import com.openjiuwen.studio.agent.manager.rce.models.memory.UserProfileModifyRequest;
import com.openjiuwen.studio.agent.manager.rce.models.memory.UserProfileTopicDeleteResponse;
import com.openjiuwen.studio.agent.manager.rce.models.memory.UserProfileTopicModifyResponse;

import jakarta.validation.Valid;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * memory-service客户端
 *
 */
@FeignClient(name = "memoryService", url = "${feign.client.config.memoryService.url:}")
public interface MemoryServiceClient {

    /**
     * 更新用户画像定义设置
     *
     * @param authToken 认证token
     * @param language  语言标识
     * @param projectId 项目ID
     * @param request   修改请求
     * @return 修改响应
     */
    @PostMapping("/private/v1/{project_id}/memories/profile/config")
    ResponseEntity<UserProfileTopicModifyResponse> modifyUserProfileConfig(
        @RequestHeader(CommonConstant.X_AUTH_TOKEN) String authToken,
        @RequestHeader(Constants.Header.X_LANGUAGE) String language,
        @PathVariable(value = "project_id") String projectId, @RequestBody UserProfileModifyRequest request);

    /**
     * 删除用户画像定义设置
     *
     * @param authToken 认证token
     * @param language  语言标识
     * @param projectId 项目ID
     * @param request   修改请求
     * @return 修改响应
     */
    @PostMapping("/private/v1/{project_id}/memories/profile/config/delete")
    ResponseEntity<UserProfileTopicDeleteResponse> deleteUserProfileConfig(
        @RequestHeader(CommonConstant.X_AUTH_TOKEN) String authToken,
        @RequestHeader(Constants.Header.X_LANGUAGE) String language,
        @PathVariable(value = "project_id") String projectId, @RequestBody UserProfileDeleteRequest request);

    /**
     * 删除某个应用下的所有长期记忆（包括存储在向量库中的自然语言的记忆还有存储到Redis中的topic记忆）
     *
     * @param authToken 认证token
     * @param language  语言标识
     * @param projectId 项目ID
     * @param appId     应用ID，可以是智能体ID，也可以是工作流ID
     * @return 修改响应
     */
    @DeleteMapping("/private/v1/{project_id}/long-term-memories")
    ResponseEntity<MemoryOperationCommonResponse> deleteLongTermMemories(
        @RequestHeader(CommonConstant.X_AUTH_TOKEN) String authToken,
        @RequestHeader(Constants.Header.X_LANGUAGE) String language,
        @PathVariable(value = "project_id") String projectId, @RequestParam(value = "app_id") String appId);


    /**
     * 保存或更新记忆库配置
     */
    @PostMapping("/v1/{project_id}/memories/memory-repositories/config")
    ResponseEntity<CommonResponseBody> saveOrUpdateMemoryRepositoryConfig(
        @RequestHeader(CommonConstant.X_AUTH_TOKEN) String authToken,
        @RequestHeader(Constants.Header.X_LANGUAGE) String language,
        @PathVariable(value = "project_id") String projectId,
        @RequestBody @Valid SaveOrUpdateMemoryRepositoryRequestBody body);

    /**
     * 批量删除记忆库配置
     */
    @DeleteMapping("/v1/{project_id}/memories/memory-repositories/config")
    ResponseEntity<CommonResponseBody> batchDeleteMemoryRepositoryConfig(
        @RequestHeader(CommonConstant.X_AUTH_TOKEN) String authToken,
        @RequestHeader(Constants.Header.X_LANGUAGE) String language,
        @PathVariable(value = "project_id") String projectId,
        @RequestBody @Valid BatchDeleteMemoryRepositoryRequestBody body);
}
