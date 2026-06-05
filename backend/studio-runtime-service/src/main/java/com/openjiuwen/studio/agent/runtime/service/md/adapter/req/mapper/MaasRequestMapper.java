/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.service.md.adapter.req.mapper;

import com.openjiuwen.studio.agent.runtime.dto.md.EmbeddingRequest;
import com.openjiuwen.studio.agent.runtime.dto.md.RankDocumentsRequest;
import com.openjiuwen.studio.agent.runtime.service.md.adapter.req.MaasEmbeddingRequestAdaptor;
import com.openjiuwen.studio.agent.runtime.service.md.adapter.req.MaasRerankRequestAdaptor;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;

/**
 * MAAS rerank/embedding模型请求参数转换mapper
 *
 */
@Mapper
public interface MaasRequestMapper {
    MaasRequestMapper INSTANCE = Mappers.getMapper(MaasRequestMapper.class);

    /**
     * 实现RankDocumentsRequest转换为MAAS的rerank模型请求参数
     *
     * @param rankDocumentsRequest rankDocumentsRequest
     * @return MaasRerankRequestAdaptor.RerankRequest
     */
    @Mapping(target = "model", source = "model")
    @Mapping(target = "query", source = "query")
    @Mapping(target = "documents", source = "docs")
    MaasRerankRequestAdaptor.RerankRequest toMaasRerankRequest(RankDocumentsRequest rankDocumentsRequest);

    /**
     * 实现EmbeddingRequest转换为MAAS的embedding模型请求参数
     *
     * @param embeddingRequest embeddingRequest
     * @return MaasEmbeddingRequestAdaptor.EmbeddingRequest
     */
    @Mapping(target = "model", source = "model")
    @Mapping(target = "input", source = "input")
    MaasEmbeddingRequestAdaptor.Request toMaasEmbeddingRequest(EmbeddingRequest embeddingRequest);

    /**
     * 处理EmbeddingRequest的input参数的转换
     *
     * @param value value
     * @return List<String>
     */
    @SuppressWarnings("unchecked")
    default List<String> toEmbeddingInput(Object value) {
        if (value instanceof List) {
            List<String> list = (List<String>) value;
            return List.copyOf(list);
        } else if (value instanceof String) {
            return Collections.singletonList((String) value);
        } else {
            return Collections.emptyList();
        }
    }
}
