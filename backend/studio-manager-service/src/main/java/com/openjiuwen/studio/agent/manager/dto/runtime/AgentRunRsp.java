/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto.runtime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.agent.common.dto.AgentInvokeInfo;
import com.openjiuwen.studio.agent.manager.dto.Message;

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
    @JsonProperty("conversation_id")
    private String conversationId;

    @Builder.Default
    private Map<String, Object> outputs = new HashMap<>();

    private List<Message> messages;

    @JsonProperty("error_code")
    private String errorCode;

    @JsonProperty("error_message")
    private String errorMessage;

    @JsonProperty("start_time")
    private Long startTime;

    @JsonProperty("end_time")
    private Long endTime;

    private List<AgentInvokeInfo> events;
}
