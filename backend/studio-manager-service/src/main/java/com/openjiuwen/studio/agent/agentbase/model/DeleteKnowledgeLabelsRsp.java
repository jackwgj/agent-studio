/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 知识库标签列表查询响应体
 *
 * @since 2025-05-29
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeleteKnowledgeLabelsRsp {
    @JsonProperty("label_id")
    private String labelId;
}
