/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.analytics;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.constraints.NotBlank;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * 分析事件信息
 */
@ApiModel(description = "分析事件信息")

@Validated

public class AnalyticsEventReq implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("event_type")
    @NotBlank
    @Length(max = 32)
    private String eventType = null;

    @JsonProperty("app_type")
    @NotBlank
    @Length(max = 32)
    private String appType = null;

    @JsonProperty("channel")
    @Length(max = 128)
    private String channel = null;

    @JsonProperty("event_id")
    @Length(max = 128)
    private String eventId = null;

    public String getEventType() {
        return eventType;
    }

    public AnalyticsEventReq setEventType(String eventType) {
        this.eventType = eventType;
        return this;
    }

    public String getAppType() {
        return appType;
    }

    public AnalyticsEventReq setAppType(String appType) {
        this.appType = appType;
        return this;
    }

    public String getChannel() {
        return channel;
    }

    public AnalyticsEventReq setChannel(String channel) {
        this.channel = channel;
        return this;
    }

    public String getEventId() {
        return eventId;
    }

    public AnalyticsEventReq setEventId(String eventId) {
        this.eventId = eventId;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AnalyticsEventReq {\n");

        sb.append("    eventType: ").append(toIndentedString(eventType)).append("\n");
        sb.append("    appType: ").append(toIndentedString(appType)).append("\n");
        sb.append("    channel: ").append(toIndentedString(channel)).append("\n");
        sb.append("    eventId: ").append(toIndentedString(eventId)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AnalyticsEventReq analyticsEventReq = (AnalyticsEventReq) o;
        return Objects.equals(this.eventType, analyticsEventReq.eventType) && Objects.equals(this.appType,
            analyticsEventReq.appType) && Objects.equals(this.channel, analyticsEventReq.channel) && Objects.equals(
            this.eventId, analyticsEventReq.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventType, appType, channel, eventId);
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
