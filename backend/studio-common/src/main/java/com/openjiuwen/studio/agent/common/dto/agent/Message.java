/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.agent;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Objects;

/**
 * Message
 */
@Validated
public class Message {
    @JsonProperty("role")
    @Length(min = 1, max = 64)
    private String role = null;

    @JsonProperty("content")
    private String content = null;

    @JsonProperty("create_time")
    private Long createTime = null;

    @JsonProperty("name")
    @Valid
    private Object name = null;

    @JsonProperty("function_call")
    @Valid
    private Object functionCall = null;

    @JsonProperty("tool_calls")
    @Valid
    private Object toolCalls = null;

    @JsonProperty("tool_call_id")
    @Valid
    private Object toolCallId = null;

    @JsonProperty("enable_history")
    private Boolean enableHistory = null;

    @JsonProperty("intent")
    @Valid
    @Size()
    private List<@Length() String> intent = null;

    @JsonProperty("execution_id")
    private String executionId = null;

    @JsonProperty("node_id")
    private String nodeId = null;

    @JsonProperty("agent_id")
    private String agentId = null;

    @JsonProperty("rating")
    @Range(min = -1L, max = 1L)
    private Integer rating = null;

    @JsonProperty("files")
    @Valid
    @Size()
    private List<Object> files = null;

    @JsonProperty("reason")
    @Valid
    private FeedbackReason reason = null;

    public String getRole() {
        return role;
    }

    public Message setRole(String role) {
        this.role = role;
        return this;
    }

    public String getContent() {
        return content;
    }

    public Message setContent(String content) {
        this.content = content;
        return this;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public Message setCreateTime(Long createTime) {
        this.createTime = createTime;
        return this;
    }

    public Object getName() {
        return name;
    }

    public Message setName(Object name) {
        this.name = name;
        return this;
    }

    public Object getFunctionCall() {
        return functionCall;
    }

    public Message setFunctionCall(Object functionCall) {
        this.functionCall = functionCall;
        return this;
    }

    public Object getToolCalls() {
        return toolCalls;
    }

    public Message setToolCalls(Object toolCalls) {
        this.toolCalls = toolCalls;
        return this;
    }

    public Object getToolCallId() {
        return toolCallId;
    }

    public Message setToolCallId(Object toolCallId) {
        this.toolCallId = toolCallId;
        return this;
    }

    public Message setEnableHistory(Boolean enableHistory) {
        this.enableHistory = enableHistory;
        return this;
    }

    public Boolean isEnableHistory() {
        return enableHistory;
    }

    public List<String> getIntent() {
        return intent;
    }

    public Message setIntent(List<String> intent) {
        this.intent = intent;
        return this;
    }

    public String getExecutionId() {
        return executionId;
    }

    public Message setExecutionId(String executionId) {
        this.executionId = executionId;
        return this;
    }

    public String getNodeId() {
        return nodeId;
    }

    public Message setNodeId(String nodeId) {
        this.nodeId = nodeId;
        return this;
    }

    public String getAgentId() {
        return agentId;
    }

    public Message setAgentId(String agentId) {
        this.agentId = agentId;
        return this;
    }

    public Integer getRating() {
        return rating;
    }

    public Message setRating(Integer rating) {
        this.rating = rating;
        return this;
    }

    public List<Object> getFiles() {
        return files;
    }

    public Message setFiles(List<Object> files) {
        this.files = files;
        return this;
    }

    public FeedbackReason getReason() {
        return reason;
    }

    public Message setReason(FeedbackReason reason) {
        this.reason = reason;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Message {\n");

        sb.append("    role: ").append(toIndentedString(role)).append("\n");
        sb.append("    content: ").append(toIndentedString(content)).append("\n");
        sb.append("    createTime: ").append(toIndentedString(createTime)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    functionCall: ").append(toIndentedString(functionCall)).append("\n");
        sb.append("    toolCalls: ").append(toIndentedString(toolCalls)).append("\n");
        sb.append("    toolCallId: ").append(toIndentedString(toolCallId)).append("\n");
        sb.append("    enableHistory: ").append(toIndentedString(enableHistory)).append("\n");
        sb.append("    intent: ").append(toIndentedString(intent)).append("\n");
        sb.append("    executionId: ").append(toIndentedString(executionId)).append("\n");
        sb.append("    nodeId: ").append(toIndentedString(nodeId)).append("\n");
        sb.append("    agentId: ").append(toIndentedString(agentId)).append("\n");
        sb.append("    rating: ").append(toIndentedString(rating)).append("\n");
        sb.append("    files: ").append(toIndentedString(files)).append("\n");
        sb.append("    reason: ").append(toIndentedString(reason)).append("\n");
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
        Message message = (Message) o;
        return Objects.equals(this.role, message.role) && Objects.equals(this.content, message.content)
            && Objects.equals(this.createTime, message.createTime) && Objects.equals(this.name, message.name)
            && Objects.equals(this.functionCall, message.functionCall) && Objects.equals(this.toolCalls,
            message.toolCalls) && Objects.equals(this.toolCallId, message.toolCallId) && Objects.equals(
            this.enableHistory, message.enableHistory) && Objects.equals(this.intent, message.intent) && Objects.equals(
            this.executionId, message.executionId) && Objects.equals(this.nodeId, message.nodeId) && Objects.equals(
            this.agentId, message.agentId) && Objects.equals(this.rating, message.rating) && Objects.equals(this.files,
            message.files) && Objects.equals(this.reason, message.reason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(role, content, createTime, name, functionCall, toolCalls, toolCallId, enableHistory, intent,
            executionId, nodeId, agentId, rating, files, reason);
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
