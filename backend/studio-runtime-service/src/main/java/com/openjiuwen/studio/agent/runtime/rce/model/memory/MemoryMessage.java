/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.rce.model.memory;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 消息模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemoryMessage {

    /**
     * 消息ID
     */
    private String id;

    /**
     * 消息角色
     */
    private MemoryExtractMessageRole role;

    /**
     * 时间戳
     */
    private Long timestamp;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 会话ID
     */
    @JsonProperty("conversation_id")
    private String conversationId;
}
