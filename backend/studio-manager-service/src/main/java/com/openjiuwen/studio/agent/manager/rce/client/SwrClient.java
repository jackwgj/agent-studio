/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.client;

import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.huaweicloud.sdk.swr.v2.model.ListNamespacesResponse;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * APIG接口调用类
 *
 */
@FeignClient(name = "swrClient", url = "${feign.client.config.swrClient.url:}")
public interface SwrClient {
    @GetMapping(value = "/v2/manage/namespaces", name = "查询组织列表")
    ResponseEntity<ListNamespacesResponse> listNamespaces(
        @RequestHeader(CommonConstant.X_AUTH_TOKEN) String authToken,
        @RequestParam(value = "namespace") String namespace,
        @RequestParam(value = "filter") String filter);
}