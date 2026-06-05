/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * AgentInteractionReq
 */

@Validated

public class AgentInteractionReq implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("domain_id")
    private String domainId = null;

    @JsonProperty("project_id")
    private String projectId = null;

    @JsonProperty("space_id")
    private String spaceId = null;

    @JsonProperty("session_id")
    private String sessionId = null;

    @JsonProperty("trace_id")
    private String traceId = null;

    @JsonProperty("span_id")
    private String spanId = null;

    @JsonProperty("user_id")
    private String userId = null;

    @JsonProperty("resource_id")
    private String resourceId = null;

    @JsonProperty("start_time")
    private Long startTime = null;

    @JsonProperty("input")
    private String input = null;

    @JsonProperty("output")
    private String output = null;

    public String getDomainId() {
        return domainId;
    }

    public AgentInteractionReq setDomainId(String domainId) {
        this.domainId = domainId;
        return this;
    }

    public String getProjectId() {
        return projectId;
    }

    public AgentInteractionReq setProjectId(String projectId) {
        this.projectId = projectId;
        return this;
    }

    public String getSpaceId() {
        return spaceId;
    }

    public AgentInteractionReq setSpaceId(String spaceId) {
        this.spaceId = spaceId;
        return this;
    }

    public String getSessionId() {
        return sessionId;
    }

    public AgentInteractionReq setSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    public String getTraceId() {
        return traceId;
    }

    public AgentInteractionReq setTraceId(String traceId) {
        this.traceId = traceId;
        return this;
    }

    public String getSpanId() {
        return spanId;
    }

    public AgentInteractionReq setSpanId(String spanId) {
        this.spanId = spanId;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public AgentInteractionReq setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getResourceId() {
        return resourceId;
    }

    public AgentInteractionReq setResourceId(String resourceId) {
        this.resourceId = resourceId;
        return this;
    }

    public Long getStartTime() {
        return startTime;
    }

    public AgentInteractionReq setStartTime(Long startTime) {
        this.startTime = startTime;
        return this;
    }

    public String getInput() {
        return input;
    }

    public AgentInteractionReq setInput(String input) {
        this.input = input;
        return this;
    }

    public String getOutput() {
        return output;
    }

    public AgentInteractionReq setOutput(String output) {
        this.output = output;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AgentInteractionReq {\n");

        sb.append("    domainId: ").append(toIndentedString(domainId)).append("\n");
        sb.append("    projectId: ").append(toIndentedString(projectId)).append("\n");
        sb.append("    spaceId: ").append(toIndentedString(spaceId)).append("\n");
        sb.append("    sessionId: ").append(toIndentedString(sessionId)).append("\n");
        sb.append("    traceId: ").append(toIndentedString(traceId)).append("\n");
        sb.append("    spanId: ").append(toIndentedString(spanId)).append("\n");
        sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
        sb.append("    resourceId: ").append(toIndentedString(resourceId)).append("\n");
        sb.append("    startTime: ").append(toIndentedString(startTime)).append("\n");
        sb.append("    input: ").append(toIndentedString(input)).append("\n");
        sb.append("    output: ").append(toIndentedString(output)).append("\n");
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
        AgentInteractionReq agentInteractionReq = (AgentInteractionReq) o;
        return Objects.equals(this.domainId, agentInteractionReq.domainId) && Objects.equals(this.projectId,
            agentInteractionReq.projectId) && Objects.equals(this.spaceId, agentInteractionReq.spaceId)
            && Objects.equals(this.sessionId, agentInteractionReq.sessionId) && Objects.equals(this.traceId,
            agentInteractionReq.traceId) && Objects.equals(this.spanId, agentInteractionReq.spanId) && Objects.equals(
            this.userId, agentInteractionReq.userId) && Objects.equals(this.resourceId, agentInteractionReq.resourceId)
            && Objects.equals(this.startTime, agentInteractionReq.startTime) && Objects.equals(this.input,
            agentInteractionReq.input) && Objects.equals(this.output, agentInteractionReq.output);
    }

    @Override
    public int hashCode() {
        return Objects.hash(domainId, projectId, spaceId, sessionId, traceId, spanId, userId, resourceId, startTime,
            input, output);
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
