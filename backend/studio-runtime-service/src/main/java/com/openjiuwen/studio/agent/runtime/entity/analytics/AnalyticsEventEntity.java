/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.entity.analytics;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * 分析事件
 *
 */
@Data
@Builder
public class AnalyticsEventEntity {
    @JsonProperty("event_id")
    private String eventId;

    @JsonProperty("event_type")
    private String eventType;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("project_id")
    private String projectId;

    @JsonProperty("event_time")
    private Date eventTime;

    @JsonProperty("event_date")
    private Date eventDate;

    @JsonProperty("app_type")
    private String appType;

    @JsonProperty("app_id")
    private String appId;

    @JsonProperty("channel")
    private String channel;

    @JsonProperty("new_app_user")
    private Boolean isNewAppUser;
}
