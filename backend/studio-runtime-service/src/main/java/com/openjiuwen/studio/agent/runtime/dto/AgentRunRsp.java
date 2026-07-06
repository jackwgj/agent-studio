/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.openjiuwen.studio.agent.common.dto.AgentInvokeInfo;
import lombok.Builder;
import lombok.Data;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 单智能体执行响应体
 *
 */
@Validated
@Data
@Builder
public class AgentRunRsp implements Serializable {

    // 静态代码检查G.SER.02：实现Serializable的类应显式声明serialVersionUID，避免类变更后反序列化兼容性问题
    private static final long serialVersionUID = 1L;

    @JsonProperty("conversation_id")
    private String conversationId;

    @Builder.Default
    private Map<String, Object> outputs = new HashMap<>();

    private List<com.openjiuwen.studio.agent.common.dto.agent.Message> messages;

    @JsonProperty("error_code")
    private String errorCode;

    @JsonProperty("error_message")
    private String errorMessage;

    @JsonProperty("start_time")
    private Long startTime;

    @JsonProperty("end_time")
    private Long endTime;

    private List<AgentInvokeInfo> events;

    @JsonProperty("knowledge_base_files_info")
    private List<KnowledgeBaseFileInfo> filesInfo;
}
