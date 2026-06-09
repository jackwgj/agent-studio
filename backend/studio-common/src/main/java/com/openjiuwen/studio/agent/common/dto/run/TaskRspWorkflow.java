/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.run;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * 任务名称。
 */
@ApiModel(description = "任务名称。")

@Validated

public class TaskRspWorkflow implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    @Length(max = 64)
    private String id = null;

    @JsonProperty("type")
    @Length(max = 64)
    private String type = null;

    @JsonProperty("version_id")
    @Length(max = 64)
    private String versionId = null;

    public String getId() {
        return id;
    }

    public TaskRspWorkflow setId(String id) {
        this.id = id;
        return this;
    }

    public String getType() {
        return type;
    }

    public TaskRspWorkflow setType(String type) {
        this.type = type;
        return this;
    }

    public String getVersionId() {
        return versionId;
    }

    public TaskRspWorkflow setVersionId(String versionId) {
        this.versionId = versionId;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class TaskRspWorkflow {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
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
        TaskRspWorkflow taskRspWorkflow = (TaskRspWorkflow) o;
        return Objects.equals(this.id, taskRspWorkflow.id) && Objects.equals(this.type, taskRspWorkflow.type)
            && Objects.equals(this.versionId, taskRspWorkflow.versionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, versionId);
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
