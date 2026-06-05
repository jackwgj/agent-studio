/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.client;

import com.openjiuwen.studio.agent.manager.rce.models.CustomModelListRsp;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 功能描述
 *
 */
@FeignClient(name = "customModelManager", url = "${feign.client.config.customModelManager.url:}")
public interface CustomModelManagerClient {
    /**
     * CUSTOM模型管理接口
     *
     * @param userId 用户id
     * @return 模型清单
     */
    @GetMapping("/aiagent/agent/getAvailableModelByUserId.htm")
    ResponseEntity<CustomModelListRsp> getAvailableModelByUserId(@RequestParam(value = "userId") String userId,
                                                                 @RequestParam(value = "projectId") String projectId);
}
