/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * 知识型Agent绑定workflow执行时，调用workflow的内容
 */
@ApiModel(description = "知识型Agent绑定workflow执行时，调用workflow的内容")

@Validated

public class AgentWorkflowContent implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("answer")
    private String answer = null;

    @JsonProperty("workflow_state")
    private String workflowState = null;

    @JsonProperty("errMessage")
    @Valid
    private Object errMessage = null;

    public String getAnswer() {
        return answer;
    }

    public AgentWorkflowContent setAnswer(String answer) {
        this.answer = answer;
        return this;
    }

    public String getWorkflowState() {
        return workflowState;
    }

    public AgentWorkflowContent setWorkflowState(String workflowState) {
        this.workflowState = workflowState;
        return this;
    }

    public Object getErrMessage() {
        return errMessage;
    }

    public AgentWorkflowContent setErrMessage(Object errMessage) {
        this.errMessage = errMessage;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AgentWorkflowContent {\n");

        sb.append("    answer: ").append(toIndentedString(answer)).append("\n");
        sb.append("    workflowState: ").append(toIndentedString(workflowState)).append("\n");
        sb.append("    errMessage: ").append(toIndentedString(errMessage)).append("\n");
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
        AgentWorkflowContent agentWorkflowContent = (AgentWorkflowContent) o;
        return Objects.equals(this.answer, agentWorkflowContent.answer) && Objects.equals(this.workflowState,
            agentWorkflowContent.workflowState) && Objects.equals(this.errMessage, agentWorkflowContent.errMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(answer, workflowState, errMessage);
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
