/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.agent.runtime.dto.MessageEvent;

import lombok.Getter;
import lombok.Setter;

/**
 * 敏感词消息块结构
 *
 */
@Setter
@Getter
public class SensitiveMessageEvent extends MessageEvent {
    @JsonProperty("offset")
    private int offset;
}
