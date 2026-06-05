/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.run;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * 工作流列表。
 */
@ApiModel(description = "工作流列表。")

@Validated

public class WorkflowListItem implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("workflow_id")
    private String workflowId = null;

    @JsonProperty("name")
    private String name = null;

    @JsonProperty("avatar")
    private String avatar = null;

    @JsonProperty("code")
    private String code = null;

    @JsonProperty("description")
    private String description = null;

    @JsonProperty("creator")
    private String creator = null;

    @JsonProperty("type")
    private String type = null;

    @JsonProperty("group")
    private String group = null;

    @JsonProperty("url")
    private String url = null;

    @JsonProperty("status")
    private Integer status = null;

    @JsonProperty("customize_node")
    private Boolean customizeNode = null;

    @JsonProperty("updated_on")
    private Long updatedOn = null;

    @JsonProperty("last_version_id")
    private String lastVersionId = null;

    @JsonProperty("is_share")
    private Integer isShare = null;

    public String getWorkflowId() {
        return workflowId;
    }

    public WorkflowListItem setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
        return this;
    }

    public String getName() {
        return name;
    }

    public WorkflowListItem setName(String name) {
        this.name = name;
        return this;
    }

    public String getAvatar() {
        return avatar;
    }

    public WorkflowListItem setAvatar(String avatar) {
        this.avatar = avatar;
        return this;
    }

    public String getCode() {
        return code;
    }

    public WorkflowListItem setCode(String code) {
        this.code = code;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public WorkflowListItem setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getCreator() {
        return creator;
    }

    public WorkflowListItem setCreator(String creator) {
        this.creator = creator;
        return this;
    }

    public String getType() {
        return type;
    }

    public WorkflowListItem setType(String type) {
        this.type = type;
        return this;
    }

    public String getGroup() {
        return group;
    }

    public WorkflowListItem setGroup(String group) {
        this.group = group;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public WorkflowListItem setUrl(String url) {
        this.url = url;
        return this;
    }

    public Integer getStatus() {
        return status;
    }

    public WorkflowListItem setStatus(Integer status) {
        this.status = status;
        return this;
    }

    public WorkflowListItem setCustomizeNode(Boolean customizeNode) {
        this.customizeNode = customizeNode;
        return this;
    }

    public Boolean isCustomizeNode() {
        return customizeNode;
    }

    public Long getUpdatedOn() {
        return updatedOn;
    }

    public WorkflowListItem setUpdatedOn(Long updatedOn) {
        this.updatedOn = updatedOn;
        return this;
    }

    public String getLastVersionId() {
        return lastVersionId;
    }

    public WorkflowListItem setLastVersionId(String lastVersionId) {
        this.lastVersionId = lastVersionId;
        return this;
    }

    public Integer getIsShare() {
        return isShare;
    }

    public WorkflowListItem setIsShare(Integer isShare) {
        this.isShare = isShare;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class WorkflowListItem {\n");

        sb.append("    workflowId: ").append(toIndentedString(workflowId)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    avatar: ").append(toIndentedString(avatar)).append("\n");
        sb.append("    code: ").append(toIndentedString(code)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    creator: ").append(toIndentedString(creator)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    group: ").append(toIndentedString(group)).append("\n");
        sb.append("    url: ").append(toIndentedString(url)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    customizeNode: ").append(toIndentedString(customizeNode)).append("\n");
        sb.append("    updatedOn: ").append(toIndentedString(updatedOn)).append("\n");
        sb.append("    lastVersionId: ").append(toIndentedString(lastVersionId)).append("\n");
        sb.append("    isShare: ").append(toIndentedString(isShare)).append("\n");
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
        WorkflowListItem workflowListItem = (WorkflowListItem) o;
        return Objects.equals(this.workflowId, workflowListItem.workflowId) && Objects.equals(this.name,
            workflowListItem.name) && Objects.equals(this.avatar, workflowListItem.avatar) && Objects.equals(this.code,
            workflowListItem.code) && Objects.equals(this.description, workflowListItem.description) && Objects.equals(
            this.creator, workflowListItem.creator) && Objects.equals(this.type, workflowListItem.type)
            && Objects.equals(this.group, workflowListItem.group) && Objects.equals(this.url, workflowListItem.url)
            && Objects.equals(this.status, workflowListItem.status) && Objects.equals(this.customizeNode,
            workflowListItem.customizeNode) && Objects.equals(this.updatedOn, workflowListItem.updatedOn)
            && Objects.equals(this.lastVersionId, workflowListItem.lastVersionId) && Objects.equals(this.isShare,
            workflowListItem.isShare);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workflowId, name, avatar, code, description, creator, type, group, url, status,
            customizeNode, updatedOn, lastVersionId, isShare);
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
