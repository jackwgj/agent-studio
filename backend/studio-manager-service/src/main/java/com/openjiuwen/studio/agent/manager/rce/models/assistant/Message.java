/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.models.assistant;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 消息，包括用户消息和助手消息，当前只支持文本类型的消息
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    @JsonProperty("message_id")
    private String messageId;

    @JsonProperty("assistant_session_id")
    private String sessionId;

    @JsonProperty("index_num")
    private Integer indexNum;

    private String role;

    private List<MessageContent> contents;

    private String metadata;

    @JsonProperty("created_on")
    private String createdOn;

    @JsonProperty("updated_on")
    private String updatedOn;
}
