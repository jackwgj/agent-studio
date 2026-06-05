/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.client;

import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.manager.dto.maas.ImageGenerationRequest;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 调用maas接口
 *
 */
@FeignClient(name = "maasModel", url = "${feign.client.config.maasModel.url:}")
public interface MaasModelClient {

    /**
     * 调用Maas服务生成图片
     *
     * @param authorization authorization认证信息
     * @param body 请求体
     * @return ResponseEntity<String>
     */
    @PostMapping("/v1/images/generations")
    ResponseEntity<String> generateImage(@RequestHeader(CommonConstant.AUTHORIZATION) String authorization,
        @RequestBody ImageGenerationRequest body);
}