/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.constraints.NotBlank;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * QueryCustomPromptApiActionsQo: converted from multi query params
 */
@ApiModel(description = "QueryCustomPromptApiActionsQo: converted from multi query params")

@Validated
public class QueryCustomPromptApiActionsQo implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("workspace_id")
    @NotBlank
    private String workspaceId = null;

    @JsonProperty("name")
    private String name = null;

    public String getWorkspaceId() {
        return workspaceId;
    }

    public QueryCustomPromptApiActionsQo setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
        return this;
    }

    public String getName() {
        return name;
    }

    public QueryCustomPromptApiActionsQo setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class QueryCustomPromptApiActionsQo {\n");
        sb.append("    workspaceId: ").append(toIndentedString(workspaceId)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
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
        QueryCustomPromptApiActionsQo queryCustomPromptApiActionsQo = (QueryCustomPromptApiActionsQo) o;
        return Objects.equals(this.workspaceId, queryCustomPromptApiActionsQo.workspaceId) && Objects.equals(this.name,
            queryCustomPromptApiActionsQo.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workspaceId, name);
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
