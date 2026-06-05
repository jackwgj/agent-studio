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
import java.util.Date;
import java.util.Objects;

/**
 * 数据源响应信息
 */
@ApiModel(description = "数据源响应信息")

@Validated

public class DatasourceBriefRsp implements Serializable {
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

    public String getId() {
        return id;
    }

    public DatasourceBriefRsp setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public DatasourceBriefRsp setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public DatasourceBriefRsp setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getType() {
        return type;
    }

    public DatasourceBriefRsp setType(String type) {
        this.type = type;
        return this;
    }

    public String getInternetAccess() {
        return internetAccess;
    }

    public DatasourceBriefRsp setInternetAccess(String internetAccess) {
        this.internetAccess = internetAccess;
        return this;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public DatasourceBriefRsp setInstanceId(String instanceId) {
        this.instanceId = instanceId;
        return this;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public DatasourceBriefRsp setInstanceName(String instanceName) {
        this.instanceName = instanceName;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public DatasourceBriefRsp setStatus(String status) {
        this.status = status;
        return this;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public DatasourceBriefRsp setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    public String getCreator() {
        return creator;
    }

    public DatasourceBriefRsp setCreator(String creator) {
        this.creator = creator;
        return this;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public DatasourceBriefRsp setCreatorId(String creatorId) {
        this.creatorId = creatorId;
        return this;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public DatasourceBriefRsp setCreateTime(Date createTime) {
        this.createTime = createTime;
        return this;
    }

    public String getUpdater() {
        return updater;
    }

    public DatasourceBriefRsp setUpdater(String updater) {
        this.updater = updater;
        return this;
    }

    public String getUpdaterId() {
        return updaterId;
    }

    public DatasourceBriefRsp setUpdaterId(String updaterId) {
        this.updaterId = updaterId;
        return this;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public DatasourceBriefRsp setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class DatasourceBriefRsp {\n");

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
        DatasourceBriefRsp datasourceBriefRsp = (DatasourceBriefRsp) o;
        return Objects.equals(this.id, datasourceBriefRsp.id) && Objects.equals(this.name, datasourceBriefRsp.name)
            && Objects.equals(this.description, datasourceBriefRsp.description) && Objects.equals(this.type,
            datasourceBriefRsp.type) && Objects.equals(this.internetAccess, datasourceBriefRsp.internetAccess)
            && Objects.equals(this.instanceId, datasourceBriefRsp.instanceId) && Objects.equals(this.instanceName,
            datasourceBriefRsp.instanceName) && Objects.equals(this.status, datasourceBriefRsp.status)
            && Objects.equals(this.errorMessage, datasourceBriefRsp.errorMessage) && Objects.equals(this.creator,
            datasourceBriefRsp.creator) && Objects.equals(this.creatorId, datasourceBriefRsp.creatorId)
            && Objects.equals(this.createTime, datasourceBriefRsp.createTime) && Objects.equals(this.updater,
            datasourceBriefRsp.updater) && Objects.equals(this.updaterId, datasourceBriefRsp.updaterId)
            && Objects.equals(this.updateTime, datasourceBriefRsp.updateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, type, internetAccess, instanceId, instanceName, status, errorMessage,
            creator, creatorId, createTime, updater, updaterId, updateTime);
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
