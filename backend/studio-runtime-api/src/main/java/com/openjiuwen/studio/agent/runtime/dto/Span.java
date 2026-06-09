/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * Span
 */

@Validated

public class Span implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("session_id")
    private String sessionId = null;

    @JsonProperty("domain_id")
    private String domainId = null;

    @JsonProperty("project_id")
    private String projectId = null;

    @JsonProperty("trace_id")
    @NotBlank
    private String traceId = null;

    @JsonProperty("space_id")
    private String spaceId = null;

    @JsonProperty("parent_span_id")
    private String parentSpanId = null;

    @JsonProperty("span_id")
    @NotBlank
    private String spanId = null;

    @JsonProperty("span_type")
    @NotBlank
    private String spanType = null;

    @JsonProperty("span_name")
    @NotBlank
    private String spanName = null;

    @JsonProperty("user_id")
    private String userId = null;

    @JsonProperty("resource_id")
    @NotBlank
    private String resourceId = null;

    @JsonProperty("status_code")
    private String statusCode = null;

    @JsonProperty("input")
    private String input = null;

    @JsonProperty("output")
    private String output = null;

    @JsonProperty("platform")
    @NotBlank
    private String platform = null;

    @JsonProperty("start_time")
    @NotNull
    private Long startTime = null;

    @JsonProperty("duration")
    @NotNull
    private Integer duration = null;

    @JsonProperty("model_props")
    @Valid
    private ModelProps modelProps = null;

    @JsonProperty("tokens_props")
    @Valid
    private TokensProps tokensProps = null;

    @JsonProperty("extra")
    @Valid
    @Size()
    private List<ExtraProps> extra = null;

    @JsonProperty("call_type")
    private String callType = null;

    public String getSessionId() {
        return sessionId;
    }

    public Span setSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    public String getDomainId() {
        return domainId;
    }

    public Span setDomainId(String domainId) {
        this.domainId = domainId;
        return this;
    }

    public String getProjectId() {
        return projectId;
    }

    public Span setProjectId(String projectId) {
        this.projectId = projectId;
        return this;
    }

    public String getTraceId() {
        return traceId;
    }

    public Span setTraceId(String traceId) {
        this.traceId = traceId;
        return this;
    }

    public String getSpaceId() {
        return spaceId;
    }

    public Span setSpaceId(String spaceId) {
        this.spaceId = spaceId;
        return this;
    }

    public String getParentSpanId() {
        return parentSpanId;
    }

    public Span setParentSpanId(String parentSpanId) {
        this.parentSpanId = parentSpanId;
        return this;
    }

    public String getSpanId() {
        return spanId;
    }

    public Span setSpanId(String spanId) {
        this.spanId = spanId;
        return this;
    }

    public String getSpanType() {
        return spanType;
    }

    public Span setSpanType(String spanType) {
        this.spanType = spanType;
        return this;
    }

    public String getSpanName() {
        return spanName;
    }

    public Span setSpanName(String spanName) {
        this.spanName = spanName;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public Span setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getResourceId() {
        return resourceId;
    }

    public Span setResourceId(String resourceId) {
        this.resourceId = resourceId;
        return this;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public Span setStatusCode(String statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    public String getInput() {
        return input;
    }

    public Span setInput(String input) {
        this.input = input;
        return this;
    }

    public String getOutput() {
        return output;
    }

    public Span setOutput(String output) {
        this.output = output;
        return this;
    }

    public String getPlatform() {
        return platform;
    }

    public Span setPlatform(String platform) {
        this.platform = platform;
        return this;
    }

    public Long getStartTime() {
        return startTime;
    }

    public Span setStartTime(Long startTime) {
        this.startTime = startTime;
        return this;
    }

    public Integer getDuration() {
        return duration;
    }

    public Span setDuration(Integer duration) {
        this.duration = duration;
        return this;
    }

    public ModelProps getModelProps() {
        return modelProps;
    }

    public Span setModelProps(ModelProps modelProps) {
        this.modelProps = modelProps;
        return this;
    }

    public TokensProps getTokensProps() {
        return tokensProps;
    }

    public Span setTokensProps(TokensProps tokensProps) {
        this.tokensProps = tokensProps;
        return this;
    }

    public List<ExtraProps> getExtra() {
        return extra;
    }

    public Span setExtra(List<ExtraProps> extra) {
        this.extra = extra;
        return this;
    }

    public String getCallType() {
        return callType;
    }

    public Span setCallType(String callType) {
        this.callType = callType;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Span {\n");

        sb.append("    sessionId: ").append(toIndentedString(sessionId)).append("\n");
        sb.append("    domainId: ").append(toIndentedString(domainId)).append("\n");
        sb.append("    projectId: ").append(toIndentedString(projectId)).append("\n");
        sb.append("    traceId: ").append(toIndentedString(traceId)).append("\n");
        sb.append("    spaceId: ").append(toIndentedString(spaceId)).append("\n");
        sb.append("    parentSpanId: ").append(toIndentedString(parentSpanId)).append("\n");
        sb.append("    spanId: ").append(toIndentedString(spanId)).append("\n");
        sb.append("    spanType: ").append(toIndentedString(spanType)).append("\n");
        sb.append("    spanName: ").append(toIndentedString(spanName)).append("\n");
        sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
        sb.append("    resourceId: ").append(toIndentedString(resourceId)).append("\n");
        sb.append("    statusCode: ").append(toIndentedString(statusCode)).append("\n");
        sb.append("    input: ").append(toIndentedString(input)).append("\n");
        sb.append("    output: ").append(toIndentedString(output)).append("\n");
        sb.append("    platform: ").append(toIndentedString(platform)).append("\n");
        sb.append("    startTime: ").append(toIndentedString(startTime)).append("\n");
        sb.append("    duration: ").append(toIndentedString(duration)).append("\n");
        sb.append("    modelProps: ").append(toIndentedString(modelProps)).append("\n");
        sb.append("    tokensProps: ").append(toIndentedString(tokensProps)).append("\n");
        sb.append("    extra: ").append(toIndentedString(extra)).append("\n");
        sb.append("    callType: ").append(toIndentedString(callType)).append("\n");
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
        Span span = (Span) o;
        return Objects.equals(this.sessionId, span.sessionId) && Objects.equals(this.domainId, span.domainId)
            && Objects.equals(this.projectId, span.projectId) && Objects.equals(this.traceId, span.traceId)
            && Objects.equals(this.spaceId, span.spaceId) && Objects.equals(this.parentSpanId, span.parentSpanId)
            && Objects.equals(this.spanId, span.spanId) && Objects.equals(this.spanType, span.spanType)
            && Objects.equals(this.spanName, span.spanName) && Objects.equals(this.userId, span.userId)
            && Objects.equals(this.resourceId, span.resourceId) && Objects.equals(this.statusCode, span.statusCode)
            && Objects.equals(this.input, span.input) && Objects.equals(this.output, span.output) && Objects.equals(
            this.platform, span.platform) && Objects.equals(this.startTime, span.startTime) && Objects.equals(
            this.duration, span.duration) && Objects.equals(this.modelProps, span.modelProps) && Objects.equals(
            this.tokensProps, span.tokensProps) && Objects.equals(this.extra, span.extra) && Objects.equals(
            this.callType, span.callType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, domainId, projectId, traceId, spaceId, parentSpanId, spanId, spanType, spanName,
            userId, resourceId, statusCode, input, output, platform, startTime, duration, modelProps, tokensProps,
            extra, callType);
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
