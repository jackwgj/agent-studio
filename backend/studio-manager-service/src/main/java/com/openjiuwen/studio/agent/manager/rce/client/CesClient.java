/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.client;

import com.alibaba.fastjson.JSONObject;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * APIG接口调用类
 *
 */
@FeignClient(name = "cseClient", url = "${feign.client.config.cseClient.url:}")
public interface CesClient {
    @GetMapping(value = "/V1.0/{project_id}/metric-data", name = "查询指定时间范围指定指标的指定粒度的监控数据，可以通过参数指定需要查询的数据维度。")
    ResponseEntity<JSONObject> queryMetricData(
        @RequestHeader
        HttpHeaders headers,
        @PathVariable(name = "project_id") String projectId,
        @RequestParam(value = "namespace") String namespace,
        @RequestParam(value = "metric_name") String metricName,
        @RequestParam(value = "from") long from,
        @RequestParam(value = "to") long to,
        @RequestParam(value = "period") Integer period,
        @RequestParam(value = "filter") String filter,
        @RequestParam(value = "dim.0") String dim);
}