/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * F1AgentEvent
 */

@Validated

public class F1AgentEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("event")
    @NotBlank
    private String event = null;

    @JsonProperty("content")
    @Valid
    private Object content = null;

    @JsonProperty("conversationId")
    private String conversationId = null;

    @JsonProperty("executionId")
    private String executionId = null;

    @JsonProperty("appType")
    private String appType = null;

    @JsonProperty("appId")
    private String appId = null;

    @JsonProperty("appName")
    private String appName = null;

    @JsonProperty("appIcon")
    private String appIcon = null;

    @JsonProperty("appDesc")
    private String appDesc = null;

    @JsonProperty("latency")
    private Long latency = null;

    @JsonProperty("createdTime")
    private Long createdTime = null;

    public String getEvent() {
        return event;
    }

    public F1AgentEvent setEvent(String event) {
        this.event = event;
        return this;
    }

    public Object getContent() {
        return content;
    }

    public F1AgentEvent setContent(Object content) {
        this.content = content;
        return this;
    }

    public String getConversationId() {
        return conversationId;
    }

    public F1AgentEvent setConversationId(String conversationId) {
        this.conversationId = conversationId;
        return this;
    }

    public String getExecutionId() {
        return executionId;
    }

    public F1AgentEvent setExecutionId(String executionId) {
        this.executionId = executionId;
        return this;
    }

    public String getAppType() {
        return appType;
    }

    public F1AgentEvent setAppType(String appType) {
        this.appType = appType;
        return this;
    }

    public String getAppId() {
        return appId;
    }

    public F1AgentEvent setAppId(String appId) {
        this.appId = appId;
        return this;
    }

    public String getAppName() {
        return appName;
    }

    public F1AgentEvent setAppName(String appName) {
        this.appName = appName;
        return this;
    }

    public String getAppIcon() {
        return appIcon;
    }

    public F1AgentEvent setAppIcon(String appIcon) {
        this.appIcon = appIcon;
        return this;
    }

    public String getAppDesc() {
        return appDesc;
    }

    public F1AgentEvent setAppDesc(String appDesc) {
        this.appDesc = appDesc;
        return this;
    }

    public Long getLatency() {
        return latency;
    }

    public F1AgentEvent setLatency(Long latency) {
        this.latency = latency;
        return this;
    }

    public Long getCreatedTime() {
        return createdTime;
    }

    public F1AgentEvent setCreatedTime(Long createdTime) {
        this.createdTime = createdTime;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class F1AgentEvent {\n");

        sb.append("    event: ").append(toIndentedString(event)).append("\n");
        sb.append("    content: ").append(toIndentedString(content)).append("\n");
        sb.append("    conversationId: ").append(toIndentedString(conversationId)).append("\n");
        sb.append("    executionId: ").append(toIndentedString(executionId)).append("\n");
        sb.append("    appType: ").append(toIndentedString(appType)).append("\n");
        sb.append("    appId: ").append(toIndentedString(appId)).append("\n");
        sb.append("    appName: ").append(toIndentedString(appName)).append("\n");
        sb.append("    appIcon: ").append(toIndentedString(appIcon)).append("\n");
        sb.append("    appDesc: ").append(toIndentedString(appDesc)).append("\n");
        sb.append("    latency: ").append(toIndentedString(latency)).append("\n");
        sb.append("    createdTime: ").append(toIndentedString(createdTime)).append("\n");
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
        F1AgentEvent f1AgentEvent = (F1AgentEvent) o;
        return Objects.equals(this.event, f1AgentEvent.event) && Objects.equals(this.content, f1AgentEvent.content)
            && Objects.equals(this.conversationId, f1AgentEvent.conversationId) && Objects.equals(this.executionId,
            f1AgentEvent.executionId) && Objects.equals(this.appType, f1AgentEvent.appType) && Objects.equals(
            this.appId, f1AgentEvent.appId) && Objects.equals(this.appName, f1AgentEvent.appName) && Objects.equals(
            this.appIcon, f1AgentEvent.appIcon) && Objects.equals(this.appDesc, f1AgentEvent.appDesc) && Objects.equals(
            this.latency, f1AgentEvent.latency) && Objects.equals(this.createdTime, f1AgentEvent.createdTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(event, content, conversationId, executionId, appType, appId, appName, appIcon, appDesc,
            latency, createdTime);
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
