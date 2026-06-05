/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.client;

import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.huaweicloud.sdk.nat.v2.model.ListNatGatewaysResponse;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * nat client
 *
 */
@FeignClient(name = "natClient", url = "${feign.client.config.natClient.url:}")
public interface NatClient {
    @GetMapping(value = "/v2/{project_id}/nat_gateways", name = "查询nat网关列表")
    ResponseEntity<ListNatGatewaysResponse> ListNatGateways(
        @RequestHeader(CommonConstant.X_AUTH_TOKEN)
        String authToken,
        @PathVariable(name = "project_id")
        String projectId,
        @RequestParam(value = "status")
        String status);
}