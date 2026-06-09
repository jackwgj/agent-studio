/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * AgentVersionListRsp搜索返回item。
 */
@ApiModel(description = "AgentVersionListRsp搜索返回item。")

@Validated

public class AgentVersionListItem implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private String id = null;

    @JsonProperty("name")
    private String name = null;

    @JsonProperty("icon")
    private String icon = null;

    @JsonProperty("description")
    private String description = null;

    @JsonProperty("creator")
    private String creator = null;

    @JsonProperty("version_id")
    private String versionId = null;

    @JsonProperty("version_name")
    private String versionName = null;

    @JsonProperty("reference_workflows")
    private String referenceWorkflows = null;

    @JsonProperty("reference_agents")
    private String referenceAgents = null;

    public String getId() {
        return id;
    }

    public AgentVersionListItem setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public AgentVersionListItem setName(String name) {
        this.name = name;
        return this;
    }

    public String getIcon() {
        return icon;
    }

    public AgentVersionListItem setIcon(String icon) {
        this.icon = icon;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public AgentVersionListItem setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getCreator() {
        return creator;
    }

    public AgentVersionListItem setCreator(String creator) {
        this.creator = creator;
        return this;
    }

    public String getVersionId() {
        return versionId;
    }

    public AgentVersionListItem setVersionId(String versionId) {
        this.versionId = versionId;
        return this;
    }

    public String getVersionName() {
        return versionName;
    }

    public AgentVersionListItem setVersionName(String versionName) {
        this.versionName = versionName;
        return this;
    }

    public String getReferenceWorkflows() {
        return referenceWorkflows;
    }

    public AgentVersionListItem setReferenceWorkflows(String referenceWorkflows) {
        this.referenceWorkflows = referenceWorkflows;
        return this;
    }

    public String getReferenceAgents() {
        return referenceAgents;
    }

    public AgentVersionListItem setReferenceAgents(String referenceAgents) {
        this.referenceAgents = referenceAgents;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AgentVersionListItem {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    icon: ").append(toIndentedString(icon)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    creator: ").append(toIndentedString(creator)).append("\n");
        sb.append("    versionId: ").append(toIndentedString(versionId)).append("\n");
        sb.append("    versionName: ").append(toIndentedString(versionName)).append("\n");
        sb.append("    referenceWorkflows: ").append(toIndentedString(referenceWorkflows)).append("\n");
        sb.append("    referenceAgents: ").append(toIndentedString(referenceAgents)).append("\n");
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
        AgentVersionListItem agentVersionListItem = (AgentVersionListItem) o;
        return Objects.equals(this.id, agentVersionListItem.id) && Objects.equals(this.name, agentVersionListItem.name)
            && Objects.equals(this.icon, agentVersionListItem.icon) && Objects.equals(this.description,
            agentVersionListItem.description) && Objects.equals(this.creator, agentVersionListItem.creator)
            && Objects.equals(this.versionId, agentVersionListItem.versionId) && Objects.equals(this.versionName,
            agentVersionListItem.versionName) && Objects.equals(this.referenceWorkflows,
            agentVersionListItem.referenceWorkflows) && Objects.equals(this.referenceAgents,
            agentVersionListItem.referenceAgents);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, icon, description, creator, versionId, versionName, referenceWorkflows,
            referenceAgents);
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
