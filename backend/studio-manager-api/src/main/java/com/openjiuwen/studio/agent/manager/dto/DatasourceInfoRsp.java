/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.agent.common.dto.agent.node.DatasourceConnectionInfo;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * 数据源响应信息
 */
@ApiModel(description = "数据源响应信息")

@Validated

public class DatasourceInfoRsp implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$")
    @Length(max = 64)
    private String id = null;

    @JsonProperty("name")
    @Length(max = 64)
    private String name = null;

    @JsonProperty("description")
    @Length(max = 2048)
    private String description = null;

    @JsonProperty("type")
    @Length(max = 16)
    private String type = null;

    @JsonProperty("internet_access")
    @Length(max = 16)
    private String internetAccess = null;

    @JsonProperty("instance_id")
    @Length(max = 64)
    private String instanceId = null;

    @JsonProperty("instance_name")
    @Length(max = 256)
    private String instanceName = null;

    @JsonProperty("status")
    @Length(max = 32)
    private String status = null;

    @JsonProperty("error_message")
    @Length(max = 2048)
    private String errorMessage = null;

    @JsonProperty("creator")
    private String creator = null;

    @JsonProperty("creator_id")
    private String creatorId = null;

    @JsonProperty("create_time")
    private Date createTime = null;

    @JsonProperty("updater")
    private String updater = null;

    @JsonProperty("updater_id")
    private String updaterId = null;

    @JsonProperty("update_time")
    private Date updateTime = null;

    @JsonProperty("connection_info")
    @Valid
    private DatasourceConnectionInfo connectionInfo = null;

    public String getId() {
        return id;
    }

    public DatasourceInfoRsp setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public DatasourceInfoRsp setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public DatasourceInfoRsp setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getType() {
        return type;
    }

    public DatasourceInfoRsp setType(String type) {
        this.type = type;
        return this;
    }

    public String getInternetAccess() {
        return internetAccess;
    }

    public DatasourceInfoRsp setInternetAccess(String internetAccess) {
        this.internetAccess = internetAccess;
        return this;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public DatasourceInfoRsp setInstanceId(String instanceId) {
        this.instanceId = instanceId;
        return this;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public DatasourceInfoRsp setInstanceName(String instanceName) {
        this.instanceName = instanceName;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public DatasourceInfoRsp setStatus(String status) {
        this.status = status;
        return this;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public DatasourceInfoRsp setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    public String getCreator() {
        return creator;
    }

    public DatasourceInfoRsp setCreator(String creator) {
        this.creator = creator;
        return this;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public DatasourceInfoRsp setCreatorId(String creatorId) {
        this.creatorId = creatorId;
        return this;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public DatasourceInfoRsp setCreateTime(Date createTime) {
        this.createTime = createTime;
        return this;
    }

    public String getUpdater() {
        return updater;
    }

    public DatasourceInfoRsp setUpdater(String updater) {
        this.updater = updater;
        return this;
    }

    public String getUpdaterId() {
        return updaterId;
    }

    public DatasourceInfoRsp setUpdaterId(String updaterId) {
        this.updaterId = updaterId;
        return this;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public DatasourceInfoRsp setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
        return this;
    }

    public DatasourceConnectionInfo getConnectionInfo() {
        return connectionInfo;
    }

    public DatasourceInfoRsp setConnectionInfo(
        DatasourceConnectionInfo connectionInfo) {
        this.connectionInfo = connectionInfo;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class DatasourceInfoRsp {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    internetAccess: ").append(toIndentedString(internetAccess)).append("\n");
        sb.append("    instanceId: ").append(toIndentedString(instanceId)).append("\n");
        sb.append("    instanceName: ").append(toIndentedString(instanceName)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    errorMessage: ").append(toIndentedString(errorMessage)).append("\n");
        sb.append("    creator: ").append(toIndentedString(creator)).append("\n");
        sb.append("    creatorId: ").append(toIndentedString(creatorId)).append("\n");
        sb.append("    createTime: ").append(toIndentedString(createTime)).append("\n");
        sb.append("    updater: ").append(toIndentedString(updater)).append("\n");
        sb.append("    updaterId: ").append(toIndentedString(updaterId)).append("\n");
        sb.append("    updateTime: ").append(toIndentedString(updateTime)).append("\n");
        sb.append("    connectionInfo: ").append(toIndentedString(connectionInfo)).append("\n");
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
        DatasourceInfoRsp datasourceInfoRsp = (DatasourceInfoRsp) o;
        return Objects.equals(this.id, datasourceInfoRsp.id) && Objects.equals(this.name, datasourceInfoRsp.name)
            && Objects.equals(this.description, datasourceInfoRsp.description) && Objects.equals(this.type,
            datasourceInfoRsp.type) && Objects.equals(this.internetAccess, datasourceInfoRsp.internetAccess)
            && Objects.equals(this.instanceId, datasourceInfoRsp.instanceId) && Objects.equals(this.instanceName,
            datasourceInfoRsp.instanceName) && Objects.equals(this.status, datasourceInfoRsp.status) && Objects.equals(
            this.errorMessage, datasourceInfoRsp.errorMessage) && Objects.equals(this.creator,
            datasourceInfoRsp.creator) && Objects.equals(this.creatorId, datasourceInfoRsp.creatorId) && Objects.equals(
            this.createTime, datasourceInfoRsp.createTime) && Objects.equals(this.updater, datasourceInfoRsp.updater)
            && Objects.equals(this.updaterId, datasourceInfoRsp.updaterId) && Objects.equals(this.updateTime,
            datasourceInfoRsp.updateTime) && Objects.equals(this.connectionInfo, datasourceInfoRsp.connectionInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, type, internetAccess, instanceId, instanceName, status, errorMessage,
            creator, creatorId, createTime, updater, updaterId, updateTime, connectionInfo);
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
