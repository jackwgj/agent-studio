/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.rce.client;

import com.alibaba.fastjson2.JSONObject;
import com.openjiuwen.studio.agent.runtime.rce.model.CreateBackflowReq;

import jakarta.validation.Valid;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 数据工程服务接口客户端
 *
 */
@FeignClient(name = "dataManager", url = "${feign.client.config.dataManager.url:}")
public interface DataManagerClient {
    /**
     * 反馈数据上报接口
     *
     * @param authToken String
     * @param req req
     */
    @PostMapping("/v1/{project_id}/workspaces/{workspace_id}/data-backflow/data")
    ResponseEntity<JSONObject> createBackflowJob(@RequestHeader("X-Auth-Token") String authToken,
        @PathVariable(value = "project_id") String projectId, @PathVariable(value = "workspace_id") String workspaceId,
        @Valid @RequestBody CreateBackflowReq req);
}
