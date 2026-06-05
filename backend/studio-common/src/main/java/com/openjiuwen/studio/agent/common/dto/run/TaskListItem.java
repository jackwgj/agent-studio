/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.run;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * TaskListItem
 */
@Validated

public class TaskListItem implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    @Length(max = 64)
    private String id = null;

    @JsonProperty("name")
    @Length(max = 64)
    private String name = null;

    @JsonProperty("type")
    @Length(max = 64)
    private String type = null;

    @JsonProperty("is_published")
    private Boolean isPublished = null;

    @JsonProperty("status")
    private TaskStatus status = null;

    @JsonProperty("create_time")
    private Date createTime = null;

    @JsonProperty("finish_time")
    private Date finishTime = null;

    public String getId() {
        return id;
    }

    public TaskListItem setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public TaskListItem setName(String name) {
        this.name = name;
        return this;
    }

    public String getType() {
        return type;
    }

    public TaskListItem setType(String type) {
        this.type = type;
        return this;
    }

    public TaskListItem setIsPublished(Boolean isPublished) {
        this.isPublished = isPublished;
        return this;
    }

    public Boolean isIsPublished() {
        return isPublished;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public TaskListItem setStatus(TaskStatus status) {
        this.status = status;
        return this;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public TaskListItem setCreateTime(Date createTime) {
        this.createTime = createTime;
        return this;
    }

    public Date getFinishTime() {
        return finishTime;
    }

    public TaskListItem setFinishTime(Date finishTime) {
        this.finishTime = finishTime;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class TaskListItem {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    isPublished: ").append(toIndentedString(isPublished)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    createTime: ").append(toIndentedString(createTime)).append("\n");
        sb.append("    finishTime: ").append(toIndentedString(finishTime)).append("\n");
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
        TaskListItem taskListItem = (TaskListItem) o;
        return Objects.equals(this.id, taskListItem.id) && Objects.equals(this.name, taskListItem.name)
            && Objects.equals(this.type, taskListItem.type) && Objects.equals(this.isPublished,
            taskListItem.isPublished) && Objects.equals(this.status, taskListItem.status) && Objects.equals(
            this.createTime, taskListItem.createTime) && Objects.equals(this.finishTime, taskListItem.finishTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, type, isPublished, status, createTime, finishTime);
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
