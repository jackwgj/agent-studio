/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * 数据源请求信息
 */
@ApiModel(description = "数据源请求信息")

@Validated

public class DatasourceInfoReq implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("name")
    @NotBlank
    @Length(max = 64)
    private String name = null;

    @JsonProperty("desc")
    @Length(max = 2048)
    private String desc = null;

    @JsonProperty("type")
    @NotBlank
    @Length(max = 16)
    private String type = null;

    @JsonProperty("internet_access")
    @NotBlank
    @Length(max = 16)
    private String internetAccess = null;

    @JsonProperty("instance_id")
    @Length(max = 64)
    private String instanceId = null;

    @JsonProperty("instance_name")
    @Length(max = 256)
    private String instanceName = null;

    @JsonProperty("connection_info")
    @Valid
    private com.openjiuwen.studio.agent.common.dto.agent.node.DatasourceConnectionInfo connectionInfo = null;

    public String getName() {
        return name;
    }

    public DatasourceInfoReq setName(String name) {
        this.name = name;
        return this;
    }

    public String getDesc() {
        return desc;
    }

    public DatasourceInfoReq setDesc(String desc) {
        this.desc = desc;
        return this;
    }

    public String getType() {
        return type;
    }

    public DatasourceInfoReq setType(String type) {
        this.type = type;
        return this;
    }

    public String getInternetAccess() {
        return internetAccess;
    }

    public DatasourceInfoReq setInternetAccess(String internetAccess) {
        this.internetAccess = internetAccess;
        return this;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public DatasourceInfoReq setInstanceId(String instanceId) {
        this.instanceId = instanceId;
        return this;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public DatasourceInfoReq setInstanceName(String instanceName) {
        this.instanceName = instanceName;
        return this;
    }

    public com.openjiuwen.studio.agent.common.dto.agent.node.DatasourceConnectionInfo getConnectionInfo() {
        return connectionInfo;
    }

    public DatasourceInfoReq setConnectionInfo(
        com.openjiuwen.studio.agent.common.dto.agent.node.DatasourceConnectionInfo connectionInfo) {
        this.connectionInfo = connectionInfo;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class DatasourceInfoReq {\n");

        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    desc: ").append(toIndentedString(desc)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    internetAccess: ").append(toIndentedString(internetAccess)).append("\n");
        sb.append("    instanceId: ").append(toIndentedString(instanceId)).append("\n");
        sb.append("    instanceName: ").append(toIndentedString(instanceName)).append("\n");
        sb.append("    connectionInfo: ").append(toIndentedString(connectionInfo)).append("\n");
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
        DatasourceInfoReq datasourceInfoReq = (DatasourceInfoReq) o;
        return Objects.equals(this.name, datasourceInfoReq.name) && Objects.equals(this.desc, datasourceInfoReq.desc)
            && Objects.equals(this.type, datasourceInfoReq.type) && Objects.equals(this.internetAccess,
            datasourceInfoReq.internetAccess) && Objects.equals(this.instanceId, datasourceInfoReq.instanceId)
            && Objects.equals(this.instanceName, datasourceInfoReq.instanceName) && Objects.equals(this.connectionInfo,
            datasourceInfoReq.connectionInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, desc, type, internetAccess, instanceId, instanceName, connectionInfo);
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
