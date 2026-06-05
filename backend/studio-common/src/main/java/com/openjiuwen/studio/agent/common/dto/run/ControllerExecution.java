/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.run;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

import org.springframework.validation.annotation.Validated;

import java.util.Objects;

/**
 * Controller会话的对话信息
 */
@ApiModel(description = "Controller会话的对话信息")
@Validated
public class ControllerExecution {
    @JsonProperty("execution_id")
    private String executionId = null;

    @JsonProperty("query")
    private String query = null;

    @JsonProperty("status")
    private String status = null;

    @JsonProperty("error_info")
    private String errorInfo = null;

    @JsonProperty("start_time")
    private Long startTime = null;

    public String getExecutionId() {
        return executionId;
    }

    public ControllerExecution setExecutionId(String executionId) {
        this.executionId = executionId;
        return this;
    }

    public String getQuery() {
        return query;
    }

    public ControllerExecution setQuery(String query) {
        this.query = query;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public ControllerExecution setStatus(String status) {
        this.status = status;
        return this;
    }

    public String getErrorInfo() {
        return errorInfo;
    }

    public ControllerExecution setErrorInfo(String errorInfo) {
        this.errorInfo = errorInfo;
        return this;
    }

    public Long getStartTime() {
        return startTime;
    }

    public ControllerExecution setStartTime(Long startTime) {
        this.startTime = startTime;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ControllerExecution {\n");

        sb.append("    executionId: ").append(toIndentedString(executionId)).append("\n");
        sb.append("    query: ").append(toIndentedString(query)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    errorInfo: ").append(toIndentedString(errorInfo)).append("\n");
        sb.append("    startTime: ").append(toIndentedString(startTime)).append("\n");
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
        ControllerExecution controllerExecution = (ControllerExecution) o;
        return Objects.equals(this.executionId, controllerExecution.executionId) && Objects.equals(this.query,
            controllerExecution.query) && Objects.equals(this.status, controllerExecution.status) && Objects.equals(
            this.errorInfo, controllerExecution.errorInfo) && Objects.equals(this.startTime,
            controllerExecution.startTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(executionId, query, status, errorInfo, startTime);
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
