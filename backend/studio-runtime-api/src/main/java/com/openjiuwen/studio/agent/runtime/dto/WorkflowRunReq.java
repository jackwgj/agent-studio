/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.openjiuwen.studio.agent.common.dto.WorkflowEnvironment;
import com.openjiuwen.studio.agent.common.dto.run.PluginConfig;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * WorkflowRunReq
 */

@Validated

public class WorkflowRunReq implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("inputs")
    @Valid
    @Size()
    private Map<String, Object> inputs = null;

    @JsonProperty("memory_inputs")
    @Valid
    @Size()
    private Map<String, Object> memoryInputs = null;

    @JsonProperty("globals")
    @Valid
    @Size()
    private Map<String, Object> globals = null;

    @JsonProperty("environment")
    @Valid
    private WorkflowEnvironment environment = null;

    @JsonProperty("messages")
    @Valid
    @Size()
    private List<com.openjiuwen.studio.agent.common.dto.agent.Message> messages = null;

    @JsonProperty("long_term_memory")
    @Valid
    private LongTermMemoryRuntime longTermMemory = null;

    @JsonProperty("plugin_configs")
    @Valid
    @Size()
    private List<PluginConfig> pluginConfigs = null;

    @JsonProperty("version")
    private Long version = null;

    @JsonProperty("userId")
    private String userId = null;

    @JsonProperty("conversation")
    @Valid
    private Conversation conversation = null;

    @JsonProperty("enable_history")
    private Boolean enableHistory = true;

    public Map<String, Object> getInputs() {
        return inputs;
    }

    public WorkflowRunReq setInputs(Map<String, Object> inputs) {
        this.inputs = inputs;
        return this;
    }

    public Map<String, Object> getMemoryInputs() {
        return memoryInputs;
    }

    public WorkflowRunReq setMemoryInputs(Map<String, Object> memoryInputs) {
        this.memoryInputs = memoryInputs;
        return this;
    }

    public Map<String, Object> getGlobals() {
        return globals;
    }

    public WorkflowRunReq setGlobals(Map<String, Object> globals) {
        this.globals = globals;
        return this;
    }

    public WorkflowEnvironment getEnvironment() {
        return environment;
    }

    public WorkflowRunReq setEnvironment(WorkflowEnvironment environment) {
        this.environment = environment;
        return this;
    }

    public List<com.openjiuwen.studio.agent.common.dto.agent.Message> getMessages() {
        return messages;
    }

    public WorkflowRunReq setMessages(List<com.openjiuwen.studio.agent.common.dto.agent.Message> messages) {
        this.messages = messages;
        return this;
    }

    public LongTermMemoryRuntime getLongTermMemory() {
        return longTermMemory;
    }

    public WorkflowRunReq setLongTermMemory(LongTermMemoryRuntime longTermMemory) {
        this.longTermMemory = longTermMemory;
        return this;
    }

    public List<PluginConfig> getPluginConfigs() {
        return pluginConfigs;
    }

    public WorkflowRunReq setPluginConfigs(List<PluginConfig> pluginConfigs) {
        this.pluginConfigs = pluginConfigs;
        return this;
    }

    public Long getVersion() {
        return version;
    }

    public WorkflowRunReq setVersion(Long version) {
        this.version = version;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public WorkflowRunReq setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public Conversation getConversation() {
        return conversation;
    }

    public WorkflowRunReq setConversation(Conversation conversation) {
        this.conversation = conversation;
        return this;
    }

    public WorkflowRunReq setEnableHistory(Boolean enableHistory) {
        this.enableHistory = enableHistory;
        return this;
    }

    public Boolean isEnableHistory() {
        return enableHistory;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class WorkflowRunReq {\n");

        sb.append("    inputs: ").append(toIndentedString(inputs)).append("\n");
        sb.append("    memoryInputs: ").append(toIndentedString(memoryInputs)).append("\n");
        sb.append("    globals: ").append(toIndentedString(globals)).append("\n");
        sb.append("    environment: ").append(toIndentedString(environment)).append("\n");
        sb.append("    messages: ").append(toIndentedString(messages)).append("\n");
        sb.append("    longTermMemory: ").append(toIndentedString(longTermMemory)).append("\n");
        sb.append("    pluginConfigs: ").append(toIndentedString(pluginConfigs)).append("\n");
        sb.append("    version: ").append(toIndentedString(version)).append("\n");
        sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
        sb.append("    conversation: ").append(toIndentedString(conversation)).append("\n");
        sb.append("    enableHistory: ").append(toIndentedString(enableHistory)).append("\n");
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
        WorkflowRunReq workflowRunReq = (WorkflowRunReq) o;
        return Objects.equals(this.inputs, workflowRunReq.inputs) && Objects.equals(this.memoryInputs,
            workflowRunReq.memoryInputs) && Objects.equals(this.globals, workflowRunReq.globals) && Objects.equals(
            this.environment, workflowRunReq.environment) && Objects.equals(this.messages, workflowRunReq.messages)
            && Objects.equals(this.longTermMemory, workflowRunReq.longTermMemory) && Objects.equals(this.pluginConfigs,
            workflowRunReq.pluginConfigs) && Objects.equals(this.version, workflowRunReq.version) && Objects.equals(
            this.userId, workflowRunReq.userId) && Objects.equals(this.conversation, workflowRunReq.conversation)
            && Objects.equals(this.enableHistory, workflowRunReq.enableHistory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inputs, memoryInputs, globals, environment, messages, longTermMemory, pluginConfigs,
            version, userId, conversation, enableHistory);
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
