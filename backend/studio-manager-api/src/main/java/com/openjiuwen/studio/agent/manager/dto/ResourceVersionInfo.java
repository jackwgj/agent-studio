/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * ResourceVersionInfo
 */

@Validated

public class ResourceVersionInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("version_id")
    private String versionId = null;

    @JsonProperty("version_name")
    private String versionName = null;

    public String getVersionId() {
        return versionId;
    }

    public ResourceVersionInfo setVersionId(String versionId) {
        this.versionId = versionId;
        return this;
    }

    public String getVersionName() {
        return versionName;
    }

    public ResourceVersionInfo setVersionName(String versionName) {
        this.versionName = versionName;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ResourceVersionInfo {\n");

        sb.append("    versionId: ").append(toIndentedString(versionId)).append("\n");
        sb.append("    versionName: ").append(toIndentedString(versionName)).append("\n");
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
        ResourceVersionInfo resourceVersionInfo = (ResourceVersionInfo) o;
        return Objects.equals(this.versionId, resourceVersionInfo.versionId) && Objects.equals(this.versionName,
            resourceVersionInfo.versionName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(versionId, versionName);
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
