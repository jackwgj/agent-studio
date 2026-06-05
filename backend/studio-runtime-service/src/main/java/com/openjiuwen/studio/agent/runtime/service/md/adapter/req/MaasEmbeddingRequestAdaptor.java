/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.service.md.adapter.req;

import com.openjiuwen.studio.agent.runtime.dto.md.EmbeddingRequest;
import com.openjiuwen.studio.agent.runtime.service.md.adapter.req.mapper.MaasRequestMapper;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * MaasEmbeddingRequestAdaptor
 * 针对MAAS-Embedding模型调用的适配器, 完成模型请求参数的转换等;
 *
 */
@Slf4j
@Component
public class MaasEmbeddingRequestAdaptor extends AbstractRequestAdapter {
    @Override
    public String getName() {
        return "maas_embedding";
    }

    @Override
    public Object requestBodyConvert(Map<String, String> headers, Object body, boolean stream) {
        if (!(body instanceof EmbeddingRequest)) {
            log.error("Not a 'EmbeddingRequest', do none conversation.");
            return body;
        }

        try {
            Request request = MaasRequestMapper.INSTANCE.toMaasEmbeddingRequest((EmbeddingRequest) body);
            log.info("Got converted request: {}.", request);
            return request;
        } catch (Exception e) {
            log.error("Convert to maas embedding request failed.", e);
            return body;
        }
    }

    /**
     * MAAS embedding 模型请求参数
     *
     */
    @Data
    public static final class Request {
        private String model;

        private List<String> input;
    }
}
