/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.model.obs;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 知识库文件（OBS）的数据结构。
 * <p>
 * 写入 kb-connection/ir/knowledge-base/{knowledgeBaseId}.json，仅含知识库自身信息
 * 与 connectionId 引用。字段名通过 @JsonProperty 锁定为驼峰，与历史 Map 序列化结果一致。
 * </p>
 *
 * @since 2026-06-28
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KbKnowledgeBaseObsData {

    @JsonProperty("knowledgeBaseId")
    private String knowledgeBaseId;

    @JsonProperty("knowledgeBaseName")
    private String knowledgeBaseName;

    @JsonProperty("type")
    private String type;

    @JsonProperty("externalId")
    private String externalId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("connectionId")
    private String connectionId;
}
