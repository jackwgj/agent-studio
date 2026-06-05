/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.rce.model;

import com.openjiuwen.studio.agent.common.dto.agent.FeedbackReason;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 功能描述 回流消息
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BackflowMessage {
    private String content;

    private String role;

    private Integer review;

    private FeedbackReason reason;

    private long timestamp;
}
