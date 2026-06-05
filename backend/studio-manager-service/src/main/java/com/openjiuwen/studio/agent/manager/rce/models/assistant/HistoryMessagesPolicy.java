/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.models.assistant;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * 历史消息处理策略
 *
 */
@Data
@Getter
@Setter
public class HistoryMessagesPolicy {
    private String type;

    private Boolean enabled;

    @JsonProperty("truncation_window")
    private Integer truncationWindow;
}
