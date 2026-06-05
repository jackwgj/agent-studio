/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * JiuwenDeepResearchEvent
 */
@jakarta.annotation.Generated(value = "")
@Validated

public class JiuwenDeepResearchEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("conversation_id")
    private String conversationId = null;

    public JiuwenDeepResearchEvent setConversationId(String conversationId) {
        this.conversationId = conversationId;
        return this;
    }

    public String getConversationId() {
        return conversationId;
    }

    @JsonProperty("agent")
    private String agent = null;

    public JiuwenDeepResearchEvent setAgent(String agent) {
        this.agent = agent;
        return this;
    }

    public String getAgent() {
        return agent;
    }

    @JsonProperty("message_id")
    private String messageId = null;

    public JiuwenDeepResearchEvent setMessageId(String messageId) {
        this.messageId = messageId;
        return this;
    }

    public String getMessageId() {
        return messageId;
    }

    @JsonProperty("role")
    private String role = null;

    public JiuwenDeepResearchEvent setRole(String role) {
        this.role = role;
        return this;
    }

    public String getRole() {
        return role;
    }

    @JsonProperty("content")
    private String content = null;

    public JiuwenDeepResearchEvent setContent(String content) {
        this.content = content;
        return this;
    }

    public String getContent() {
        return content;
    }

    @JsonProperty("message_type")
    private String messageType = null;

    public JiuwenDeepResearchEvent setMessageType(String messageType) {
        this.messageType = messageType;
        return this;
    }

    public String getMessageType() {
        return messageType;
    }

    @JsonProperty("event")
    private String event = null;

    public JiuwenDeepResearchEvent setEvent(String event) {
        this.event = event;
        return this;
    }

    public String getEvent() {
        return event;
    }

    @JsonProperty("created_time")
    private Long createdTime = null;

    public JiuwenDeepResearchEvent setCreatedTime(Long createdTime) {
        this.createdTime = createdTime;
        return this;
    }

    public Long getCreatedTime() {
        return createdTime;
    }

    @JsonProperty("section_idx")
    private String sectionIdx = null;

    public JiuwenDeepResearchEvent setSectionIdx(String sectionIdx) {
        this.sectionIdx = sectionIdx;
        return this;
    }

    public String getSectionIdx() {
        return sectionIdx;
    }

    @JsonProperty("latency")
    @Valid
    private Latency latency = null;

    public JiuwenDeepResearchEvent setLatency(Latency latency) {
        this.latency = latency;
        return this;
    }

    public Latency getLatency() {
        return latency;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class JiuwenDeepResearchEvent {\n");

        sb.append("    conversationId: ").append(toIndentedString(conversationId)).append("\n");
        sb.append("    agent: ").append(toIndentedString(agent)).append("\n");
        sb.append("    messageId: ").append(toIndentedString(messageId)).append("\n");
        sb.append("    role: ").append(toIndentedString(role)).append("\n");
        sb.append("    content: ").append(toIndentedString(content)).append("\n");
        sb.append("    messageType: ").append(toIndentedString(messageType)).append("\n");
        sb.append("    event: ").append(toIndentedString(event)).append("\n");
        sb.append("    createdTime: ").append(toIndentedString(createdTime)).append("\n");
        sb.append("    sectionIdx: ").append(toIndentedString(sectionIdx)).append("\n");
        sb.append("    latency: ").append(toIndentedString(latency)).append("\n");
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
        JiuwenDeepResearchEvent jiuwenDeepResearchEvent = (JiuwenDeepResearchEvent) o;
        return Objects.equals(this.conversationId, jiuwenDeepResearchEvent.conversationId) && Objects.equals(this.agent,
            jiuwenDeepResearchEvent.agent) && Objects.equals(this.messageId, jiuwenDeepResearchEvent.messageId)
            && Objects.equals(this.role, jiuwenDeepResearchEvent.role) && Objects.equals(this.content,
            jiuwenDeepResearchEvent.content) && Objects.equals(this.messageType, jiuwenDeepResearchEvent.messageType)
            && Objects.equals(this.event, jiuwenDeepResearchEvent.event) && Objects.equals(this.createdTime,
            jiuwenDeepResearchEvent.createdTime) && Objects.equals(this.sectionIdx, jiuwenDeepResearchEvent.sectionIdx)
            && Objects.equals(this.latency, jiuwenDeepResearchEvent.latency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(conversationId, agent, messageId, role, content, messageType, event, createdTime,
            sectionIdx, latency);
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
