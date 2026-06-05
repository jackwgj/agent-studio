/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * ListKnowledgeBasesQo: converted from multi query params
 */
@ApiModel(description = "ListKnowledgeBasesQo: converted from multi query params")

@Validated

public class ListKnowledgeBasesQo implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("workspace_id")
    @Pattern(regexp = "^[a-zA-Z0-9_()\\-]+$")
    @NotBlank
    @Length(min = 1, max = 64)
    private String workspaceId = null;

    @JsonProperty("offset")
    @Range(min = 0L, max = 10000L)
    private Integer offset = 0;

    @JsonProperty("share_scope")
    private String shareScope = null;

    @JsonProperty("limit")
    @Range(min = 1L, max = 1000L)
    private Integer limit = 10;

    @JsonProperty("name")
    @Pattern(regexp = "^.{0,64}$")
    @Length(max = 192)
    private String name = null;

    @JsonProperty("type")
    private String type = null;

    @JsonProperty("knowledge_base_connection_id")
    @Length(max = 100)
    private String knowledgeBaseConnectionId = null;

    @JsonProperty("status")
    private String status = null;

    @JsonProperty("knowledge_base_ids")
    @Valid
    @Size(min = 1, max = 100)
    private List<@Length(max = 128) String> knowledgeBaseIds = null;

    @JsonProperty("share_type")
    private String shareType = null;

    public String getWorkspaceId() {
        return workspaceId;
    }

    public ListKnowledgeBasesQo setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
        return this;
    }

    public Integer getOffset() {
        return offset;
    }

    public ListKnowledgeBasesQo setOffset(Integer offset) {
        this.offset = offset;
        return this;
    }

    public String getShareScope() {
        return shareScope;
    }

    public ListKnowledgeBasesQo setShareScope(String shareScope) {
        this.shareScope = shareScope;
        return this;
    }

    public Integer getLimit() {
        return limit;
    }

    public ListKnowledgeBasesQo setLimit(Integer limit) {
        this.limit = limit;
        return this;
    }

    public String getName() {
        return name;
    }

    public ListKnowledgeBasesQo setName(String name) {
        this.name = name;
        return this;
    }

    public String getType() {
        return type;
    }

    public ListKnowledgeBasesQo setType(String type) {
        this.type = type;
        return this;
    }

    public String getKnowledgeBaseConnectionId() {
        return knowledgeBaseConnectionId;
    }

    public ListKnowledgeBasesQo setKnowledgeBaseConnectionId(String knowledgeBaseConnectionId) {
        this.knowledgeBaseConnectionId = knowledgeBaseConnectionId;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public ListKnowledgeBasesQo setStatus(String status) {
        this.status = status;
        return this;
    }

    public List<String> getKnowledgeBaseIds() {
        return knowledgeBaseIds;
    }

    public ListKnowledgeBasesQo setKnowledgeBaseIds(List<String> knowledgeBaseIds) {
        this.knowledgeBaseIds = knowledgeBaseIds;
        return this;
    }

    public String getShareType() {
        return shareType;
    }

    public ListKnowledgeBasesQo setShareType(String shareType) {
        this.shareType = shareType;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ListKnowledgeBasesQo {\n");

        sb.append("    workspaceId: ").append(toIndentedString(workspaceId)).append("\n");
        sb.append("    offset: ").append(toIndentedString(offset)).append("\n");
        sb.append("    shareScope: ").append(toIndentedString(shareScope)).append("\n");
        sb.append("    limit: ").append(toIndentedString(limit)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    knowledgeBaseConnectionId: ").append(toIndentedString(knowledgeBaseConnectionId)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    knowledgeBaseIds: ").append(toIndentedString(knowledgeBaseIds)).append("\n");
        sb.append("    shareType: ").append(toIndentedString(shareType)).append("\n");
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
        ListKnowledgeBasesQo listKnowledgeBasesQo = (ListKnowledgeBasesQo) o;
        return Objects.equals(this.workspaceId, listKnowledgeBasesQo.workspaceId) && Objects.equals(this.offset,
            listKnowledgeBasesQo.offset) && Objects.equals(this.shareScope, listKnowledgeBasesQo.shareScope)
            && Objects.equals(this.limit, listKnowledgeBasesQo.limit) && Objects.equals(this.name,
            listKnowledgeBasesQo.name) && Objects.equals(this.type, listKnowledgeBasesQo.type) && Objects.equals(
            this.knowledgeBaseConnectionId, listKnowledgeBasesQo.knowledgeBaseConnectionId) && Objects.equals(
            this.status, listKnowledgeBasesQo.status) && Objects.equals(this.knowledgeBaseIds,
            listKnowledgeBasesQo.knowledgeBaseIds) && Objects.equals(this.shareType, listKnowledgeBasesQo.shareType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workspaceId, offset, shareScope, limit, name, type, knowledgeBaseConnectionId, status,
            knowledgeBaseIds, shareType);
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
