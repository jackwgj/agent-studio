/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.models.assistant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 消息内容，当前只支持文本类型的消息
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageContent {
    /**
     * 消息类型，当前只支持text
     */
    private String type;

    private String content;

    private String metadata;
}
