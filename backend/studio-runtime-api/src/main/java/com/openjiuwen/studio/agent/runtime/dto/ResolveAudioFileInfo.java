/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * ResolveAudioFileInfo
 */

@Validated

public class ResolveAudioFileInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("status")
    private String status = null;

    @JsonProperty("error_message")
    private String errorMessage = null;

    @JsonProperty("segments")
    @Valid
    @Size()
    private List<AudioResult> segments = null;

    public String getStatus() {
        return status;
    }

    public ResolveAudioFileInfo setStatus(String status) {
        this.status = status;
        return this;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public ResolveAudioFileInfo setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    public List<AudioResult> getSegments() {
        return segments;
    }

    public ResolveAudioFileInfo setSegments(List<AudioResult> segments) {
        this.segments = segments;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ResolveAudioFileInfo {\n");

        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    errorMessage: ").append(toIndentedString(errorMessage)).append("\n");
        sb.append("    segments: ").append(toIndentedString(segments)).append("\n");
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
        ResolveAudioFileInfo resolveAudioFileInfo = (ResolveAudioFileInfo) o;
        return Objects.equals(this.status, resolveAudioFileInfo.status) && Objects.equals(this.errorMessage,
            resolveAudioFileInfo.errorMessage) && Objects.equals(this.segments, resolveAudioFileInfo.segments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, errorMessage, segments);
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
