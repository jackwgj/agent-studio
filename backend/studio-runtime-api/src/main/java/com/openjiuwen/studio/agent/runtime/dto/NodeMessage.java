/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * NodeMessage
 */

@Validated

public class NodeMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("role")
    @Length(min = 1, max = 64)
    private String role = null;

    @JsonProperty("content")
    private String content = null;

    @JsonProperty("origin")
    private String origin = null;

    @JsonProperty("nodeId")
    private String nodeId = null;

    @JsonProperty("nodeType")
    @Length(min = 1)
    private String nodeType = null;

    @JsonProperty("nodeName")
    private String nodeName = null;

    @JsonProperty("createdTime")
    private Long createdTime = null;

    public String getRole() {
        return role;
    }

    public NodeMessage setRole(String role) {
        this.role = role;
        return this;
    }

    public String getContent() {
        return content;
    }

    public NodeMessage setContent(String content) {
        this.content = content;
        return this;
    }

    public String getOrigin() {
        return origin;
    }

    public NodeMessage setOrigin(String origin) {
        this.origin = origin;
        return this;
    }

    public String getNodeId() {
        return nodeId;
    }

    public NodeMessage setNodeId(String nodeId) {
        this.nodeId = nodeId;
        return this;
    }

    public String getNodeType() {
        return nodeType;
    }

    public NodeMessage setNodeType(String nodeType) {
        this.nodeType = nodeType;
        return this;
    }

    public String getNodeName() {
        return nodeName;
    }

    public NodeMessage setNodeName(String nodeName) {
        this.nodeName = nodeName;
        return this;
    }

    public Long getCreatedTime() {
        return createdTime;
    }

    public NodeMessage setCreatedTime(Long createdTime) {
        this.createdTime = createdTime;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class NodeMessage {\n");

        sb.append("    role: ").append(toIndentedString(role)).append("\n");
        sb.append("    content: ").append(toIndentedString(content)).append("\n");
        sb.append("    origin: ").append(toIndentedString(origin)).append("\n");
        sb.append("    nodeId: ").append(toIndentedString(nodeId)).append("\n");
        sb.append("    nodeType: ").append(toIndentedString(nodeType)).append("\n");
        sb.append("    nodeName: ").append(toIndentedString(nodeName)).append("\n");
        sb.append("    createdTime: ").append(toIndentedString(createdTime)).append("\n");
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
        NodeMessage nodeMessage = (NodeMessage) o;
        return Objects.equals(this.role, nodeMessage.role) && Objects.equals(this.content, nodeMessage.content)
            && Objects.equals(this.origin, nodeMessage.origin) && Objects.equals(this.nodeId, nodeMessage.nodeId)
            && Objects.equals(this.nodeType, nodeMessage.nodeType) && Objects.equals(this.nodeName,
            nodeMessage.nodeName) && Objects.equals(this.createdTime, nodeMessage.createdTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(role, content, origin, nodeId, nodeType, nodeName, createdTime);
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
