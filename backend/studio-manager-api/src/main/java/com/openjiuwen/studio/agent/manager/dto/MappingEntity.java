/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * 资源与应用的映射关系实体，记录应用引用资源的关联信息，包含引用类型、归属空间、有效性等核心属性
 */
@ApiModel(description = "资源与应用的映射关系实体，记录应用引用资源的关联信息，包含引用类型、归属空间、有效性等核心属性")

@Validated

public class MappingEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("mapping_id")
    private String mappingId = null;

    @JsonProperty("app_id")
    private String appId = null;

    @JsonProperty("app_version")
    private String appVersion = null;

    @JsonProperty("app_type")
    private String appType = null;

    @JsonProperty("app_name")
    private String appName = null;

    @JsonProperty("resource_id")
    private String resourceId = null;

    @JsonProperty("resource_type")
    private String resourceType = null;

    @JsonProperty("resource_name")
    private String resourceName = null;

    @JsonProperty("resource_version")
    private String resourceVersion = null;

    @JsonProperty("resource_desc")
    private String resourceDesc = null;

    @JsonProperty("valid")
    private Boolean valid = null;

    @JsonProperty("created_on")
    private Date createdOn = null;

    @JsonProperty("updated_on")
    private Date updatedOn = null;

    @JsonProperty("app_name_zh")
    private String appNameZh = null;

    @JsonProperty("reference_type")
    private String referenceType = null;

    @JsonProperty("app_workspace_id")
    private String appWorkspaceId = null;

    @JsonProperty("resource_workspace_id")
    private String resourceWorkspaceId = null;

    @JsonProperty("extends")
    private String _extends = null;

    @JsonProperty("sub_type")
    private String subType = null;

    public String getMappingId() {
        return mappingId;
    }

    public MappingEntity setMappingId(String mappingId) {
        this.mappingId = mappingId;
        return this;
    }

    public String getAppId() {
        return appId;
    }

    public MappingEntity setAppId(String appId) {
        this.appId = appId;
        return this;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public MappingEntity setAppVersion(String appVersion) {
        this.appVersion = appVersion;
        return this;
    }

    public String getAppType() {
        return appType;
    }

    public MappingEntity setAppType(String appType) {
        this.appType = appType;
        return this;
    }

    public String getAppName() {
        return appName;
    }

    public MappingEntity setAppName(String appName) {
        this.appName = appName;
        return this;
    }

    public String getResourceId() {
        return resourceId;
    }

    public MappingEntity setResourceId(String resourceId) {
        this.resourceId = resourceId;
        return this;
    }

    public String getResourceType() {
        return resourceType;
    }

    public MappingEntity setResourceType(String resourceType) {
        this.resourceType = resourceType;
        return this;
    }

    public String getResourceName() {
        return resourceName;
    }

    public MappingEntity setResourceName(String resourceName) {
        this.resourceName = resourceName;
        return this;
    }

    public String getResourceVersion() {
        return resourceVersion;
    }

    public MappingEntity setResourceVersion(String resourceVersion) {
        this.resourceVersion = resourceVersion;
        return this;
    }

    public String getResourceDesc() {
        return resourceDesc;
    }

    public MappingEntity setResourceDesc(String resourceDesc) {
        this.resourceDesc = resourceDesc;
        return this;
    }

    public MappingEntity setValid(Boolean valid) {
        this.valid = valid;
        return this;
    }

    public Boolean isValid() {
        return valid;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public MappingEntity setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
        return this;
    }

    public Date getUpdatedOn() {
        return updatedOn;
    }

    public MappingEntity setUpdatedOn(Date updatedOn) {
        this.updatedOn = updatedOn;
        return this;
    }

    public String getAppNameZh() {
        return appNameZh;
    }

    public MappingEntity setAppNameZh(String appNameZh) {
        this.appNameZh = appNameZh;
        return this;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public MappingEntity setReferenceType(String referenceType) {
        this.referenceType = referenceType;
        return this;
    }

    public String getAppWorkspaceId() {
        return appWorkspaceId;
    }

    public MappingEntity setAppWorkspaceId(String appWorkspaceId) {
        this.appWorkspaceId = appWorkspaceId;
        return this;
    }

    public String getResourceWorkspaceId() {
        return resourceWorkspaceId;
    }

    public MappingEntity setResourceWorkspaceId(String resourceWorkspaceId) {
        this.resourceWorkspaceId = resourceWorkspaceId;
        return this;
    }

    public String getExtends() {
        return _extends;
    }

    public MappingEntity setExtends(String _extends) {
        this._extends = _extends;
        return this;
    }

    public String getSubType() {
        return subType;
    }

    public MappingEntity setSubType(String subType) {
        this.subType = subType;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class MappingEntity {\n");

        sb.append("    mappingId: ").append(toIndentedString(mappingId)).append("\n");
        sb.append("    appId: ").append(toIndentedString(appId)).append("\n");
        sb.append("    appVersion: ").append(toIndentedString(appVersion)).append("\n");
        sb.append("    appType: ").append(toIndentedString(appType)).append("\n");
        sb.append("    appName: ").append(toIndentedString(appName)).append("\n");
        sb.append("    resourceId: ").append(toIndentedString(resourceId)).append("\n");
        sb.append("    resourceType: ").append(toIndentedString(resourceType)).append("\n");
        sb.append("    resourceName: ").append(toIndentedString(resourceName)).append("\n");
        sb.append("    resourceVersion: ").append(toIndentedString(resourceVersion)).append("\n");
        sb.append("    resourceDesc: ").append(toIndentedString(resourceDesc)).append("\n");
        sb.append("    valid: ").append(toIndentedString(valid)).append("\n");
        sb.append("    createdOn: ").append(toIndentedString(createdOn)).append("\n");
        sb.append("    updatedOn: ").append(toIndentedString(updatedOn)).append("\n");
        sb.append("    appNameZh: ").append(toIndentedString(appNameZh)).append("\n");
        sb.append("    referenceType: ").append(toIndentedString(referenceType)).append("\n");
        sb.append("    appWorkspaceId: ").append(toIndentedString(appWorkspaceId)).append("\n");
        sb.append("    resourceWorkspaceId: ").append(toIndentedString(resourceWorkspaceId)).append("\n");
        sb.append("    _extends: ").append(toIndentedString(_extends)).append("\n");
        sb.append("    subType: ").append(toIndentedString(subType)).append("\n");
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
        MappingEntity mappingEntity = (MappingEntity) o;
        return Objects.equals(this.mappingId, mappingEntity.mappingId) && Objects.equals(this.appId,
            mappingEntity.appId) && Objects.equals(this.appVersion, mappingEntity.appVersion) && Objects.equals(
            this.appType, mappingEntity.appType) && Objects.equals(this.appName, mappingEntity.appName)
            && Objects.equals(this.resourceId, mappingEntity.resourceId) && Objects.equals(this.resourceType,
            mappingEntity.resourceType) && Objects.equals(this.resourceName, mappingEntity.resourceName)
            && Objects.equals(this.resourceVersion, mappingEntity.resourceVersion) && Objects.equals(this.resourceDesc,
            mappingEntity.resourceDesc) && Objects.equals(this.valid, mappingEntity.valid) && Objects.equals(
            this.createdOn, mappingEntity.createdOn) && Objects.equals(this.updatedOn, mappingEntity.updatedOn)
            && Objects.equals(this.appNameZh, mappingEntity.appNameZh) && Objects.equals(this.referenceType,
            mappingEntity.referenceType) && Objects.equals(this.appWorkspaceId, mappingEntity.appWorkspaceId)
            && Objects.equals(this.resourceWorkspaceId, mappingEntity.resourceWorkspaceId) && Objects.equals(
            this._extends, mappingEntity._extends) && Objects.equals(this.subType, mappingEntity.subType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mappingId, appId, appVersion, appType, appName, resourceId, resourceType, resourceName,
            resourceVersion, resourceDesc, valid, createdOn, updatedOn, appNameZh, referenceType, appWorkspaceId,
            resourceWorkspaceId, _extends, subType);
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
