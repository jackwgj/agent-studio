/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.run;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * CancelTaskRsp
 */
@Validated

public class CancelTaskRsp implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    @Length(max = 64)
    private String id = null;

    @JsonProperty("name")
    @Length(max = 64)
    private String name = null;

    @JsonProperty("workflow_id")
    @Length(max = 64)
    private String workflowId = null;

    @JsonProperty("status")
    private TaskStatus status = null;

    @JsonProperty("message")
    @Length(max = 2048)
    private String message = null;

    public String getId() {
        return id;
    }

    public CancelTaskRsp setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public CancelTaskRsp setName(String name) {
        this.name = name;
        return this;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public CancelTaskRsp setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
        return this;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public CancelTaskRsp setStatus(TaskStatus status) {
        this.status = status;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public CancelTaskRsp setMessage(String message) {
        this.message = message;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class CancelTaskRsp {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    workflowId: ").append(toIndentedString(workflowId)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    message: ").append(toIndentedString(message)).append("\n");
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
        CancelTaskRsp cancelTaskRsp = (CancelTaskRsp) o;
        return Objects.equals(this.id, cancelTaskRsp.id) && Objects.equals(this.name, cancelTaskRsp.name)
            && Objects.equals(this.workflowId, cancelTaskRsp.workflowId) && Objects.equals(this.status,
            cancelTaskRsp.status) && Objects.equals(this.message, cancelTaskRsp.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, workflowId, status, message);
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
