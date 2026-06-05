/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * 工作流基础信息
 */
@ApiModel(description = "工作流基础信息")

@Validated

public class WorkflowBaseData implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("workflow_id")
    @Length(max = 64)
    private String workflowId = null;

    @JsonProperty("workflow_name")
    @Length(max = 1000)
    private String workflowName = null;

    public String getWorkflowId() {
        return workflowId;
    }

    public WorkflowBaseData setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
        return this;
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public WorkflowBaseData setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class WorkflowBaseData {\n");

        sb.append("    workflowId: ").append(toIndentedString(workflowId)).append("\n");
        sb.append("    workflowName: ").append(toIndentedString(workflowName)).append("\n");
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
        WorkflowBaseData workflowBaseData = (WorkflowBaseData) o;
        return Objects.equals(this.workflowId, workflowBaseData.workflowId) && Objects.equals(this.workflowName,
            workflowBaseData.workflowName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workflowId, workflowName);
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
