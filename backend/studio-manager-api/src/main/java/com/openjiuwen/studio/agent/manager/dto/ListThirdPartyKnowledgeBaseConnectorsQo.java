/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.constraints.NotBlank;

import org.hibernate.validator.constraints.Range;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * ListThirdPartyKnowledgeBaseConnectorsQo: converted from multi query params
 */
@ApiModel(description = "ListThirdPartyKnowledgeBaseConnectorsQo: converted from multi query params")

@Validated

public class ListThirdPartyKnowledgeBaseConnectorsQo implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("workspace_id")
    @NotBlank
    private String workspaceId = null;

    @JsonProperty("offset")
    @Range(min = 0L, max = 10000L)
    private Integer offset = 0;

    @JsonProperty("limit")
    @Range(min = 1L, max = 1000L)
    private Integer limit = 10;

    public String getWorkspaceId() {
        return workspaceId;
    }

    public ListThirdPartyKnowledgeBaseConnectorsQo setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
        return this;
    }

    public Integer getOffset() {
        return offset;
    }

    public ListThirdPartyKnowledgeBaseConnectorsQo setOffset(Integer offset) {
        this.offset = offset;
        return this;
    }

    public Integer getLimit() {
        return limit;
    }

    public ListThirdPartyKnowledgeBaseConnectorsQo setLimit(Integer limit) {
        this.limit = limit;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ListThirdPartyKnowledgeBaseConnectorsQo {\n");

        sb.append("    workspaceId: ").append(toIndentedString(workspaceId)).append("\n");
        sb.append("    offset: ").append(toIndentedString(offset)).append("\n");
        sb.append("    limit: ").append(toIndentedString(limit)).append("\n");
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
        ListThirdPartyKnowledgeBaseConnectorsQo listThirdPartyKnowledgeBaseConnectorsQo
            = (ListThirdPartyKnowledgeBaseConnectorsQo) o;
        return Objects.equals(this.workspaceId, listThirdPartyKnowledgeBaseConnectorsQo.workspaceId) && Objects.equals(
            this.offset, listThirdPartyKnowledgeBaseConnectorsQo.offset) && Objects.equals(this.limit,
            listThirdPartyKnowledgeBaseConnectorsQo.limit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workspaceId, offset, limit);
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
