/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.agent;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * NodeRunInfo
 */
@Validated
public class NodeRunInfo {
    @JsonProperty("agent_id")
    @Length(max = 128)
    private String agentId = null;

    @JsonProperty("node_id")
    @Length(max = 128)
    private String nodeId = null;

    @JsonProperty("parent_node_id")
    @Length(max = 128)
    private String parentNodeId = null;

    @JsonProperty("node_status")
    private NodeStatusEnum nodeStatus = null;

    @JsonProperty("parent_workflow_id")
    @Length(max = 128)
    private String parentWorkflowId = null;

    @JsonProperty("loop_index")
    private Integer loopIndex = null;

    @JsonProperty("loop_node_id")
    @Length(max = 128)
    private String loopNodeId = null;

    @JsonProperty("status")
    @Valid
    private Status status = null;

    @JsonProperty("node_name")
    @Length(max = 128)
    private String nodeName = null;

    @JsonProperty("node_type")
    @Length(max = 32)
    private String nodeType = null;

    @JsonProperty("error_message")
    @Length(max = 4096)
    private String errorMessage = null;

    @JsonProperty("inputs")
    @Valid
    @Size()
    private Map<String, Object> inputs = null;

    @JsonProperty("outputs")
    @Valid
    @Size()
    private Map<String, Object> outputs = null;

    @JsonProperty("messages")
    @Valid
    @Size()
    private List<Message> messages = null;

    @JsonProperty("metadata")
    @Valid
    @Size()
    private Map<String, Object> metadata = null;

    @JsonProperty("start_time")
    private Long startTime = null;

    @JsonProperty("end_time")
    private Long endTime = null;

    @JsonProperty("startup_time")
    private Long startupTime = null;

    @JsonProperty("prefill_time")
    private Long prefillTime = null;

    @JsonProperty("execution_id")
    @Length(max = 128)
    private String executionId = null;

    @JsonProperty("inner_error")
    @Valid
    private NodeRunInfoInnerError innerError = null;

    @JsonProperty("memory")
    @Valid
    @Size()
    private Map<String, Object> memory = null;

    public String getAgentId() {
        return agentId;
    }

    public NodeRunInfo setAgentId(String agentId) {
        this.agentId = agentId;
        return this;
    }

    public String getNodeId() {
        return nodeId;
    }

    public NodeRunInfo setNodeId(String nodeId) {
        this.nodeId = nodeId;
        return this;
    }

    public String getParentNodeId() {
        return parentNodeId;
    }

    public NodeRunInfo setParentNodeId(String parentNodeId) {
        this.parentNodeId = parentNodeId;
        return this;
    }

    public NodeStatusEnum getNodeStatus() {
        return nodeStatus;
    }

    public NodeRunInfo setNodeStatus(NodeStatusEnum nodeStatus) {
        this.nodeStatus = nodeStatus;
        return this;
    }

    public String getParentWorkflowId() {
        return parentWorkflowId;
    }

    public NodeRunInfo setParentWorkflowId(String parentWorkflowId) {
        this.parentWorkflowId = parentWorkflowId;
        return this;
    }

    public Integer getLoopIndex() {
        return loopIndex;
    }

    public NodeRunInfo setLoopIndex(Integer loopIndex) {
        this.loopIndex = loopIndex;
        return this;
    }

    public String getLoopNodeId() {
        return loopNodeId;
    }

    public NodeRunInfo setLoopNodeId(String loopNodeId) {
        this.loopNodeId = loopNodeId;
        return this;
    }

    public Status getStatus() {
        return status;
    }

    public NodeRunInfo setStatus(Status status) {
        this.status = status;
        return this;
    }

    public String getNodeName() {
        return nodeName;
    }

    public NodeRunInfo setNodeName(String nodeName) {
        this.nodeName = nodeName;
        return this;
    }

    public String getNodeType() {
        return nodeType;
    }

