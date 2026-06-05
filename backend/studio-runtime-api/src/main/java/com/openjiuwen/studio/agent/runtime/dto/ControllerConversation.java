/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * ControllerConversation
 */

@Validated

public class ControllerConversation implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("project_id")
    private String projectId = null;

    @JsonProperty("agent_id")
    private String agentId = null;

    @JsonProperty("conversation_id")
    private String conversationId = null;

    @JsonProperty("inputs")
    @Valid
    private Object inputs = null;

    @JsonProperty("outputs")
    @Valid
    private Object outputs = null;

    @JsonProperty("status")
    private String status = null;

    @JsonProperty("start_time")
    private Long startTime = null;

    @JsonProperty("end_time")
    private Long endTime = null;

    public String getProjectId() {
        return projectId;
    }

    public ControllerConversation setProjectId(String projectId) {
        this.projectId = projectId;
        return this;
    }

    public String getAgentId() {
        return agentId;
    }

    public ControllerConversation setAgentId(String agentId) {
        this.agentId = agentId;
        return this;
    }

    public String getConversationId() {
        return conversationId;
    }

    public ControllerConversation setConversationId(String conversationId) {
        this.conversationId = conversationId;
        return this;
    }

    public Object getInputs() {
        return inputs;
    }

    public ControllerConversation setInputs(Object inputs) {
        this.inputs = inputs;
        return this;
    }

    public Object getOutputs() {
        return outputs;
    }

    public ControllerConversation setOutputs(Object outputs) {
        this.outputs = outputs;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public ControllerConversation setStatus(String status) {
        this.status = status;
        return this;
    }

    public Long getStartTime() {
        return startTime;
    }

    public ControllerConversation setStartTime(Long startTime) {
        this.startTime = startTime;
        return this;
    }

    public Long getEndTime() {
        return endTime;
    }

    public ControllerConversation setEndTime(Long endTime) {
        this.endTime = endTime;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ControllerConversation {\n");

        sb.append("    projectId: ").append(toIndentedString(projectId)).append("\n");
        sb.append("    agentId: ").append(toIndentedString(agentId)).append("\n");
        sb.append("    conversationId: ").append(toIndentedString(conversationId)).append("\n");
        sb.append("    inputs: ").append(toIndentedString(inputs)).append("\n");
        sb.append("    outputs: ").append(toIndentedString(outputs)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    startTime: ").append(toIndentedString(startTime)).append("\n");
        sb.append("    endTime: ").append(toIndentedString(endTime)).append("\n");
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
        ControllerConversation controllerConversation = (ControllerConversation) o;
        return Objects.equals(this.projectId, controllerConversation.projectId) && Objects.equals(this.agentId,
            controllerConversation.agentId) && Objects.equals(this.conversationId,
            controllerConversation.conversationId) && Objects.equals(this.inputs, controllerConversation.inputs)
            && Objects.equals(this.outputs, controllerConversation.outputs) && Objects.equals(this.status,
            controllerConversation.status) && Objects.equals(this.startTime, controllerConversation.startTime)
            && Objects.equals(this.endTime, controllerConversation.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectId, agentId, conversationId, inputs, outputs, status, startTime, endTime);
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
