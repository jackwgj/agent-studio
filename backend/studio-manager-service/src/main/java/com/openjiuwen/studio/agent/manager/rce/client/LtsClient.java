/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.client;

import com.alibaba.fastjson.JSONObject;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * APIG接口调用类
 *
 */
@FeignClient(name = "ltsClient", url = "${feign.client.config.ltsClient.url:}")
public interface LtsClient {
    @PostMapping(value = "/v2/{project_id}/groups/{log_group_id}/streams/{log_stream_id}/content/query", name = "查询指定日志流下的日志内容。")
    ResponseEntity<JSONObject> queryLtsLogStream(
        @RequestHeader
        HttpHeaders headers,
        @PathVariable(name = "project_id") String projectId,
        @PathVariable(name = "log_group_id") String logGroupId,
        @PathVariable(name = "log_stream_id") String logStreamId,
        @RequestBody String jsonModel);
}