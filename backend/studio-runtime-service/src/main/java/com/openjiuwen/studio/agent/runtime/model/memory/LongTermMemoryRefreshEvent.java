/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.model.memory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.agent.runtime.enums.EventType;

import lombok.Data;

import java.util.List;

/**
 * 用户画像刷新事件定义
 *
 */
@Data
public class LongTermMemoryRefreshEvent {
    @JsonProperty("event")
    private final String event = EventType.MEMORY_UPDATED.toString();

    @JsonProperty("data")
    private EventData data;

    /**
     * 时间的data结构
     *
     */
    @Data
    public static final class EventData {
        @JsonProperty("time")
        private Long time;

        @JsonProperty("memory_repo_id")
        private String memoryRepoId;

        @JsonProperty("conversation_id")
        private String conversationId;

        @JsonProperty("memories")
        private List<NaturalLanguageMemory> memories;
    }
}
