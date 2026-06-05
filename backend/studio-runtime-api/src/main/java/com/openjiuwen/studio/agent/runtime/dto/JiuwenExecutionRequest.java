/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * JiuwenExecutionRequest
 */

@Validated

public class JiuwenExecutionRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("conversationId")
    @Length(max = 64)
    private String conversationId = null;

    @JsonProperty("query")
    private String query = null;

    @JsonProperty("responseMode")
    @Length(max = 20)
    private String responseMode = null;

    @JsonProperty("params")
    @Valid
    private JiuwenParams params = null;

    @JsonProperty("irPath")
    @Length(max = 256)
    private String irPath = null;

    @JsonProperty("userId")
    private String userId = null;

    @JsonProperty("agentId")
    private String agentId = null;

    @JsonProperty("dialogueCount")
    private Long dialogueCount = null;

    public String getConversationId() {
        return conversationId;
    }

    public JiuwenExecutionRequest setConversationId(String conversationId) {
        this.conversationId = conversationId;
        return this;
    }

    public String getQuery() {
        return query;
    }

    public JiuwenExecutionRequest setQuery(String query) {
        this.query = query;
        return this;
    }

    public String getResponseMode() {
        return responseMode;
    }

    public JiuwenExecutionRequest setResponseMode(String responseMode) {
        this.responseMode = responseMode;
        return this;
    }

    public JiuwenParams getParams() {
        return params;
    }

    public JiuwenExecutionRequest setParams(JiuwenParams params) {
        this.params = params;
        return this;
    }

    public String getIrPath() {
        return irPath;
    }

    public JiuwenExecutionRequest setIrPath(String irPath) {
        this.irPath = irPath;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public JiuwenExecutionRequest setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getAgentId() {
        return agentId;
    }

    public JiuwenExecutionRequest setAgentId(String agentId) {
        this.agentId = agentId;
        return this;
    }

    public Long getDialogueCount() {
        return dialogueCount;
    }

    public JiuwenExecutionRequest setDialogueCount(Long dialogueCount) {
        this.dialogueCount = dialogueCount;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class JiuwenExecutionRequest {\n");

        sb.append("    conversationId: ").append(toIndentedString(conversationId)).append("\n");
        sb.append("    query: ").append(toIndentedString(query)).append("\n");
        sb.append("    responseMode: ").append(toIndentedString(responseMode)).append("\n");
        sb.append("    params: ").append(toIndentedString(params)).append("\n");
        sb.append("    irPath: ").append(toIndentedString(irPath)).append("\n");
        sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
        sb.append("    agentId: ").append(toIndentedString(agentId)).append("\n");
        sb.append("    dialogueCount: ").append(toIndentedString(dialogueCount)).append("\n");
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
        JiuwenExecutionRequest jiuwenExecutionRequest = (JiuwenExecutionRequest) o;
        return Objects.equals(this.conversationId, jiuwenExecutionRequest.conversationId) && Objects.equals(this.query,
            jiuwenExecutionRequest.query) && Objects.equals(this.responseMode, jiuwenExecutionRequest.responseMode)
            && Objects.equals(this.params, jiuwenExecutionRequest.params) && Objects.equals(this.irPath,
            jiuwenExecutionRequest.irPath) && Objects.equals(this.userId, jiuwenExecutionRequest.userId)
            && Objects.equals(this.agentId, jiuwenExecutionRequest.agentId) && Objects.equals(this.dialogueCount,
            jiuwenExecutionRequest.dialogueCount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(conversationId, query, responseMode, params, irPath, userId, agentId, dialogueCount);
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
