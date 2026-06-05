/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * JiuwenComponentExecutionRequest
 */

@Validated

public class JiuwenComponentExecutionRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("conversationId")
    @Length(max = 64)
    private String conversationId = null;

    @JsonProperty("inputs")
    @Valid
    @Size()
    private Map<String, Object> inputs = null;

    @JsonProperty("componentId")
    @Length(max = 64)
    private String componentId = null;

    @JsonProperty("params")
    @Valid
    private JiuwenParams params = null;

    @JsonProperty("irPath")
    @Length(max = 256)
    private String irPath = null;

    @JsonProperty("userId")
    private String userId = null;

    public String getConversationId() {
        return conversationId;
    }

    public JiuwenComponentExecutionRequest setConversationId(String conversationId) {
        this.conversationId = conversationId;
        return this;
    }

    public Map<String, Object> getInputs() {
        return inputs;
    }

    public JiuwenComponentExecutionRequest setInputs(Map<String, Object> inputs) {
        this.inputs = inputs;
        return this;
    }

    public String getComponentId() {
        return componentId;
    }

    public JiuwenComponentExecutionRequest setComponentId(String componentId) {
        this.componentId = componentId;
        return this;
    }

    public JiuwenParams getParams() {
        return params;
    }

    public JiuwenComponentExecutionRequest setParams(JiuwenParams params) {
        this.params = params;
        return this;
    }

    public String getIrPath() {
        return irPath;
    }

    public JiuwenComponentExecutionRequest setIrPath(String irPath) {
        this.irPath = irPath;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public JiuwenComponentExecutionRequest setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class JiuwenComponentExecutionRequest {\n");

        sb.append("    conversationId: ").append(toIndentedString(conversationId)).append("\n");
        sb.append("    inputs: ").append(toIndentedString(inputs)).append("\n");
        sb.append("    componentId: ").append(toIndentedString(componentId)).append("\n");
        sb.append("    params: ").append(toIndentedString(params)).append("\n");
        sb.append("    irPath: ").append(toIndentedString(irPath)).append("\n");
        sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
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
        JiuwenComponentExecutionRequest jiuwenComponentExecutionRequest = (JiuwenComponentExecutionRequest) o;
        return Objects.equals(this.conversationId, jiuwenComponentExecutionRequest.conversationId) && Objects.equals(
            this.inputs, jiuwenComponentExecutionRequest.inputs) && Objects.equals(this.componentId,
            jiuwenComponentExecutionRequest.componentId) && Objects.equals(this.params,
            jiuwenComponentExecutionRequest.params) && Objects.equals(this.irPath,
            jiuwenComponentExecutionRequest.irPath) && Objects.equals(this.userId,
            jiuwenComponentExecutionRequest.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(conversationId, inputs, componentId, params, irPath, userId);
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
