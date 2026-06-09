/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.constraints.NotBlank;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * 应用版本信息
 */
@ApiModel(description = "应用版本信息")

@Validated

public class VersionInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("version_id")
    @NotBlank
    private String versionId = null;

    @JsonProperty("version_name")
    private String versionName = null;

    @JsonProperty("version_note")
    private String versionNote = null;

    @JsonProperty("status")
    private String status = null;

    @JsonProperty("release_time")
    private Date releaseTime = null;

    @JsonProperty("creator")
    private String creator = null;

    @JsonProperty("creator_id")
    private String creatorId = null;

    public String getVersionId() {
        return versionId;
    }

    public VersionInfo setVersionId(String versionId) {
        this.versionId = versionId;
        return this;
    }

    public String getVersionName() {
        return versionName;
    }

    public VersionInfo setVersionName(String versionName) {
        this.versionName = versionName;
        return this;
    }

    public String getVersionNote() {
        return versionNote;
    }

    public VersionInfo setVersionNote(String versionNote) {
        this.versionNote = versionNote;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public VersionInfo setStatus(String status) {
        this.status = status;
        return this;
    }

    public Date getReleaseTime() {
        return releaseTime;
    }

    public VersionInfo setReleaseTime(Date releaseTime) {
        this.releaseTime = releaseTime;
        return this;
    }

    public String getCreator() {
        return creator;
    }

    public VersionInfo setCreator(String creator) {
        this.creator = creator;
        return this;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public VersionInfo setCreatorId(String creatorId) {
        this.creatorId = creatorId;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class VersionInfo {\n");

        sb.append("    versionId: ").append(toIndentedString(versionId)).append("\n");
        sb.append("    versionName: ").append(toIndentedString(versionName)).append("\n");
        sb.append("    versionNote: ").append(toIndentedString(versionNote)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    releaseTime: ").append(toIndentedString(releaseTime)).append("\n");
        sb.append("    creator: ").append(toIndentedString(creator)).append("\n");
        sb.append("    creatorId: ").append(toIndentedString(creatorId)).append("\n");
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
        VersionInfo versionInfo = (VersionInfo) o;
        return Objects.equals(this.versionId, versionInfo.versionId) && Objects.equals(this.versionName,
            versionInfo.versionName) && Objects.equals(this.versionNote, versionInfo.versionNote) && Objects.equals(
            this.status, versionInfo.status) && Objects.equals(this.releaseTime, versionInfo.releaseTime)
            && Objects.equals(this.creator, versionInfo.creator) && Objects.equals(this.creatorId,
            versionInfo.creatorId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(versionId, versionName, versionNote, status, releaseTime, creator, creatorId);
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
