/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import io.swagger.annotations.ApiModel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * 导入Skill响应体
 */
@ApiModel(description = "导入Skill响应体")

@Validated

public class ImportStudioSkillResponseBody implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("skill_id")
    @Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")
    private String skillId = null;

    @JsonProperty("skill_name")
    @Length(min = 1, max = 64)
    private String skillName = null;

    @JsonProperty("description")
    @NotBlank
    @Length(min = 1, max = 1024)
    private String description = null;

    @JsonProperty("obs_url")
    @NotBlank
    @Length(min = 1, max = 1024)
    private String obsUrl = null;

    @JsonProperty("version_name")
    @Length(min = 1, max = 32)
    private String versionName = null;

    @JsonProperty("status")
    @NotNull
    private SkillStatus status = null;

    @JsonProperty("source")
    @NotNull
    private SourceEnum source = null;

    public String getSkillId() {
        return skillId;
    }

    public ImportStudioSkillResponseBody setSkillId(String skillId) {
        this.skillId = skillId;
        return this;
    }

    public String getSkillName() {
        return skillName;
    }

    public ImportStudioSkillResponseBody setSkillName(String skillName) {
        this.skillName = skillName;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ImportStudioSkillResponseBody setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getObsUrl() {
        return obsUrl;
    }

    public ImportStudioSkillResponseBody setObsUrl(String obsUrl) {
        this.obsUrl = obsUrl;
        return this;
    }

    public String getVersionName() {
        return versionName;
    }

    public ImportStudioSkillResponseBody setVersionName(String versionName) {
        this.versionName = versionName;
        return this;
    }

    public SkillStatus getStatus() {
        return status;
    }

    public ImportStudioSkillResponseBody setStatus(SkillStatus status) {
        this.status = status;
        return this;
    }

    public SourceEnum getSource() {
        return source;
    }

    public ImportStudioSkillResponseBody setSource(SourceEnum source) {
        this.source = source;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ImportStudioSkillResponseBody {\n");

        sb.append("    skillId: ").append(toIndentedString(skillId)).append("\n");
        sb.append("    skillName: ").append(toIndentedString(skillName)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    obsUrl: ").append(toIndentedString(obsUrl)).append("\n");
        sb.append("    versionName: ").append(toIndentedString(versionName)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    source: ").append(toIndentedString(source)).append("\n");
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
        ImportStudioSkillResponseBody importStudioSkillResponseBody = (ImportStudioSkillResponseBody) o;
        return Objects.equals(this.skillId, importStudioSkillResponseBody.skillId) && Objects.equals(this.skillName,
            importStudioSkillResponseBody.skillName) && Objects.equals(this.description,
            importStudioSkillResponseBody.description) && Objects.equals(this.obsUrl,
            importStudioSkillResponseBody.obsUrl) && Objects.equals(this.versionName,
            importStudioSkillResponseBody.versionName) && Objects.equals(this.status,
            importStudioSkillResponseBody.status) && Objects.equals(this.source, importStudioSkillResponseBody.source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(skillId, skillName, description, obsUrl, versionName, status, source);
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

    /**
     * Skill 来源（导入固定为 import）
     */
    public enum SourceEnum {
        IMPORT("import");

        private String value;

        SourceEnum(String value) {
            this.value = value;
        }

        @JsonCreator
        public static SourceEnum fromValue(String text) {
            for (SourceEnum b : SourceEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }
    }
}
