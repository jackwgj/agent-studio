/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.client;

import com.alibaba.fastjson2.JSONObject;
import com.openjiuwen.studio.agent.manager.service.mcp.model.ElementBasicInfo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 模型中心微服务接口调用FeignClient
 */
@FeignClient(name = "managementCenterFeignClient", url = "${feign.client.config.managementCenterFeignClient.url:}")
public interface ManagementCenterFeignClient {

    @GetMapping(value = "/v1/common/project/{project-id}/elements/{element-type}", name = "公共中心")
    ResponseEntity<JSONObject> fetchElementsForProject(@RequestHeader HttpHeaders header, @PathVariable("project-id") String projectId, @PathVariable("element-type") String elementType, @RequestParam(value = "description") String description, @RequestParam(value = "owner-name") String ownerName);

    @GetMapping(value = "/v1/common/project/elements/validate", name = "查询元素状态")
    ResponseEntity<JSONObject> elementsHasBelongToProject(@RequestHeader HttpHeaders header, @RequestBody @Valid @Size(max = 20) List<ElementBasicInfo> req);
}