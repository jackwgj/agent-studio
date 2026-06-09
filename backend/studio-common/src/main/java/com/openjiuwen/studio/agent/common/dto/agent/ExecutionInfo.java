/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.agent;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * ExecutionInfo
 */
@Validated

public class ExecutionInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("project_id")
    private String projectId = null;

    @JsonProperty("workflow_id")
    private String workflowId = null;

    @JsonProperty("conversation_id")
    private String conversationId = null;

    @JsonProperty("execution_id")
    private String executionId = null;

    @JsonProperty("inputs")
    @Valid
    private Object inputs = null;

    @JsonProperty("outputs")
    @Valid
    private Object outputs = null;

    @JsonProperty("status")
    private String status = null;

    @JsonProperty("run_info")
    private String runInfo = null;

    @JsonProperty("start_time")
    private Long startTime = null;

    @JsonProperty("end_time")
    private Long endTime = null;

    @JsonProperty("error_info")
    private String errorInfo = null;

    @JsonProperty("event_list")
    @Valid
    @Size()
    private List<NodeRunInfo> eventList = null;

    public String getProjectId() {
        return projectId;
    }

    public ExecutionInfo setProjectId(String projectId) {
        this.projectId = projectId;
        return this;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public ExecutionInfo setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
        return this;
    }

    public String getConversationId() {
        return conversationId;
    }

    public ExecutionInfo setConversationId(String conversationId) {
        this.conversationId = conversationId;
        return this;
    }

    public String getExecutionId() {
        return executionId;
    }

    public ExecutionInfo setExecutionId(String executionId) {
        this.executionId = executionId;
        return this;
    }

    public Object getInputs() {
        return inputs;
    }

    public ExecutionInfo setInputs(Object inputs) {
        this.inputs = inputs;
        return this;
    }

    public Object getOutputs() {
        return outputs;
    }

    public ExecutionInfo setOutputs(Object outputs) {
        this.outputs = outputs;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public ExecutionInfo setStatus(String status) {
        this.status = status;
        return this;
    }

    public String getRunInfo() {
        return runInfo;
    }

    public ExecutionInfo setRunInfo(String runInfo) {
        this.runInfo = runInfo;
        return this;
    }

    public Long getStartTime() {
        return startTime;
    }

    public ExecutionInfo setStartTime(Long startTime) {
        this.startTime = startTime;
        return this;
    }

    public Long getEndTime() {
        return endTime;
    }

    public ExecutionInfo setEndTime(Long endTime) {
        this.endTime = endTime;
        return this;
    }

    public String getErrorInfo() {
        return errorInfo;
    }

    public ExecutionInfo setErrorInfo(String errorInfo) {
        this.errorInfo = errorInfo;
        return this;
    }

    public List<NodeRunInfo> getEventList() {
        return eventList;
    }

    public ExecutionInfo setEventList(List<NodeRunInfo> eventList) {
        this.eventList = eventList;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ExecutionInfo {\n");

        sb.append("    projectId: ").append(toIndentedString(projectId)).append("\n");
        sb.append("    workflowId: ").append(toIndentedString(workflowId)).append("\n");
        sb.append("    conversationId: ").append(toIndentedString(conversationId)).append("\n");
        sb.append("    executionId: ").append(toIndentedString(executionId)).append("\n");
        sb.append("    inputs: ").append(toIndentedString(inputs)).append("\n");
        sb.append("    outputs: ").append(toIndentedString(outputs)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    runInfo: ").append(toIndentedString(runInfo)).append("\n");
        sb.append("    startTime: ").append(toIndentedString(startTime)).append("\n");
        sb.append("    endTime: ").append(toIndentedString(endTime)).append("\n");
        sb.append("    errorInfo: ").append(toIndentedString(errorInfo)).append("\n");
        sb.append("    eventList: ").append(toIndentedString(eventList)).append("\n");
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
        ExecutionInfo executionInfo = (ExecutionInfo) o;
        return Objects.equals(this.projectId, executionInfo.projectId) && Objects.equals(this.workflowId,
            executionInfo.workflowId) && Objects.equals(this.conversationId, executionInfo.conversationId)
            && Objects.equals(this.executionId, executionInfo.executionId) && Objects.equals(this.inputs,
            executionInfo.inputs) && Objects.equals(this.outputs, executionInfo.outputs) && Objects.equals(this.status,
            executionInfo.status) && Objects.equals(this.runInfo, executionInfo.runInfo) && Objects.equals(
            this.startTime, executionInfo.startTime) && Objects.equals(this.endTime, executionInfo.endTime)
            && Objects.equals(this.errorInfo, executionInfo.errorInfo) && Objects.equals(this.eventList,
            executionInfo.eventList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectId, workflowId, conversationId, executionId, inputs, outputs, status, runInfo,
            startTime, endTime, errorInfo, eventList);
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