    public NodeRunInfo setNodeType(String nodeType) {
        this.nodeType = nodeType;
        return this;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public NodeRunInfo setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    public Map<String, Object> getInputs() {
        return inputs;
    }

    public NodeRunInfo setInputs(Map<String, Object> inputs) {
        this.inputs = inputs;
        return this;
    }

    public Map<String, Object> getOutputs() {
        return outputs;
    }

    public NodeRunInfo setOutputs(Map<String, Object> outputs) {
        this.outputs = outputs;
        return this;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public NodeRunInfo setMessages(List<Message> messages) {
        this.messages = messages;
        return this;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public NodeRunInfo setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
        return this;
    }

    public Long getStartTime() {
        return startTime;
    }

    public NodeRunInfo setStartTime(Long startTime) {
        this.startTime = startTime;
        return this;
    }

    public Long getEndTime() {
        return endTime;
    }

    public NodeRunInfo setEndTime(Long endTime) {
        this.endTime = endTime;
        return this;
    }

    public Long getStartupTime() {
        return startupTime;
    }

    public NodeRunInfo setStartupTime(Long startupTime) {
        this.startupTime = startupTime;
        return this;
    }

    public Long getPrefillTime() {
        return prefillTime;
    }

    public NodeRunInfo setPrefillTime(Long prefillTime) {
        this.prefillTime = prefillTime;
        return this;
    }

    public String getExecutionId() {
        return executionId;
    }

    public NodeRunInfo setExecutionId(String executionId) {
        this.executionId = executionId;
        return this;
    }

    public NodeRunInfoInnerError getInnerError() {
        return innerError;
    }

    public NodeRunInfo setInnerError(NodeRunInfoInnerError innerError) {
        this.innerError = innerError;
        return this;
    }

    public Map<String, Object> getMemory() {
        return memory;
    }

    public NodeRunInfo setMemory(Map<String, Object> memory) {
        this.memory = memory;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class NodeRunInfo {\n");

        sb.append("    agentId: ").append(toIndentedString(agentId)).append("\n");
        sb.append("    nodeId: ").append(toIndentedString(nodeId)).append("\n");
        sb.append("    parentNodeId: ").append(toIndentedString(parentNodeId)).append("\n");
        sb.append("    nodeStatus: ").append(toIndentedString(nodeStatus)).append("\n");
        sb.append("    parentWorkflowId: ").append(toIndentedString(parentWorkflowId)).append("\n");
        sb.append("    loopIndex: ").append(toIndentedString(loopIndex)).append("\n");
        sb.append("    loopNodeId: ").append(toIndentedString(loopNodeId)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    nodeName: ").append(toIndentedString(nodeName)).append("\n");
        sb.append("    nodeType: ").append(toIndentedString(nodeType)).append("\n");
        sb.append("    errorMessage: ").append(toIndentedString(errorMessage)).append("\n");
        sb.append("    inputs: ").append(toIndentedString(inputs)).append("\n");
        sb.append("    outputs: ").append(toIndentedString(outputs)).append("\n");
        sb.append("    messages: ").append(toIndentedString(messages)).append("\n");
        sb.append("    metadata: ").append(toIndentedString(metadata)).append("\n");
        sb.append("    startTime: ").append(toIndentedString(startTime)).append("\n");
        sb.append("    endTime: ").append(toIndentedString(endTime)).append("\n");
        sb.append("    startupTime: ").append(toIndentedString(startupTime)).append("\n");
        sb.append("    prefillTime: ").append(toIndentedString(prefillTime)).append("\n");
        sb.append("    executionId: ").append(toIndentedString(executionId)).append("\n");
        sb.append("    innerError: ").append(toIndentedString(innerError)).append("\n");
        sb.append("    memory: ").append(toIndentedString(memory)).append("\n");
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
        NodeRunInfo nodeRunInfo = (NodeRunInfo) o;
        return Objects.equals(this.agentId, nodeRunInfo.agentId) && Objects.equals(this.nodeId, nodeRunInfo.nodeId)
            && Objects.equals(this.parentNodeId, nodeRunInfo.parentNodeId) && Objects.equals(this.nodeStatus,
            nodeRunInfo.nodeStatus) && Objects.equals(this.parentWorkflowId, nodeRunInfo.parentWorkflowId)
            && Objects.equals(this.loopIndex, nodeRunInfo.loopIndex) && Objects.equals(this.loopNodeId,
            nodeRunInfo.loopNodeId) && Objects.equals(this.status, nodeRunInfo.status) && Objects.equals(this.nodeName,
            nodeRunInfo.nodeName) && Objects.equals(this.nodeType, nodeRunInfo.nodeType) && Objects.equals(
            this.errorMessage, nodeRunInfo.errorMessage) && Objects.equals(this.inputs, nodeRunInfo.inputs)
            && Objects.equals(this.outputs, nodeRunInfo.outputs) && Objects.equals(this.messages, nodeRunInfo.messages)
            && Objects.equals(this.metadata, nodeRunInfo.metadata) && Objects.equals(this.startTime,
            nodeRunInfo.startTime) && Objects.equals(this.endTime, nodeRunInfo.endTime) && Objects.equals(
            this.startupTime, nodeRunInfo.startupTime) && Objects.equals(this.prefillTime, nodeRunInfo.prefillTime)
            && Objects.equals(this.executionId, nodeRunInfo.executionId) && Objects.equals(this.innerError,
            nodeRunInfo.innerError) && Objects.equals(this.memory, nodeRunInfo.memory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(agentId, nodeId, parentNodeId, nodeStatus, parentWorkflowId, loopIndex, loopNodeId, status,
            nodeName, nodeType, errorMessage, inputs, outputs, messages, metadata, startTime, endTime, startupTime,
            prefillTime, executionId, innerError, memory);
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

    /**
     * 工作流节点状态
     */
    public enum NodeStatusEnum {
        STARTED("node_started"),
        WAIT("node_wait"),
        FINISHED("node_finished");

        private String value;

        NodeStatusEnum(String value) {
            this.value = value;
        }

        @JsonCreator
        public static NodeStatusEnum fromValue(String text) {
            for (NodeStatusEnum b : NodeStatusEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }
    }
}
