/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.rce.models.knowledge;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class KnowledgeBaseListItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("knowledge_base_id")
    private String knowledgeBaseId = null;

    public KnowledgeBaseListItem setKnowledgeBaseId(String knowledgeBaseId) {
        this.knowledgeBaseId = knowledgeBaseId;
        return this;
    }

    public String getKnowledgeBaseId() {
        return knowledgeBaseId;
    }

    @JsonProperty("type")
    private String type = null;

    public KnowledgeBaseListItem setType(String type) {
        this.type = type;
        return this;
    }

    public String getType() {
        return type;
    }

    @JsonProperty("created_domain_id")
    private String createdDomainId = null;

    public KnowledgeBaseListItem setCreatedDomainId(String createdDomainId) {
        this.createdDomainId = createdDomainId;
        return this;
    }

    public String getCreatedDomainId() {
        return createdDomainId;
    }

    @JsonProperty("name")
    private String name = null;

    public KnowledgeBaseListItem setName(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    @JsonProperty("description")
    private String description = null;

    public KnowledgeBaseListItem setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getDescription() {
        return description;
    }

    @JsonProperty("status")
    private String status = null;

    public KnowledgeBaseListItem setStatus(String status) {
        this.status = status;
        return this;
    }

    public String getStatus() {
        return status;
    }

    @JsonProperty("created_user_id")
    private String createdUserId = null;

    public KnowledgeBaseListItem setCreatedUserId(String createdUserId) {
        this.createdUserId = createdUserId;
        return this;
    }

    public String getCreatedUserId() {
        return createdUserId;
    }

    @JsonProperty("created_user_name")
    private String createdUserName = null;

    public KnowledgeBaseListItem setCreatedUserName(String createdUserName) {
        this.createdUserName = createdUserName;
        return this;
    }

    public String getCreatedUserName() {
        return createdUserName;
    }

    @JsonProperty("create_time")
    private Date createTime = null;

    public KnowledgeBaseListItem setCreateTime(Date createTime) {
        this.createTime = createTime;
        return this;
    }

    public Date getCreateTime() {
        return createTime;
    }

    @JsonProperty("last_update_user_id")
    private String lastUpdateUserId = null;

    public KnowledgeBaseListItem setLastUpdateUserId(String lastUpdateUserId) {
        this.lastUpdateUserId = lastUpdateUserId;
        return this;
    }

    public String getLastUpdateUserId() {
        return lastUpdateUserId;
    }

    @JsonProperty("last_update_user_name")
    private String lastUpdateUserName = null;

    public KnowledgeBaseListItem setLastUpdateUserName(String lastUpdateUserName) {
        this.lastUpdateUserName = lastUpdateUserName;
        return this;
    }

    public String getLastUpdateUserName() {
        return lastUpdateUserName;
    }

    @JsonProperty("update_time")
    private Date updateTime = null;

    public KnowledgeBaseListItem setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
        return this;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class KnowledgeBaseListItem {\n");
        sb.append("    knowledgeBaseId: ").append(toIndentedString(knowledgeBaseId)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    createdDomainId: ").append(toIndentedString(createdDomainId)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    createdUserId: ").append(toIndentedString(createdUserId)).append("\n");
        sb.append("    createdUserName: ").append(toIndentedString(createdUserName)).append("\n");
        sb.append("    createTime: ").append(toIndentedString(createTime)).append("\n");
        sb.append("    lastUpdateUserId: ").append(toIndentedString(lastUpdateUserId)).append("\n");
        sb.append("    lastUpdateUserName: ").append(toIndentedString(lastUpdateUserName)).append("\n");
        sb.append("    updateTime: ").append(toIndentedString(updateTime)).append("\n");
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
        KnowledgeBaseListItem knowledgeBaseListItem = (KnowledgeBaseListItem) o;
        return Objects.equals(this.knowledgeBaseId, knowledgeBaseListItem.knowledgeBaseId) && Objects.equals(this.type, knowledgeBaseListItem.type) && Objects.equals(this.createdDomainId, knowledgeBaseListItem.createdDomainId) && Objects.equals(this.name, knowledgeBaseListItem.name) && Objects.equals(this.description, knowledgeBaseListItem.description) && Objects.equals(this.status, knowledgeBaseListItem.status) && Objects.equals(this.createdUserId, knowledgeBaseListItem.createdUserId) && Objects.equals(this.createdUserName, knowledgeBaseListItem.createdUserName) && Objects.equals(this.createTime, knowledgeBaseListItem.createTime) && Objects.equals(this.lastUpdateUserId, knowledgeBaseListItem.lastUpdateUserId) && Objects.equals(this.lastUpdateUserName, knowledgeBaseListItem.lastUpdateUserName) && Objects.equals(this.updateTime, knowledgeBaseListItem.updateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(knowledgeBaseId, type, createdDomainId, name, description, status, createdUserId, createdUserName, createTime, lastUpdateUserId, lastUpdateUserName, updateTime);
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
