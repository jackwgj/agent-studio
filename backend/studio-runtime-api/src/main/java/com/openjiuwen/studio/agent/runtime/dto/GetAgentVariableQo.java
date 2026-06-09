/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * GetAgentVariableQo: converted from multi query params
 */
@ApiModel(description = "GetAgentVariableQo: converted from multi query params")

@Validated

public class GetAgentVariableQo implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("resource_type")
    private String resourceType = "workflow";

    @JsonProperty("version_id")
    private String versionId = null;

    public String getResourceType() {
        return resourceType;
    }

    public GetAgentVariableQo setResourceType(String resourceType) {
        this.resourceType = resourceType;
        return this;
    }

    public String getVersionId() {
        return versionId;
    }

    public GetAgentVariableQo setVersionId(String versionId) {
        this.versionId = versionId;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class GetAgentVariableQo {\n");

        sb.append("    resourceType: ").append(toIndentedString(resourceType)).append("\n");
        sb.append("    versionId: ").append(toIndentedString(versionId)).append("\n");
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
        GetAgentVariableQo getAgentVariableQo = (GetAgentVariableQo) o;
        return Objects.equals(this.resourceType, getAgentVariableQo.resourceType) && Objects.equals(this.versionId,
            getAgentVariableQo.versionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourceType, versionId);
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
