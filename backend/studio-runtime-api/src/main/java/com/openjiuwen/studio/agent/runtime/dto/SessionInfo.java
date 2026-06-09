/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * SessionInfo
 */

@Validated

public class SessionInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("session_id")
    private String sessionId = null;

    @JsonProperty("start_time")
    private Long startTime = null;

    @JsonProperty("end_time")
    private Long endTime = null;

    @JsonProperty("resource_id")
    private String resourceId = null;

    @JsonProperty("resource_name")
    private String resourceName = null;

    @JsonProperty("domain_id")
    private String domainId = null;

    @JsonProperty("project_id")
    private String projectId = null;

    @JsonProperty("space_id")
    private String spaceId = null;

    @JsonProperty("user_id")
    private String userId = null;

    @JsonProperty("user_name")
    private String userName = null;

    @JsonProperty("platform")
    private String platform = null;

    @JsonProperty("call_type")
    private String callType = null;

    public String getSessionId() {
        return sessionId;
    }

    public SessionInfo setSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    public Long getStartTime() {
        return startTime;
    }

    public SessionInfo setStartTime(Long startTime) {
        this.startTime = startTime;
        return this;
    }

    public Long getEndTime() {
        return endTime;
    }

    public SessionInfo setEndTime(Long endTime) {
        this.endTime = endTime;
        return this;
    }

    public String getResourceId() {
        return resourceId;
    }

    public SessionInfo setResourceId(String resourceId) {
        this.resourceId = resourceId;
        return this;
    }

    public String getResourceName() {
        return resourceName;
    }

    public SessionInfo setResourceName(String resourceName) {
        this.resourceName = resourceName;
        return this;
    }

    public String getDomainId() {
        return domainId;
    }

    public SessionInfo setDomainId(String domainId) {
        this.domainId = domainId;
        return this;
    }

    public String getProjectId() {
        return projectId;
    }

    public SessionInfo setProjectId(String projectId) {
        this.projectId = projectId;
        return this;
    }

    public String getSpaceId() {
        return spaceId;
    }

    public SessionInfo setSpaceId(String spaceId) {
        this.spaceId = spaceId;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public SessionInfo setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getUserName() {
        return userName;
    }

    public SessionInfo setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public String getPlatform() {
        return platform;
    }

    public SessionInfo setPlatform(String platform) {
        this.platform = platform;
        return this;
    }

    public String getCallType() {
        return callType;
    }

    public SessionInfo setCallType(String callType) {
        this.callType = callType;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class SessionInfo {\n");

        sb.append("    sessionId: ").append(toIndentedString(sessionId)).append("\n");
        sb.append("    startTime: ").append(toIndentedString(startTime)).append("\n");
        sb.append("    endTime: ").append(toIndentedString(endTime)).append("\n");
        sb.append("    resourceId: ").append(toIndentedString(resourceId)).append("\n");
        sb.append("    resourceName: ").append(toIndentedString(resourceName)).append("\n");
        sb.append("    domainId: ").append(toIndentedString(domainId)).append("\n");
        sb.append("    projectId: ").append(toIndentedString(projectId)).append("\n");
        sb.append("    spaceId: ").append(toIndentedString(spaceId)).append("\n");
        sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
        sb.append("    userName: ").append(toIndentedString(userName)).append("\n");
        sb.append("    platform: ").append(toIndentedString(platform)).append("\n");
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
        SessionInfo sessionInfo = (SessionInfo) o;
        return Objects.equals(this.sessionId, sessionInfo.sessionId) && Objects.equals(this.startTime,
            sessionInfo.startTime) && Objects.equals(this.endTime, sessionInfo.endTime) && Objects.equals(
            this.resourceId, sessionInfo.resourceId) && Objects.equals(this.resourceName, sessionInfo.resourceName)
            && Objects.equals(this.domainId, sessionInfo.domainId) && Objects.equals(this.projectId,
            sessionInfo.projectId) && Objects.equals(this.spaceId, sessionInfo.spaceId) && Objects.equals(this.userId,
            sessionInfo.userId) && Objects.equals(this.userName, sessionInfo.userName) && Objects.equals(this.platform,
            sessionInfo.platform) && Objects.equals(this.callType, sessionInfo.callType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, startTime, endTime, resourceId, resourceName, domainId, projectId, spaceId,
            userId, userName, platform, callType);
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
