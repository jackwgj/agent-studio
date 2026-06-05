/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.rce.client;

import com.openjiuwen.studio.agent.common.constant.Constants;
import com.openjiuwen.studio.agent.common.rce.model.ModelServiceInfo;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * model-manager服务client
 *
 */
@FeignClient(name = "modelManager", url = "${feign.client.config.modelManager.url:}")
public interface ModelManagerClient {
    /**
     * 根据模型deployment_id，查询模型推理服务详情信息
     *
     * @param authToken 认证token
     * @param serviceId serviceId
     * @return 模型详情信息
     */
    @GetMapping("/v1/model-service/inner/service-detail/{service_id}")
    ResponseEntity<ModelServiceInfo> queryModelInfo(@RequestHeader(Constants.Header.X_AUTH_TOKEN) String authToken,
        @PathVariable(value = "service_id") String serviceId);
}
