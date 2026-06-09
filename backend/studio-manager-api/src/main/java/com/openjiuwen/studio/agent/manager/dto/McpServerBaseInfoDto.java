/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * mcp 服务信息
 */
@ApiModel(description = "mcp 服务信息")

@Validated

public class McpServerBaseInfoDto implements Serializable {
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

    @JsonProperty("org_type")
    private String orgType = null;

    @JsonProperty("view_times")
    private Long viewTimes = null;

    @JsonProperty("install_times")
    private Long installTimes = null;

    @JsonProperty("score")
    private Double score = null;

    @JsonProperty("category")
    private String category = null;

    @JsonProperty("server_config")
    private String serverConfig = null;

    @JsonProperty("last_updated_date")
    private Date lastUpdatedDate = null;

    @JsonProperty("created_date")
    private Date createdDate = null;

    public String getId() {
        return id;
    }

    public McpServerBaseInfoDto setId(String id) {
        this.id = id;
        return this;
    }

    public String getIcon() {
        return icon;
    }

    public McpServerBaseInfoDto setIcon(String icon) {
        this.icon = icon;
        return this;
    }

    public String getName() {
        return name;
    }

    public McpServerBaseInfoDto setName(String name) {
        this.name = name;
        return this;
    }

    public String getNameEn() {
        return nameEn;
    }

    public McpServerBaseInfoDto setNameEn(String nameEn) {
        this.nameEn = nameEn;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public McpServerBaseInfoDto setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getDescriptionEn() {
        return descriptionEn;
    }

    public McpServerBaseInfoDto setDescriptionEn(String descriptionEn) {
        this.descriptionEn = descriptionEn;
        return this;
    }

    public String getType() {
        return type;
    }

    public McpServerBaseInfoDto setType(String type) {
        this.type = type;
        return this;
    }

    public String getDeployType() {
        return deployType;
    }

    public McpServerBaseInfoDto setDeployType(String deployType) {
        this.deployType = deployType;
        return this;
    }

    public String getOrgType() {
        return orgType;
    }

    public McpServerBaseInfoDto setOrgType(String orgType) {
        this.orgType = orgType;
        return this;
    }

    public Long getViewTimes() {
        return viewTimes;
    }

    public McpServerBaseInfoDto setViewTimes(Long viewTimes) {
        this.viewTimes = viewTimes;
        return this;
    }

    public Long getInstallTimes() {
        return installTimes;
    }

    public McpServerBaseInfoDto setInstallTimes(Long installTimes) {
        this.installTimes = installTimes;
        return this;
    }

    public Double getScore() {
        return score;
    }

    public McpServerBaseInfoDto setScore(Double score) {
        this.score = score;
        return this;
    }

    public String getCategory() {
        return category;
    }

    public McpServerBaseInfoDto setCategory(String category) {
        this.category = category;
        return this;
    }

    public String getServerConfig() {
        return serverConfig;
    }

    public McpServerBaseInfoDto setServerConfig(String serverConfig) {
        this.serverConfig = serverConfig;
        return this;
    }

    public Date getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public McpServerBaseInfoDto setLastUpdatedDate(Date lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
        return this;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public McpServerBaseInfoDto setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class McpServerBaseInfoDto {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    icon: ").append(toIndentedString(icon)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    nameEn: ").append(toIndentedString(nameEn)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    descriptionEn: ").append(toIndentedString(descriptionEn)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    deployType: ").append(toIndentedString(deployType)).append("\n");
        sb.append("    orgType: ").append(toIndentedString(orgType)).append("\n");
        sb.append("    viewTimes: ").append(toIndentedString(viewTimes)).append("\n");
        sb.append("    installTimes: ").append(toIndentedString(installTimes)).append("\n");
        sb.append("    score: ").append(toIndentedString(score)).append("\n");
        sb.append("    category: ").append(toIndentedString(category)).append("\n");
        sb.append("    serverConfig: ").append(toIndentedString(serverConfig)).append("\n");
        sb.append("    lastUpdatedDate: ").append(toIndentedString(lastUpdatedDate)).append("\n");
        sb.append("    createdDate: ").append(toIndentedString(createdDate)).append("\n");
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
        McpServerBaseInfoDto mcpServerBaseInfoDto = (McpServerBaseInfoDto) o;
        return Objects.equals(this.id, mcpServerBaseInfoDto.id) && Objects.equals(this.icon, mcpServerBaseInfoDto.icon)
            && Objects.equals(this.name, mcpServerBaseInfoDto.name) && Objects.equals(this.nameEn,
            mcpServerBaseInfoDto.nameEn) && Objects.equals(this.description, mcpServerBaseInfoDto.description)
            && Objects.equals(this.descriptionEn, mcpServerBaseInfoDto.descriptionEn) && Objects.equals(this.type,
            mcpServerBaseInfoDto.type) && Objects.equals(this.deployType, mcpServerBaseInfoDto.deployType)
            && Objects.equals(this.orgType, mcpServerBaseInfoDto.orgType) && Objects.equals(this.viewTimes,
            mcpServerBaseInfoDto.viewTimes) && Objects.equals(this.installTimes, mcpServerBaseInfoDto.installTimes)
            && Objects.equals(this.score, mcpServerBaseInfoDto.score) && Objects.equals(this.category,
            mcpServerBaseInfoDto.category) && Objects.equals(this.serverConfig, mcpServerBaseInfoDto.serverConfig)
            && Objects.equals(this.lastUpdatedDate, mcpServerBaseInfoDto.lastUpdatedDate) && Objects.equals(
            this.createdDate, mcpServerBaseInfoDto.createdDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, icon, name, nameEn, description, descriptionEn, type, deployType, orgType, viewTimes,
            installTimes, score, category, serverConfig, lastUpdatedDate, createdDate);
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
