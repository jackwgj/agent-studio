/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * rerank模型请求体
 *
 */
@Data
@Builder
public class AskRerankReq {
    private String query;

    @JsonProperty("ranking_order")
    private List<String> rankingOrder;

    private List<Document> docs;

    /**
     * rerank请求测试文档
     *
     */
    @Data
    public static class Document {
        String id;

        String content;
    }
}
