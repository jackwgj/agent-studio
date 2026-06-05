/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.run;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * TaskRsp
 */
@Validated

public class TaskRsp implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    @Length(max = 64)
    private String id = null;

    @JsonProperty("name")
    @Length(max = 64)
    private String name = null;

    @JsonProperty("conversation_id")
    @Length(max = 64)
    private String conversationId = null;

    @JsonProperty("is_published")
    private Boolean isPublished = null;

    @JsonProperty("workflow")
    @Valid
    private TaskRspWorkflow workflow = null;

    @JsonProperty("status")
    private TaskStatus status = null;

    @JsonProperty("message")
    private String message = null;

    @JsonProperty("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime = null;

    @JsonProperty("start_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime = null;

    @JsonProperty("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime = null;

    @JsonProperty("finish_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date finishTime = null;

    @JsonProperty("inputs")
    @Valid
    private Object inputs = null;

    @JsonProperty("outputs")
    @Valid
    private Object outputs = null;

    public String getId() {
        return id;
    }

    public TaskRsp setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public TaskRsp setName(String name) {
        this.name = name;
        return this;
    }

    public String getConversationId() {
        return conversationId;
    }

    public TaskRsp setConversationId(String conversationId) {
        this.conversationId = conversationId;
        return this;
    }

    public TaskRsp setIsPublished(Boolean isPublished) {
        this.isPublished = isPublished;
        return this;
    }

    public Boolean isIsPublished() {
        return isPublished;
    }

    public TaskRspWorkflow getWorkflow() {
        return workflow;
    }

    public TaskRsp setWorkflow(TaskRspWorkflow workflow) {
        this.workflow = workflow;
        return this;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public TaskRsp setStatus(TaskStatus status) {
        this.status = status;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public TaskRsp setMessage(String message) {
        this.message = message;
        return this;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public TaskRsp setCreateTime(Date createTime) {
        this.createTime = createTime;
        return this;
    }

    public Date getStartTime() {
        return startTime;
    }

    public TaskRsp setStartTime(Date startTime) {
        this.startTime = startTime;
        return this;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public TaskRsp setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
        return this;
    }

    public Date getFinishTime() {
        return finishTime;
    }

    public TaskRsp setFinishTime(Date finishTime) {
        this.finishTime = finishTime;
        return this;
    }

    public Object getInputs() {
        return inputs;
    }

    public TaskRsp setInputs(Object inputs) {
        this.inputs = inputs;
        return this;
    }

    public Object getOutputs() {
        return outputs;
    }

    public TaskRsp setOutputs(Object outputs) {
        this.outputs = outputs;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class TaskRsp {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    conversationId: ").append(toIndentedString(conversationId)).append("\n");
        sb.append("    isPublished: ").append(toIndentedString(isPublished)).append("\n");
        sb.append("    workflow: ").append(toIndentedString(workflow)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    message: ").append(toIndentedString(message)).append("\n");
        sb.append("    createTime: ").append(toIndentedString(createTime)).append("\n");
        sb.append("    startTime: ").append(toIndentedString(startTime)).append("\n");
        sb.append("    updateTime: ").append(toIndentedString(updateTime)).append("\n");
        sb.append("    finishTime: ").append(toIndentedString(finishTime)).append("\n");
        sb.append("    inputs: ").append(toIndentedString(inputs)).append("\n");
        sb.append("    outputs: ").append(toIndentedString(outputs)).append("\n");
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
        TaskRsp taskRsp = (TaskRsp) o;
        return Objects.equals(this.id, taskRsp.id) && Objects.equals(this.name, taskRsp.name) && Objects.equals(
            this.conversationId, taskRsp.conversationId) && Objects.equals(this.isPublished, taskRsp.isPublished)
            && Objects.equals(this.workflow, taskRsp.workflow) && Objects.equals(this.status, taskRsp.status)
            && Objects.equals(this.message, taskRsp.message) && Objects.equals(this.createTime, taskRsp.createTime)
            && Objects.equals(this.startTime, taskRsp.startTime) && Objects.equals(this.updateTime, taskRsp.updateTime)
            && Objects.equals(this.finishTime, taskRsp.finishTime) && Objects.equals(this.inputs, taskRsp.inputs)
            && Objects.equals(this.outputs, taskRsp.outputs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, conversationId, isPublished, workflow, status, message, createTime, startTime,
            updateTime, finishTime, inputs, outputs);
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
