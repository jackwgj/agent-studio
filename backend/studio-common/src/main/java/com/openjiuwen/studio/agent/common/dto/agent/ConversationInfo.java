/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.agent;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;

import org.springframework.validation.annotation.Validated;

import java.util.Objects;

/**
 * ConversationInfo
 */
@Validated
public class ConversationInfo {
    @JsonProperty("project_id")
    private String projectId = null;

    @JsonProperty("workflow_id")
    private String workflowId = null;

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

    @JsonProperty("success_count")
    private Integer successCount = null;

    @JsonProperty("failure_count")
    private Integer failureCount = null;

    public String getProjectId() {
        return projectId;
    }

    public ConversationInfo setProjectId(String projectId) {
        this.projectId = projectId;
        return this;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public ConversationInfo setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
        return this;
    }

    public String getConversationId() {
        return conversationId;
    }

    public ConversationInfo setConversationId(String conversationId) {
        this.conversationId = conversationId;
        return this;
    }

    public Object getInputs() {
        return inputs;
    }

    public ConversationInfo setInputs(Object inputs) {
        this.inputs = inputs;
        return this;
    }

    public Object getOutputs() {
        return outputs;
    }

    public ConversationInfo setOutputs(Object outputs) {
        this.outputs = outputs;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public ConversationInfo setStatus(String status) {
        this.status = status;
        return this;
    }

    public Long getStartTime() {
        return startTime;
    }

    public ConversationInfo setStartTime(Long startTime) {
        this.startTime = startTime;
        return this;
    }

    public Long getEndTime() {
        return endTime;
    }

    public ConversationInfo setEndTime(Long endTime) {
        this.endTime = endTime;
        return this;
    }

    public Integer getSuccessCount() {
        return successCount;
    }

    public ConversationInfo setSuccessCount(Integer successCount) {
        this.successCount = successCount;
        return this;
    }

    public Integer getFailureCount() {
        return failureCount;
    }

    public ConversationInfo setFailureCount(Integer failureCount) {
        this.failureCount = failureCount;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ConversationInfo {\n");

        sb.append("    projectId: ").append(toIndentedString(projectId)).append("\n");
        sb.append("    workflowId: ").append(toIndentedString(workflowId)).append("\n");
        sb.append("    conversationId: ").append(toIndentedString(conversationId)).append("\n");
        sb.append("    inputs: ").append(toIndentedString(inputs)).append("\n");
        sb.append("    outputs: ").append(toIndentedString(outputs)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    startTime: ").append(toIndentedString(startTime)).append("\n");
        sb.append("    endTime: ").append(toIndentedString(endTime)).append("\n");
        sb.append("    successCount: ").append(toIndentedString(successCount)).append("\n");
        sb.append("    failureCount: ").append(toIndentedString(failureCount)).append("\n");
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
        ConversationInfo conversationInfo = (ConversationInfo) o;
        return Objects.equals(this.projectId, conversationInfo.projectId) && Objects.equals(this.workflowId,
            conversationInfo.workflowId) && Objects.equals(this.conversationId, conversationInfo.conversationId)
            && Objects.equals(this.inputs, conversationInfo.inputs) && Objects.equals(this.outputs,
            conversationInfo.outputs) && Objects.equals(this.status, conversationInfo.status) && Objects.equals(
            this.startTime, conversationInfo.startTime) && Objects.equals(this.endTime, conversationInfo.endTime)
            && Objects.equals(this.successCount, conversationInfo.successCount) && Objects.equals(this.failureCount,
            conversationInfo.failureCount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectId, workflowId, conversationId, inputs, outputs, status, startTime, endTime,
            successCount, failureCount);
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