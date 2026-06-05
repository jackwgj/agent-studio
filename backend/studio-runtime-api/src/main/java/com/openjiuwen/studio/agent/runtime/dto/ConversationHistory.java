/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * ConversationHistory
 */

@Validated

public class ConversationHistory implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("role")
    private String role = null;

    @JsonProperty("content")
    private String content = null;

    @JsonProperty("enable_history")
    private Boolean enableHistory = null;

    @JsonProperty("intent")
    @Valid
    @Size()
    private List<@Length() String> intent = null;

    @JsonProperty("function_call")
    @Valid
    private Object functionCall = null;

    @JsonProperty("name")
    @Valid
    private Object name = null;

    @JsonProperty("tool_calls")
    @Valid
    private Object toolCalls = null;

    @JsonProperty("tool_call_id")
    @Valid
    private Object toolCallId = null;

    @JsonProperty("files")
    @Valid
    @Size()
    private List<Object> files = null;

    @JsonProperty("agent_id")
    private String agentId = null;

    public String getRole() {
        return role;
    }

    public ConversationHistory setRole(String role) {
        this.role = role;
        return this;
    }

    public String getContent() {
        return content;
    }

    public ConversationHistory setContent(String content) {
        this.content = content;
        return this;
    }

    public ConversationHistory setEnableHistory(Boolean enableHistory) {
        this.enableHistory = enableHistory;
        return this;
    }

    public Boolean isEnableHistory() {
        return enableHistory;
    }

    public List<String> getIntent() {
        return intent;
    }

    public ConversationHistory setIntent(List<String> intent) {
        this.intent = intent;
        return this;
    }

    public Object getFunctionCall() {
        return functionCall;
    }

    public ConversationHistory setFunctionCall(Object functionCall) {
        this.functionCall = functionCall;
        return this;
    }

    public Object getName() {
        return name;
    }

    public ConversationHistory setName(Object name) {
        this.name = name;
        return this;
    }

    public Object getToolCalls() {
        return toolCalls;
    }

    public ConversationHistory setToolCalls(Object toolCalls) {
        this.toolCalls = toolCalls;
        return this;
    }

    public Object getToolCallId() {
        return toolCallId;
    }

    public ConversationHistory setToolCallId(Object toolCallId) {
        this.toolCallId = toolCallId;
        return this;
    }

    public List<Object> getFiles() {
        return files;
    }

    public ConversationHistory setFiles(List<Object> files) {
        this.files = files;
        return this;
    }

    public String getAgentId() {
        return agentId;
    }

    public ConversationHistory setAgentId(String agentId) {
        this.agentId = agentId;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ConversationHistory {\n");

        sb.append("    role: ").append(toIndentedString(role)).append("\n");
        sb.append("    content: ").append(toIndentedString(content)).append("\n");
        sb.append("    enableHistory: ").append(toIndentedString(enableHistory)).append("\n");
        sb.append("    intent: ").append(toIndentedString(intent)).append("\n");
        sb.append("    functionCall: ").append(toIndentedString(functionCall)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    toolCalls: ").append(toIndentedString(toolCalls)).append("\n");
        sb.append("    toolCallId: ").append(toIndentedString(toolCallId)).append("\n");
        sb.append("    files: ").append(toIndentedString(files)).append("\n");
        sb.append("    agentId: ").append(toIndentedString(agentId)).append("\n");
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
        ConversationHistory conversationHistory = (ConversationHistory) o;
        return Objects.equals(this.role, conversationHistory.role) && Objects.equals(this.content,
            conversationHistory.content) && Objects.equals(this.enableHistory, conversationHistory.enableHistory)
            && Objects.equals(this.intent, conversationHistory.intent) && Objects.equals(this.functionCall,
            conversationHistory.functionCall) && Objects.equals(this.name, conversationHistory.name) && Objects.equals(
            this.toolCalls, conversationHistory.toolCalls) && Objects.equals(this.toolCallId,
            conversationHistory.toolCallId) && Objects.equals(this.files, conversationHistory.files) && Objects.equals(
            this.agentId, conversationHistory.agentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(role, content, enableHistory, intent, functionCall, name, toolCalls, toolCallId, files,
            agentId);
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
