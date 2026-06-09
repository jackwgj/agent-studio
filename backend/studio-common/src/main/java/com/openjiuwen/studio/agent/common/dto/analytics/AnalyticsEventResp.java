/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.analytics;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * 分析事件记录响应
 */
@ApiModel(description = "分析事件记录响应")

@Validated

public class AnalyticsEventResp implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("error_message")
    private String errorMessage = null;

    @JsonProperty("error_code")
    private String errorCode = null;

    @JsonProperty("event_id")
    private String eventId = null;

    public String getErrorMessage() {
        return errorMessage;
    }

    public AnalyticsEventResp setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public AnalyticsEventResp setErrorCode(String errorCode) {
        this.errorCode = errorCode;
        return this;
    }

    public String getEventId() {
        return eventId;
    }

    public AnalyticsEventResp setEventId(String eventId) {
        this.eventId = eventId;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AnalyticsEventResp {\n");

        sb.append("    errorMessage: ").append(toIndentedString(errorMessage)).append("\n");
        sb.append("    errorCode: ").append(toIndentedString(errorCode)).append("\n");
        sb.append("    eventId: ").append(toIndentedString(eventId)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AnalyticsEventResp analyticsEventResp = (AnalyticsEventResp) o;
        return Objects.equals(this.errorMessage, analyticsEventResp.errorMessage) && Objects.equals(this.errorCode,
            analyticsEventResp.errorCode) && Objects.equals(this.eventId, analyticsEventResp.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(errorMessage, errorCode, eventId);
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
