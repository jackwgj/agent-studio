/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.client;

import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.huaweicloud.sdk.eip.v3.model.ListPublicipsResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * eip client
 *
 */
@FeignClient(name = "eipClient", url = "${feign.client.config.eipClient.url:}")
public interface EipClient {
    @GetMapping(value = "/v3/{project_id}/eip/publicips", name = "查询弹性公网ip列表")
    ResponseEntity<ListPublicipsResponse> ListEipPublicIps(
        @RequestHeader(CommonConstant.X_AUTH_TOKEN)
        String authToken,
        @PathVariable(name = "project_id")
        String projectId,
        @RequestParam(value = "marker")
        String marker,
        @RequestParam(value = "offset")
        int offset,
        @RequestParam(value = "limit")
        int limit,
        @RequestParam(value = "id")
        @Valid @Size(max = 100) List<String> id,
        @RequestParam(value = "ip_version")
        @Valid @Size(max = 100) List<Integer> ipVersion,
        @RequestParam(value = "type")
        @Valid @Size(max = 100) List<String> type,
        @RequestParam(value = "status")
        @Valid @Size(max = 100) List<String> status);
}