/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 记忆库，包含若干文档
 */
@ApiModel(description = "记忆库，包含若干文档")

@Validated

public class ShowMemoryRepoResponseBody implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("memory_repo_id")
    @Length(min = 1, max = 64)
    private String memoryRepoId = null;

    @JsonProperty("name")
    @Length(min = 1, max = 64)
    private String name = null;

    @JsonProperty("description")
    private String description = null;

    @JsonProperty("icon")
    private String icon = null;

    @JsonProperty("long_term_memory_strategies")
    @Valid
    @Size(max = 200)
    private List<LongTermMemoryStrategy> longTermMemoryStrategies = null;

    @JsonProperty("created_user_id")
    @Length(max = 100)
    private String createdUserId = null;

    @JsonProperty("created_user_name")
    @Length(max = 100)
    private String createdUserName = null;

    @JsonProperty("create_time")
    private Date createTime = null;

    @JsonProperty("last_update_user_id")
    @Length(max = 100)
    private String lastUpdateUserId = null;

    @JsonProperty("last_update_user_name")
    @Length(max = 100)
    private String lastUpdateUserName = null;

    @JsonProperty("update_time")
    private Date updateTime = null;

    public String getMemoryRepoId() {
        return memoryRepoId;
    }

    public ShowMemoryRepoResponseBody setMemoryRepoId(String memoryRepoId) {
        this.memoryRepoId = memoryRepoId;
        return this;
    }

    public String getName() {
        return name;
    }

    public ShowMemoryRepoResponseBody setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ShowMemoryRepoResponseBody setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getIcon() {
        return icon;
    }

    public ShowMemoryRepoResponseBody setIcon(String icon) {
        this.icon = icon;
        return this;
    }

    public List<LongTermMemoryStrategy> getLongTermMemoryStrategies() {
        return longTermMemoryStrategies;
    }

    public ShowMemoryRepoResponseBody setLongTermMemoryStrategies(
        List<LongTermMemoryStrategy> longTermMemoryStrategies) {
        this.longTermMemoryStrategies = longTermMemoryStrategies;
        return this;
    }

    public String getCreatedUserId() {
        return createdUserId;
    }

    public ShowMemoryRepoResponseBody setCreatedUserId(String createdUserId) {
        this.createdUserId = createdUserId;
        return this;
    }

    public String getCreatedUserName() {
        return createdUserName;
    }

    public ShowMemoryRepoResponseBody setCreatedUserName(String createdUserName) {
        this.createdUserName = createdUserName;
        return this;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public ShowMemoryRepoResponseBody setCreateTime(Date createTime) {
        this.createTime = createTime;
        return this;
    }

    public String getLastUpdateUserId() {
        return lastUpdateUserId;
    }

    public ShowMemoryRepoResponseBody setLastUpdateUserId(String lastUpdateUserId) {
        this.lastUpdateUserId = lastUpdateUserId;
        return this;
    }

    public String getLastUpdateUserName() {
        return lastUpdateUserName;
    }

    public ShowMemoryRepoResponseBody setLastUpdateUserName(String lastUpdateUserName) {
        this.lastUpdateUserName = lastUpdateUserName;
        return this;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public ShowMemoryRepoResponseBody setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ShowMemoryRepoResponseBody {\n");

        sb.append("    memoryRepoId: ").append(toIndentedString(memoryRepoId)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    icon: ").append(toIndentedString(icon)).append("\n");
        sb.append("    longTermMemoryStrategies: ").append(toIndentedString(longTermMemoryStrategies)).append("\n");
        sb.append("    createdUserId: ").append(toIndentedString(createdUserId)).append("\n");
        sb.append("    createdUserName: ").append(toIndentedString(createdUserName)).append("\n");
        sb.append("    createTime: ").append(toIndentedString(createTime)).append("\n");
        sb.append("    lastUpdateUserId: ").append(toIndentedString(lastUpdateUserId)).append("\n");
        sb.append("    lastUpdateUserName: ").append(toIndentedString(lastUpdateUserName)).append("\n");
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
        ShowMemoryRepoResponseBody showMemoryRepoResponseBody = (ShowMemoryRepoResponseBody) o;
        return Objects.equals(this.memoryRepoId, showMemoryRepoResponseBody.memoryRepoId) && Objects.equals(this.name,
            showMemoryRepoResponseBody.name) && Objects.equals(this.description, showMemoryRepoResponseBody.description)
            && Objects.equals(this.icon, showMemoryRepoResponseBody.icon) && Objects.equals(
            this.longTermMemoryStrategies, showMemoryRepoResponseBody.longTermMemoryStrategies) && Objects.equals(
            this.createdUserId, showMemoryRepoResponseBody.createdUserId) && Objects.equals(this.createdUserName,
            showMemoryRepoResponseBody.createdUserName) && Objects.equals(this.createTime,
            showMemoryRepoResponseBody.createTime) && Objects.equals(this.lastUpdateUserId,
            showMemoryRepoResponseBody.lastUpdateUserId) && Objects.equals(this.lastUpdateUserName,
            showMemoryRepoResponseBody.lastUpdateUserName) && Objects.equals(this.updateTime,
            showMemoryRepoResponseBody.updateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memoryRepoId, name, description, icon, longTermMemoryStrategies, createdUserId,
            createdUserName, createTime, lastUpdateUserId, lastUpdateUserName, updateTime);
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
