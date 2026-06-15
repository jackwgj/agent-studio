/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.client;

import com.alibaba.fastjson2.JSONObject;
import com.openjiuwen.studio.agent.manager.rce.models.KooSearchModelCheckReq;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 功能描述
 *
 */
@FeignClient(name = "jiuWenService", url = "${feign.client.config.jiuWenService.url:}")
public interface JiuWenClient {

    /**
     * JiuWen kooSearch embedding rerank ocr模型调用接口
     *
     * @param authToken String
     * @param kooSearchModelCheckReq kooSearchModelCheckReq
     * @return 响应体
     */
    @PostMapping("/v1/agentBuilder/search_model")
    ResponseEntity<JSONObject> askKooSearchModelCheck(@RequestHeader("X-Auth-Token") String authToken,
        @RequestBody KooSearchModelCheckReq kooSearchModelCheckReq);

    /**
     * JiuWen记忆删除接口
     *
     * @param authToken String
     * @param workflowId String
     */
    @DeleteMapping("/v1/workflow/{workflow_id}")
    Void deleteMemory(@RequestHeader("X-Auth-Token") String authToken,
        @PathVariable(value = "workflow_id") String workflowId);
}
