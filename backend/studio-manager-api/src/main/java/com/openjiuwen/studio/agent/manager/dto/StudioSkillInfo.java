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
 * Skill 基本信息。
 */
@ApiModel(description = "Skill 基本信息。")

@Validated

public class StudioSkillInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("skill_id")
    @Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")
    private String skillId = null;

    @JsonProperty("skill_name")
    @Length(min = 1, max = 64)
    private String skillName = null;

    @JsonProperty("icon")
    private String icon = null;

    @JsonProperty("status")
    private SkillStatus status = null;

    @JsonProperty("source")
    private SkillSource source = null;

    @JsonProperty("description")
    @Length(min = 1, max = 1024)
    private String description = null;

    @JsonProperty("created_time")
    private Long createdTime = null;

    @JsonProperty("creator_name")
    @Length(min = 1, max = 64)
    private String creatorName = null;

    @JsonProperty("updated_time")
    private Long updatedTime = null;

    @JsonProperty("obs_url")
    @Length(min = 1, max = 1024)
    private String obsUrl = null;

    @JsonProperty("obs_path")
    @Length(min = 1, max = 1024)
    private String obsPath = null;

    @JsonProperty("domain_id")
    private String domainId = null;

    @JsonProperty("latest_version")
    @Length(min = 1, max = 64)
    private String latestVersion = null;

    @JsonProperty("used_version")
    @Length(min = 1, max = 64)
    private String usedVersion = null;

    @JsonProperty("used_version_name")
    @Length(min = 1, max = 32)
    private String usedVersionName = null;

    public String getSkillId() {
        return skillId;
    }

    public StudioSkillInfo setSkillId(String skillId) {
        this.skillId = skillId;
        return this;
    }

    public String getSkillName() {
        return skillName;
    }

    public StudioSkillInfo setSkillName(String skillName) {
        this.skillName = skillName;
        return this;
    }

    public String getIcon() {
        return icon;
    }

    public StudioSkillInfo setIcon(String icon) {
        this.icon = icon;
        return this;
    }

    public SkillStatus getStatus() {
        return status;
    }

    public StudioSkillInfo setStatus(SkillStatus status) {
        this.status = status;
        return this;
    }

    public SkillSource getSource() {
        return source;
    }

    public StudioSkillInfo setSource(SkillSource source) {
        this.source = source;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public StudioSkillInfo setDescription(String description) {
        this.description = description;
        return this;
    }

    public Long getCreatedTime() {
        return createdTime;
    }

    public StudioSkillInfo setCreatedTime(Long createdTime) {
        this.createdTime = createdTime;
        return this;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public StudioSkillInfo setCreatorName(String creatorName) {
        this.creatorName = creatorName;
        return this;
    }

    public Long getUpdatedTime() {
        return updatedTime;
    }

    public StudioSkillInfo setUpdatedTime(Long updatedTime) {
        this.updatedTime = updatedTime;
        return this;
    }

    public String getObsUrl() {
        return obsUrl;
    }

    public StudioSkillInfo setObsUrl(String obsUrl) {
        this.obsUrl = obsUrl;
        return this;
    }

    public String getObsPath() {
        return obsPath;
    }

    public StudioSkillInfo setObsPath(String obsPath) {
        this.obsPath = obsPath;
        return this;
    }

    public String getDomainId() {
        return domainId;
    }

    public StudioSkillInfo setDomainId(String domainId) {
        this.domainId = domainId;
        return this;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public StudioSkillInfo setLatestVersion(String latestVersion) {
        this.latestVersion = latestVersion;
        return this;
    }

    public String getUsedVersion() {
        return usedVersion;
    }

    public StudioSkillInfo setUsedVersion(String usedVersion) {
        this.usedVersion = usedVersion;
        return this;
    }

    public String getUsedVersionName() {
        return usedVersionName;
    }

    public StudioSkillInfo setUsedVersionName(String usedVersionName) {
        this.usedVersionName = usedVersionName;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class StudioSkillInfo {\n");

        sb.append("    skillId: ").append(toIndentedString(skillId)).append("\n");
        sb.append("    skillName: ").append(toIndentedString(skillName)).append("\n");
        sb.append("    icon: ").append(toIndentedString(icon)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    source: ").append(toIndentedString(source)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    createdTime: ").append(toIndentedString(createdTime)).append("\n");
        sb.append("    creatorName: ").append(toIndentedString(creatorName)).append("\n");
        sb.append("    updatedTime: ").append(toIndentedString(updatedTime)).append("\n");
        sb.append("    obsUrl: ").append(toIndentedString(obsUrl)).append("\n");
        sb.append("    obsPath: ").append(toIndentedString(obsPath)).append("\n");
        sb.append("    domainId: ").append(toIndentedString(domainId)).append("\n");
        sb.append("    latestVersion: ").append(toIndentedString(latestVersion)).append("\n");
        sb.append("    usedVersion: ").append(toIndentedString(usedVersion)).append("\n");
        sb.append("    usedVersionName: ").append(toIndentedString(usedVersionName)).append("\n");
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
        StudioSkillInfo studioSkillInfo = (StudioSkillInfo) o;
        return Objects.equals(this.skillId, studioSkillInfo.skillId) && Objects.equals(this.skillName,
            studioSkillInfo.skillName) && Objects.equals(this.icon, studioSkillInfo.icon) && Objects.equals(this.status,
            studioSkillInfo.status) && Objects.equals(this.source, studioSkillInfo.source) && Objects.equals(
            this.description, studioSkillInfo.description) && Objects.equals(this.createdTime,
            studioSkillInfo.createdTime) && Objects.equals(this.creatorName, studioSkillInfo.creatorName)
            && Objects.equals(this.updatedTime, studioSkillInfo.updatedTime) && Objects.equals(this.obsUrl,
            studioSkillInfo.obsUrl) && Objects.equals(this.obsPath, studioSkillInfo.obsPath) && Objects.equals(
            this.domainId, studioSkillInfo.domainId) && Objects.equals(this.latestVersion,
            studioSkillInfo.latestVersion) && Objects.equals(this.usedVersion, studioSkillInfo.usedVersion)
            && Objects.equals(this.usedVersionName, studioSkillInfo.usedVersionName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(skillId, skillName, icon, status, source, description, createdTime, creatorName,
            updatedTime, obsUrl, obsPath, domainId, latestVersion, usedVersion, usedVersionName);
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
