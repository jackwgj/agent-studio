/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.run;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * workflow应用列表响应体
 */
@ApiModel(description = "workflow应用列表响应体")

@Validated

public class TaskListRsp implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("workflow_id")
    @Length(max = 64)
    private String workflowId = null;

    @JsonProperty("count")
    private Long count = null;

    @JsonProperty("data")
    @Valid
    @Size()
    private List<TaskListItem> data = null;

    public String getWorkflowId() {
        return workflowId;
    }

    public TaskListRsp setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
        return this;
    }

    public Long getCount() {
        return count;
    }

    public TaskListRsp setCount(Long count) {
        this.count = count;
        return this;
    }

    public List<TaskListItem> getData() {
        return data;
    }

    public TaskListRsp setData(List<TaskListItem> data) {
        this.data = data;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class TaskListRsp {\n");

        sb.append("    workflowId: ").append(toIndentedString(workflowId)).append("\n");
        sb.append("    count: ").append(toIndentedString(count)).append("\n");
        sb.append("    data: ").append(toIndentedString(data)).append("\n");
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
        TaskListRsp taskListRsp = (TaskListRsp) o;
        return Objects.equals(this.workflowId, taskListRsp.workflowId) && Objects.equals(this.count, taskListRsp.count)
            && Objects.equals(this.data, taskListRsp.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workflowId, count, data);
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
