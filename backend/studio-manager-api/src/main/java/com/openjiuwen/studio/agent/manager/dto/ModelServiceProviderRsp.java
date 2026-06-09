/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * ModelServiceProviderRsp
 */

@Validated

public class ModelServiceProviderRsp implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private String id = null;

    @JsonProperty("provider_name")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5a-zA-Z0-9_ |.:/\\\\-]{2,64}$")
    private String providerName = null;

    @JsonProperty("provider_name_en")
    @Pattern(regexp = "^[a-zA-Z0-9_| /:.\\\\-]{2,64}$")
    private String providerNameEn = null;

    @JsonProperty("description")
    private String description = null;

    @JsonProperty("tags")
    private String tags = null;

    @JsonProperty("provider_url")
    private String providerUrl = null;

    @JsonProperty("logo")
    private String logo = null;

    @JsonProperty("auth_configs")
    @Valid
    @Size()
    private List<ProviderAuthConfig> authConfigs = null;

    @JsonProperty("created_date")
    private Long createdDate = null;

    @JsonProperty("last_updated_date")
    private Long lastUpdatedDate = null;

    @JsonProperty("domain_id")
    private String domainId = null;

    @JsonProperty("project_id")
    @Pattern(regexp = "^[a-zA-Z0-9_-]{1,40}$")
    private String projectId = null;

    @JsonProperty("workspace_id")
    @Pattern(regexp = "^[a-zA-Z0-9_-]{1,40}$")
    private String workspaceId = null;

    @JsonProperty("auth_config_status")
    private String authConfigStatus = null;

    @JsonProperty("created_by_user")
    private String createdByUser = null;

    @JsonProperty("last_updated_by_user")
    private String lastUpdatedByUser = null;

    public String getId() {
        return id;
    }

    public ModelServiceProviderRsp setId(String id) {
        this.id = id;
        return this;
    }

    public String getProviderName() {
        return providerName;
    }

    public ModelServiceProviderRsp setProviderName(String providerName) {
        this.providerName = providerName;
        return this;
    }

    public String getProviderNameEn() {
        return providerNameEn;
    }

    public ModelServiceProviderRsp setProviderNameEn(String providerNameEn) {
        this.providerNameEn = providerNameEn;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ModelServiceProviderRsp setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getTags() {
        return tags;
    }

    public ModelServiceProviderRsp setTags(String tags) {
        this.tags = tags;
        return this;
    }

    public String getProviderUrl() {
        return providerUrl;
    }

    public ModelServiceProviderRsp setProviderUrl(String providerUrl) {
        this.providerUrl = providerUrl;
        return this;
    }

    public String getLogo() {
        return logo;
    }

    public ModelServiceProviderRsp setLogo(String logo) {
        this.logo = logo;
        return this;
    }

    public List<ProviderAuthConfig> getAuthConfigs() {
        return authConfigs;
    }

    public ModelServiceProviderRsp setAuthConfigs(List<ProviderAuthConfig> authConfigs) {
        this.authConfigs = authConfigs;
        return this;
    }

    public Long getCreatedDate() {
        return createdDate;
    }

    public ModelServiceProviderRsp setCreatedDate(Long createdDate) {
        this.createdDate = createdDate;
        return this;
    }

    public Long getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public ModelServiceProviderRsp setLastUpdatedDate(Long lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
        return this;
    }

    public String getDomainId() {
        return domainId;
    }

    public ModelServiceProviderRsp setDomainId(String domainId) {
        this.domainId = domainId;
        return this;
    }

    public String getProjectId() {
        return projectId;
    }

    public ModelServiceProviderRsp setProjectId(String projectId) {
        this.projectId = projectId;
        return this;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public ModelServiceProviderRsp setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
        return this;
    }

    public String getAuthConfigStatus() {
        return authConfigStatus;
    }

    public ModelServiceProviderRsp setAuthConfigStatus(String authConfigStatus) {
        this.authConfigStatus = authConfigStatus;
        return this;
    }

    public String getCreatedByUser() {
        return createdByUser;
    }

    public ModelServiceProviderRsp setCreatedByUser(String createdByUser) {
        this.createdByUser = createdByUser;
        return this;
    }

    public String getLastUpdatedByUser() {
        return lastUpdatedByUser;
    }

    public ModelServiceProviderRsp setLastUpdatedByUser(String lastUpdatedByUser) {
        this.lastUpdatedByUser = lastUpdatedByUser;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ModelServiceProviderRsp {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    providerName: ").append(toIndentedString(providerName)).append("\n");
        sb.append("    providerNameEn: ").append(toIndentedString(providerNameEn)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    tags: ").append(toIndentedString(tags)).append("\n");
        sb.append("    providerUrl: ").append(toIndentedString(providerUrl)).append("\n");
        sb.append("    logo: ").append(toIndentedString(logo)).append("\n");
        sb.append("    authConfigs: ").append(toIndentedString(authConfigs)).append("\n");
        sb.append("    createdDate: ").append(toIndentedString(createdDate)).append("\n");
        sb.append("    lastUpdatedDate: ").append(toIndentedString(lastUpdatedDate)).append("\n");
        sb.append("    domainId: ").append(toIndentedString(domainId)).append("\n");
        sb.append("    projectId: ").append(toIndentedString(projectId)).append("\n");
        sb.append("    workspaceId: ").append(toIndentedString(workspaceId)).append("\n");
        sb.append("    authConfigStatus: ").append(toIndentedString(authConfigStatus)).append("\n");
        sb.append("    createdByUser: ").append(toIndentedString(createdByUser)).append("\n");
        sb.append("    lastUpdatedByUser: ").append(toIndentedString(lastUpdatedByUser)).append("\n");
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
        ModelServiceProviderRsp modelServiceProviderRsp = (ModelServiceProviderRsp) o;
        return Objects.equals(this.id, modelServiceProviderRsp.id) && Objects.equals(this.providerName,
            modelServiceProviderRsp.providerName) && Objects.equals(this.providerNameEn,
            modelServiceProviderRsp.providerNameEn) && Objects.equals(this.description,
            modelServiceProviderRsp.description) && Objects.equals(this.tags, modelServiceProviderRsp.tags)
            && Objects.equals(this.providerUrl, modelServiceProviderRsp.providerUrl) && Objects.equals(this.logo,
            modelServiceProviderRsp.logo) && Objects.equals(this.authConfigs, modelServiceProviderRsp.authConfigs)
            && Objects.equals(this.createdDate, modelServiceProviderRsp.createdDate) && Objects.equals(
            this.lastUpdatedDate, modelServiceProviderRsp.lastUpdatedDate) && Objects.equals(this.domainId,
            modelServiceProviderRsp.domainId) && Objects.equals(this.projectId, modelServiceProviderRsp.projectId)
            && Objects.equals(this.workspaceId, modelServiceProviderRsp.workspaceId) && Objects.equals(
            this.authConfigStatus, modelServiceProviderRsp.authConfigStatus) && Objects.equals(this.createdByUser,
            modelServiceProviderRsp.createdByUser) && Objects.equals(this.lastUpdatedByUser,
            modelServiceProviderRsp.lastUpdatedByUser);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, providerName, providerNameEn, description, tags, providerUrl, logo, authConfigs,
            createdDate, lastUpdatedDate, domainId, projectId, workspaceId, authConfigStatus, createdByUser,
            lastUpdatedByUser);
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
