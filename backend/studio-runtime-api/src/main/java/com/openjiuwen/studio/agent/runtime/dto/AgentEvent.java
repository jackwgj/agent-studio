/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * AgentEvent
 */

@Validated

public class AgentEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("event")
    @NotBlank
    private String event = null;

    @JsonProperty("content")
    @Valid
    private Object content = null;

    @JsonProperty("reasoning_content")
    private String reasoningContent = null;

    @JsonProperty("index")
    private Integer index = null;

    @JsonProperty("role")
    private String role = null;

    @JsonProperty("tokens")
    private Long tokens = null;

    @JsonProperty("type")
    private TypeEnum type = null;

    @JsonProperty("status")
    private StatusEnum status = null;

    @JsonProperty("latency")
    @Valid
    private Latency latency = null;

    @JsonProperty("plugin")
    @Valid
    private Plugin plugin = null;

    @JsonProperty("createdTime")
    private Long createdTime = null;

    @JsonProperty("executionId")
    private String executionId = null;

    public String getEvent() {
        return event;
    }

    public AgentEvent setEvent(String event) {
        this.event = event;
        return this;
    }

    public Object getContent() {
        return content;
    }

    public AgentEvent setContent(Object content) {
        this.content = content;
        return this;
    }

    public String getReasoningContent() {
        return reasoningContent;
    }

    public AgentEvent setReasoningContent(String reasoningContent) {
        this.reasoningContent = reasoningContent;
        return this;
    }

    public Integer getIndex() {
        return index;
    }

    public AgentEvent setIndex(Integer index) {
        this.index = index;
        return this;
    }

    public String getRole() {
        return role;
    }

    public AgentEvent setRole(String role) {
        this.role = role;
        return this;
    }

    public Long getTokens() {
        return tokens;
    }

    public AgentEvent setTokens(Long tokens) {
        this.tokens = tokens;
        return this;
    }

    public TypeEnum getType() {
        return type;
    }

    public AgentEvent setType(TypeEnum type) {
        this.type = type;
        return this;
    }

    public StatusEnum getStatus() {
        return status;
    }

    public AgentEvent setStatus(StatusEnum status) {
        this.status = status;
        return this;
    }

    public Latency getLatency() {
        return latency;
    }

    public AgentEvent setLatency(Latency latency) {
        this.latency = latency;
        return this;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public AgentEvent setPlugin(Plugin plugin) {
        this.plugin = plugin;
        return this;
    }

    public Long getCreatedTime() {
        return createdTime;
    }

    public AgentEvent setCreatedTime(Long createdTime) {
        this.createdTime = createdTime;
        return this;
    }

    public String getExecutionId() {
        return executionId;
    }

    public AgentEvent setExecutionId(String executionId) {
        this.executionId = executionId;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AgentEvent {\n");

        sb.append("    event: ").append(toIndentedString(event)).append("\n");
        sb.append("    content: ").append(toIndentedString(content)).append("\n");
        sb.append("    reasoningContent: ").append(toIndentedString(reasoningContent)).append("\n");
        sb.append("    index: ").append(toIndentedString(index)).append("\n");
        sb.append("    role: ").append(toIndentedString(role)).append("\n");
        sb.append("    tokens: ").append(toIndentedString(tokens)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    latency: ").append(toIndentedString(latency)).append("\n");
        sb.append("    plugin: ").append(toIndentedString(plugin)).append("\n");
        sb.append("    createdTime: ").append(toIndentedString(createdTime)).append("\n");
        sb.append("    executionId: ").append(toIndentedString(executionId)).append("\n");
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
        AgentEvent agentEvent = (AgentEvent) o;
        return Objects.equals(this.event, agentEvent.event) && Objects.equals(this.content, agentEvent.content)
            && Objects.equals(this.reasoningContent, agentEvent.reasoningContent) && Objects.equals(this.index,
            agentEvent.index) && Objects.equals(this.role, agentEvent.role) && Objects.equals(this.tokens,
            agentEvent.tokens) && Objects.equals(this.type, agentEvent.type) && Objects.equals(this.status,
            agentEvent.status) && Objects.equals(this.latency, agentEvent.latency) && Objects.equals(this.plugin,
            agentEvent.plugin) && Objects.equals(this.createdTime, agentEvent.createdTime) && Objects.equals(
            this.executionId, agentEvent.executionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(event, content, reasoningContent, index, role, tokens, type, status, latency, plugin,
            createdTime, executionId);
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

    /**
     * 类型
     */
    public enum TypeEnum {
        PLUGIN("plugin"),

        WORKFLOW("workflow"),

        MCP("mcp");

        private String value;

        TypeEnum(String value) {
            this.value = value;
        }

        @JsonCreator
        public static TypeEnum fromValue(String text) {
            for (TypeEnum b : TypeEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }
    }

    /**
     * 状态
     */
    public enum StatusEnum {
        ERROR("error"),

        RUNNING("running"),

        END("end");

        private String value;

        StatusEnum(String value) {
            this.value = value;
        }

        @JsonCreator
        public static StatusEnum fromValue(String text) {
            for (StatusEnum b : StatusEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }
    }
}
