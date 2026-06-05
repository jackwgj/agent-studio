/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.constraints.NotBlank;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * 工作流节点连接信息
 */
@ApiModel(description = "工作流节点连接信息")

@Validated

public class WorkflowConnection implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("source")
    @NotBlank
    @Length(max = 64)
    private String source = null;

    @JsonProperty("target")
    @NotBlank
    @Length(max = 64)
    private String target = null;

    @JsonProperty("conditionId")
    @Length(max = 64)
    private String conditionId = null;

    public String getSource() {
        return source;
    }

    public WorkflowConnection setSource(String source) {
        this.source = source;
        return this;
    }

    public String getTarget() {
        return target;
    }

    public WorkflowConnection setTarget(String target) {
        this.target = target;
        return this;
    }

    public String getConditionId() {
        return conditionId;
    }

    public WorkflowConnection setConditionId(String conditionId) {
        this.conditionId = conditionId;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class WorkflowConnection {\n");

        sb.append("    source: ").append(toIndentedString(source)).append("\n");
        sb.append("    target: ").append(toIndentedString(target)).append("\n");
        sb.append("    conditionId: ").append(toIndentedString(conditionId)).append("\n");
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
        WorkflowConnection workflowConnection = (WorkflowConnection) o;
        return Objects.equals(this.source, workflowConnection.source) && Objects.equals(this.target,
            workflowConnection.target) && Objects.equals(this.conditionId, workflowConnection.conditionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, target, conditionId);
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
