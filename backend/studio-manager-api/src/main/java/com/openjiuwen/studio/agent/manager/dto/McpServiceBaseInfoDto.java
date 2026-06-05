/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.openjiuwen.studio.agent.common.dto.auth.AuthInfo;
import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * mcp 服务信息
 */
@ApiModel(description = "mcp 服务信息")

@Validated

public class McpServiceBaseInfoDto implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    @Length(max = 64)
    private String id = null;

    @JsonProperty("icon")
    private String icon = null;

    @JsonProperty("name")
    private String name = null;

    @JsonProperty("name_en")
    private String nameEn = null;

    @JsonProperty("description")
    private String description = null;

    @JsonProperty("description_en")
    private String descriptionEn = null;

    @JsonProperty("type")
    private String type = null;

    @JsonProperty("deploy_type")
    private String deployType = null;

    @JsonProperty("fc_instance_status")
    private String fcInstanceStatus = null;

    @JsonProperty("fail_reason_detail")
    @Valid
    private McpFailReasonDetailDto failReasonDetail = null;

    @JsonProperty("view_times")
    private Long viewTimes = null;

    @JsonProperty("install_times")
    private Long installTimes = null;

    @JsonProperty("node_type")
    private String nodeType = null;

    @JsonProperty("created_date")
    private Long createdDate = null;

    @JsonProperty("last_updated_date")
    private Long lastUpdatedDate = null;

    @JsonProperty("visibility")
    private String visibility = null;

    @JsonProperty("resource")
    private String resource = null;

    @JsonProperty("mcp_servers")
    @Valid
    private McpServerRuntimeVo mcpServers = null;

    @JsonProperty("auth_info")
    @Valid
    private AuthInfo authInfo = null;

    @JsonProperty("is_share")
    private Integer isShare = null;

    public String getId() {
        return id;
    }

    public McpServiceBaseInfoDto setId(String id) {
        this.id = id;
        return this;
    }

    public String getIcon() {
        return icon;
    }

    public McpServiceBaseInfoDto setIcon(String icon) {
        this.icon = icon;
        return this;
    }

    public String getName() {
        return name;
    }

    public McpServiceBaseInfoDto setName(String name) {
        this.name = name;
        return this;
    }

    public String getNameEn() {
        return nameEn;
    }

    public McpServiceBaseInfoDto setNameEn(String nameEn) {
        this.nameEn = nameEn;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public McpServiceBaseInfoDto setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getDescriptionEn() {
        return descriptionEn;
    }

    public McpServiceBaseInfoDto setDescriptionEn(String descriptionEn) {
        this.descriptionEn = descriptionEn;
        return this;
    }

    public String getType() {
        return type;
    }

    public McpServiceBaseInfoDto setType(String type) {
        this.type = type;
        return this;
    }

    public String getDeployType() {
        return deployType;
    }

    public McpServiceBaseInfoDto setDeployType(String deployType) {
        this.deployType = deployType;
        return this;
    }

    public String getFcInstanceStatus() {
        return fcInstanceStatus;
    }

    public McpServiceBaseInfoDto setFcInstanceStatus(String fcInstanceStatus) {
        this.fcInstanceStatus = fcInstanceStatus;
        return this;
    }

    public McpFailReasonDetailDto getFailReasonDetail() {
        return failReasonDetail;
    }

    public McpServiceBaseInfoDto setFailReasonDetail(McpFailReasonDetailDto failReasonDetail) {
        this.failReasonDetail = failReasonDetail;
        return this;
    }

    public Long getViewTimes() {
        return viewTimes;
    }

    public McpServiceBaseInfoDto setViewTimes(Long viewTimes) {
        this.viewTimes = viewTimes;
        return this;
    }

    public Long getInstallTimes() {
        return installTimes;
    }

    public McpServiceBaseInfoDto setInstallTimes(Long installTimes) {
        this.installTimes = installTimes;
        return this;
    }

    public String getNodeType() {
        return nodeType;
    }

    public McpServiceBaseInfoDto setNodeType(String nodeType) {
        this.nodeType = nodeType;
        return this;
    }

    public Long getCreatedDate() {
        return createdDate;
    }

    public McpServiceBaseInfoDto setCreatedDate(Long createdDate) {
        this.createdDate = createdDate;
        return this;
    }

    public Long getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public McpServiceBaseInfoDto setLastUpdatedDate(Long lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
        return this;
    }

    public String getVisibility() {
        return visibility;
    }

    public McpServiceBaseInfoDto setVisibility(String visibility) {
        this.visibility = visibility;
        return this;
    }

    public String getResource() {
        return resource;
    }

    public McpServiceBaseInfoDto setResource(String resource) {
        this.resource = resource;
        return this;
    }

    public McpServerRuntimeVo getMcpServers() {
        return mcpServers;
    }

    public McpServiceBaseInfoDto setMcpServers(McpServerRuntimeVo mcpServers) {
        this.mcpServers = mcpServers;
        return this;
    }

    public AuthInfo getAuthInfo() {
        return authInfo;
    }

    public McpServiceBaseInfoDto setAuthInfo(AuthInfo authInfo) {
        this.authInfo = authInfo;
        return this;
    }

    public Integer getIsShare() {
        return isShare;
    }

    public McpServiceBaseInfoDto setIsShare(Integer isShare) {
        this.isShare = isShare;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class McpServiceBaseInfoDto {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    icon: ").append(toIndentedString(icon)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    nameEn: ").append(toIndentedString(nameEn)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    descriptionEn: ").append(toIndentedString(descriptionEn)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    deployType: ").append(toIndentedString(deployType)).append("\n");
        sb.append("    fcInstanceStatus: ").append(toIndentedString(fcInstanceStatus)).append("\n");
        sb.append("    failReasonDetail: ").append(toIndentedString(failReasonDetail)).append("\n");
        sb.append("    viewTimes: ").append(toIndentedString(viewTimes)).append("\n");
        sb.append("    installTimes: ").append(toIndentedString(installTimes)).append("\n");
        sb.append("    nodeType: ").append(toIndentedString(nodeType)).append("\n");
        sb.append("    createdDate: ").append(toIndentedString(createdDate)).append("\n");
        sb.append("    lastUpdatedDate: ").append(toIndentedString(lastUpdatedDate)).append("\n");
        sb.append("    visibility: ").append(toIndentedString(visibility)).append("\n");
        sb.append("    resource: ").append(toIndentedString(resource)).append("\n");
        sb.append("    mcpServers: ").append(toIndentedString(mcpServers)).append("\n");
        sb.append("    authInfo: ").append(toIndentedString(authInfo)).append("\n");
        sb.append("    isShare: ").append(toIndentedString(isShare)).append("\n");
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
        McpServiceBaseInfoDto mcpServiceBaseInfoDto = (McpServiceBaseInfoDto) o;
        return Objects.equals(this.id, mcpServiceBaseInfoDto.id) && Objects.equals(this.icon,
            mcpServiceBaseInfoDto.icon) && Objects.equals(this.name, mcpServiceBaseInfoDto.name) && Objects.equals(
            this.nameEn, mcpServiceBaseInfoDto.nameEn) && Objects.equals(this.description,
            mcpServiceBaseInfoDto.description) && Objects.equals(this.descriptionEn,
            mcpServiceBaseInfoDto.descriptionEn) && Objects.equals(this.type, mcpServiceBaseInfoDto.type)
            && Objects.equals(this.deployType, mcpServiceBaseInfoDto.deployType) && Objects.equals(
            this.fcInstanceStatus, mcpServiceBaseInfoDto.fcInstanceStatus) && Objects.equals(this.failReasonDetail,
            mcpServiceBaseInfoDto.failReasonDetail) && Objects.equals(this.viewTimes, mcpServiceBaseInfoDto.viewTimes)
            && Objects.equals(this.installTimes, mcpServiceBaseInfoDto.installTimes) && Objects.equals(this.nodeType,
            mcpServiceBaseInfoDto.nodeType) && Objects.equals(this.createdDate, mcpServiceBaseInfoDto.createdDate)
            && Objects.equals(this.lastUpdatedDate, mcpServiceBaseInfoDto.lastUpdatedDate) && Objects.equals(
            this.visibility, mcpServiceBaseInfoDto.visibility) && Objects.equals(this.resource,
            mcpServiceBaseInfoDto.resource) && Objects.equals(this.mcpServers, mcpServiceBaseInfoDto.mcpServers)
            && Objects.equals(this.authInfo, mcpServiceBaseInfoDto.authInfo) && Objects.equals(this.isShare,
            mcpServiceBaseInfoDto.isShare);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, icon, name, nameEn, description, descriptionEn, type, deployType, fcInstanceStatus,
            failReasonDetail, viewTimes, installTimes, nodeType, createdDate, lastUpdatedDate, visibility, resource,
            mcpServers, authInfo, isShare);
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
