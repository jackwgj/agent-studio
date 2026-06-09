/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * mcp 服务信息
 */
@ApiModel(description = "mcp 服务信息")

@Validated

public class McpServerDetailInfoDto implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    @Pattern(regexp = "^[a-zA-Z0-9-]+$")
    @Length(max = 64)
    private String id = null;

    @JsonProperty("icon")
    private String icon = null;

    @JsonProperty("serverCode")
    @Pattern(regexp = "^(?!_)(?!-)(?!\\d)[a-zA-Z0-9_\\-\\u4e00-\\u9fa5]{2,64}$")
    private String serverCode = null;

    @JsonProperty("name")
    private String name = null;

    @JsonProperty("name_en")
    @Pattern(regexp = "^[a-zA-Z0-9\\s.,!?;:'\"()_（）-]{2,64}$")
    private String nameEn = null;

    @JsonProperty("description")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5_a-zA-Z0-9\\-,.?:;\"'：；“”‘’，。？、()（）/@!！*%# ]*$")
    private String description = null;

    @JsonProperty("description_en")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5_a-zA-Z0-9\\-,.?:;\"'：；“”‘’，。？、()（）/@!！*%# ]*$")
    private String descriptionEn = null;

    @JsonProperty("url")
    @Pattern(regexp = "(http|https)://[a-zA-Z0-9-.]+(:[0-9]+)?(/\\S*)?")
    private String url = null;

    @JsonProperty("type")
    private String type = null;

    @JsonProperty("org_type")
    private String orgType = null;

    @JsonProperty("readme")
    private String readme = null;

    @JsonProperty("server_config")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5_a-zA-Z0-9\\-,.?:;\"'：=；“”‘’//，。？、()（）\\[\\]{}/@!！*%# \\s]*$")
    private String serverConfig = null;

    @JsonProperty("tools")
    private String tools = null;

    @JsonProperty("view_times")
    private Long viewTimes = null;

    @JsonProperty("install_times")
    private Long installTimes = null;

    @JsonProperty("score")
    private Double score = null;

    @JsonProperty("score_avg")
    private Double scoreAvg = null;

    public String getId() {
        return id;
    }

    public McpServerDetailInfoDto setId(String id) {
        this.id = id;
        return this;
    }

    public String getIcon() {
        return icon;
    }

    public McpServerDetailInfoDto setIcon(String icon) {
        this.icon = icon;
        return this;
    }

    public String getServerCode() {
        return serverCode;
    }

    public McpServerDetailInfoDto setServerCode(String serverCode) {
        this.serverCode = serverCode;
        return this;
    }

    public String getName() {
        return name;
    }

    public McpServerDetailInfoDto setName(String name) {
        this.name = name;
        return this;
    }

    public String getNameEn() {
        return nameEn;
    }

    public McpServerDetailInfoDto setNameEn(String nameEn) {
        this.nameEn = nameEn;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public McpServerDetailInfoDto setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getDescriptionEn() {
        return descriptionEn;
    }

    public McpServerDetailInfoDto setDescriptionEn(String descriptionEn) {
        this.descriptionEn = descriptionEn;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public McpServerDetailInfoDto setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getType() {
        return type;
    }

    public McpServerDetailInfoDto setType(String type) {
        this.type = type;
        return this;
    }

    public String getOrgType() {
        return orgType;
    }

    public McpServerDetailInfoDto setOrgType(String orgType) {
        this.orgType = orgType;
        return this;
    }

    public String getReadme() {
        return readme;
    }

    public McpServerDetailInfoDto setReadme(String readme) {
        this.readme = readme;
        return this;
    }

    public String getServerConfig() {
        return serverConfig;
    }

    public McpServerDetailInfoDto setServerConfig(String serverConfig) {
        this.serverConfig = serverConfig;
        return this;
    }

    public String getTools() {
        return tools;
    }

    public McpServerDetailInfoDto setTools(String tools) {
        this.tools = tools;
        return this;
    }

    public Long getViewTimes() {
        return viewTimes;
    }

    public McpServerDetailInfoDto setViewTimes(Long viewTimes) {
        this.viewTimes = viewTimes;
        return this;
    }

    public Long getInstallTimes() {
        return installTimes;
    }

    public McpServerDetailInfoDto setInstallTimes(Long installTimes) {
        this.installTimes = installTimes;
        return this;
    }

    public Double getScore() {
        return score;
    }

    public McpServerDetailInfoDto setScore(Double score) {
        this.score = score;
        return this;
    }

    public Double getScoreAvg() {
        return scoreAvg;
    }

    public McpServerDetailInfoDto setScoreAvg(Double scoreAvg) {
        this.scoreAvg = scoreAvg;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class McpServerDetailInfoDto {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    icon: ").append(toIndentedString(icon)).append("\n");
        sb.append("    serverCode: ").append(toIndentedString(serverCode)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    nameEn: ").append(toIndentedString(nameEn)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    descriptionEn: ").append(toIndentedString(descriptionEn)).append("\n");
        sb.append("    url: ").append(toIndentedString(url)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    orgType: ").append(toIndentedString(orgType)).append("\n");
        sb.append("    readme: ").append(toIndentedString(readme)).append("\n");
        sb.append("    serverConfig: ").append(toIndentedString(serverConfig)).append("\n");
        sb.append("    tools: ").append(toIndentedString(tools)).append("\n");
        sb.append("    viewTimes: ").append(toIndentedString(viewTimes)).append("\n");
        sb.append("    installTimes: ").append(toIndentedString(installTimes)).append("\n");
        sb.append("    score: ").append(toIndentedString(score)).append("\n");
        sb.append("    scoreAvg: ").append(toIndentedString(scoreAvg)).append("\n");
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
        McpServerDetailInfoDto mcpServerDetailInfoDto = (McpServerDetailInfoDto) o;
        return Objects.equals(this.id, mcpServerDetailInfoDto.id) && Objects.equals(this.icon,
            mcpServerDetailInfoDto.icon) && Objects.equals(this.serverCode, mcpServerDetailInfoDto.serverCode)
            && Objects.equals(this.name, mcpServerDetailInfoDto.name) && Objects.equals(this.nameEn,
            mcpServerDetailInfoDto.nameEn) && Objects.equals(this.description, mcpServerDetailInfoDto.description)
            && Objects.equals(this.descriptionEn, mcpServerDetailInfoDto.descriptionEn) && Objects.equals(this.url,
            mcpServerDetailInfoDto.url) && Objects.equals(this.type, mcpServerDetailInfoDto.type) && Objects.equals(
            this.orgType, mcpServerDetailInfoDto.orgType) && Objects.equals(this.readme, mcpServerDetailInfoDto.readme)
            && Objects.equals(this.serverConfig, mcpServerDetailInfoDto.serverConfig) && Objects.equals(this.tools,
            mcpServerDetailInfoDto.tools) && Objects.equals(this.viewTimes, mcpServerDetailInfoDto.viewTimes)
            && Objects.equals(this.installTimes, mcpServerDetailInfoDto.installTimes) && Objects.equals(this.score,
            mcpServerDetailInfoDto.score) && Objects.equals(this.scoreAvg, mcpServerDetailInfoDto.scoreAvg);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, icon, serverCode, name, nameEn, description, descriptionEn, url, type, orgType, readme,
            serverConfig, tools, viewTimes, installTimes, score, scoreAvg);
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
