/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.client;

import com.openjiuwen.studio.agent.manager.rce.models.PetalSearchRsp;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * petal search服务的client
 *
 */
@FeignClient(name = "petalSearch", url = "${feign.client.config.petalSearch.url:}")
public interface PetalSearchClient {

    /**
     * web搜索服务调用接口
     *
     * @param query 搜索字段参数
     * @param limit 搜索返回数目限制
     * @return PetalSearchRsp 搜索结果
     */
    @GetMapping("/v1/finance/applications/custom/websearch")
    PetalSearchRsp getPetalSearchResult(
        @RequestHeader("X-Auth-Token") String authToken,
        @RequestHeader("X-Language") String language,
        @RequestParam String query,
        @RequestParam Integer limit);
}