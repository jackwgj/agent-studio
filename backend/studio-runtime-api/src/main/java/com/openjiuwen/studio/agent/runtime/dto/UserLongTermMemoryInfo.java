/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * 用户长期记忆内容
 */
@ApiModel(description = "用户长期记忆内容")

@Validated

public class UserLongTermMemoryInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("memory_id")
    @Length(max = 100)
    private String memoryId = null;

    @JsonProperty("content")
    @Length(max = 10000)
    private String content = null;

    @JsonProperty("create_time")
    @Range(min = 0L, max = 10000000000000L)
    private Long createTime = null;

    @JsonProperty("update_time")
    @Range(min = 0L, max = 10000000000000L)
    private Long updateTime = null;

    public String getMemoryId() {
        return memoryId;
    }

    public UserLongTermMemoryInfo setMemoryId(String memoryId) {
        this.memoryId = memoryId;
        return this;
    }

    public String getContent() {
        return content;
    }

    public UserLongTermMemoryInfo setContent(String content) {
        this.content = content;
        return this;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public UserLongTermMemoryInfo setCreateTime(Long createTime) {
        this.createTime = createTime;
        return this;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public UserLongTermMemoryInfo setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class UserLongTermMemoryInfo {\n");

        sb.append("    memoryId: ").append(toIndentedString(memoryId)).append("\n");
        sb.append("    content: ").append(toIndentedString(content)).append("\n");
        sb.append("    createTime: ").append(toIndentedString(createTime)).append("\n");
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
        UserLongTermMemoryInfo userLongTermMemoryInfo = (UserLongTermMemoryInfo) o;
        return Objects.equals(this.memoryId, userLongTermMemoryInfo.memoryId) && Objects.equals(this.content,
            userLongTermMemoryInfo.content) && Objects.equals(this.createTime, userLongTermMemoryInfo.createTime)
            && Objects.equals(this.updateTime, userLongTermMemoryInfo.updateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memoryId, content, createTime, updateTime);
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
