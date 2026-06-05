/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.agent.common.dto.agent.Feedback;

import lombok.Data;

/**
 * 功能描述 Feedback元数据
 *
 */
@Data
public class FeedbackEntity {
    @JsonProperty("app_id")
    String appId;

    @JsonProperty("app_type")
    String appType;

    @JsonProperty("app_name")
    String appName;

    @JsonProperty("domain_id")
    String domainId;

    @JsonProperty("project_id")
    String projectId;

    @JsonProperty("user_id")
    String userId;

    @JsonProperty("version_id")
    String versionId;

    @JsonProperty("conversation_id")
    String conversationId;

    @JsonProperty("message_id")
    String messageId;

    Feedback feedback;

    /**
     * 反馈元数据记录时间戳
     */
    private Long createAt;
}
