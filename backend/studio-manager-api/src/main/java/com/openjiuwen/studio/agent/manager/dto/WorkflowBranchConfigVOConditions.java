/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * WorkflowBranchConfigVOConditions
 */

@Validated

public class WorkflowBranchConfigVOConditions implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("operator")
    private String operator = null;

    @JsonProperty("left")
    @Valid
    private WorkflowFieldVO left = null;

    @JsonProperty("right")
    @Valid
    private WorkflowFieldVO right = null;

    public String getOperator() {
        return operator;
    }

    public WorkflowBranchConfigVOConditions setOperator(String operator) {
        this.operator = operator;
        return this;
    }

    public WorkflowFieldVO getLeft() {
        return left;
    }

    public WorkflowBranchConfigVOConditions setLeft(WorkflowFieldVO left) {
        this.left = left;
        return this;
    }

    public WorkflowFieldVO getRight() {
        return right;
    }

    public WorkflowBranchConfigVOConditions setRight(WorkflowFieldVO right) {
        this.right = right;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class WorkflowBranchConfigVOConditions {\n");

        sb.append("    operator: ").append(toIndentedString(operator)).append("\n");
        sb.append("    left: ").append(toIndentedString(left)).append("\n");
        sb.append("    right: ").append(toIndentedString(right)).append("\n");
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
        WorkflowBranchConfigVOConditions workflowBranchConfigVOConditions = (WorkflowBranchConfigVOConditions) o;
        return Objects.equals(this.operator, workflowBranchConfigVOConditions.operator) && Objects.equals(this.left,
            workflowBranchConfigVOConditions.left) && Objects.equals(this.right,
            workflowBranchConfigVOConditions.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operator, left, right);
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
