/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.agent.common.dto.agent.node.DatasourceConnectionInfo;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * 数据源配置信息
 */
@ApiModel(description = "数据源配置信息")

@Validated

public class DatasourceConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$")
    @NotBlank
    @Length(max = 64)
    private String id = null;

    @JsonProperty("project_id")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$")
    @NotBlank
    @Length(max = 64)
    private String projectId = null;

    @JsonProperty("name")
    @Length(max = 64)
    private String name = null;

    @JsonProperty("type")
    @NotBlank
    @Length(max = 16)
    private String type = null;

    @JsonProperty("internet_access")
    @NotBlank
    @Length(max = 16)
    private String internetAccess = null;

    @JsonProperty("connection_info")
    @Valid
    @NotNull
    private DatasourceConnectionInfo connectionInfo = null;

    @JsonProperty("updated_on")
    private Long updatedOn = null;

    public String getId() {
        return id;
    }

    public DatasourceConfig setId(String id) {
        this.id = id;
        return this;
    }

    public String getProjectId() {
        return projectId;
    }

    public DatasourceConfig setProjectId(String projectId) {
        this.projectId = projectId;
        return this;
    }

    public String getName() {
        return name;
    }

    public DatasourceConfig setName(String name) {
        this.name = name;
        return this;
    }

    public String getType() {
        return type;
    }

    public DatasourceConfig setType(String type) {
        this.type = type;
        return this;
    }

    public String getInternetAccess() {
        return internetAccess;
    }

    public DatasourceConfig setInternetAccess(String internetAccess) {
        this.internetAccess = internetAccess;
        return this;
    }

    public DatasourceConnectionInfo getConnectionInfo() {
        return connectionInfo;
    }

    public DatasourceConfig setConnectionInfo(DatasourceConnectionInfo connectionInfo) {
        this.connectionInfo = connectionInfo;
        return this;
    }

    public Long getUpdatedOn() {
        return updatedOn;
    }

    public DatasourceConfig setUpdatedOn(Long updatedOn) {
        this.updatedOn = updatedOn;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class DatasourceConfig {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    projectId: ").append(toIndentedString(projectId)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    internetAccess: ").append(toIndentedString(internetAccess)).append("\n");
        sb.append("    connectionInfo: ").append(toIndentedString(connectionInfo)).append("\n");
        sb.append("    updatedOn: ").append(toIndentedString(updatedOn)).append("\n");
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
        DatasourceConfig datasourceConfig = (DatasourceConfig) o;
        return Objects.equals(this.id, datasourceConfig.id) && Objects.equals(this.projectId,
            datasourceConfig.projectId) && Objects.equals(this.name, datasourceConfig.name) && Objects.equals(this.type,
            datasourceConfig.type) && Objects.equals(this.internetAccess, datasourceConfig.internetAccess)
            && Objects.equals(this.connectionInfo, datasourceConfig.connectionInfo) && Objects.equals(this.updatedOn,
            datasourceConfig.updatedOn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, projectId, name, type, internetAccess, connectionInfo, updatedOn);
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
