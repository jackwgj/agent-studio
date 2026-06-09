/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * AnalysisInfo
 */

@Validated

public class AnalysisInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("role")
    private String role = null;

    @JsonProperty("emotion")
    private String emotion = null;

    public String getRole() {
        return role;
    }

    public AnalysisInfo setRole(String role) {
        this.role = role;
        return this;
    }

    public String getEmotion() {
        return emotion;
    }

    public AnalysisInfo setEmotion(String emotion) {
        this.emotion = emotion;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AnalysisInfo {\n");

        sb.append("    role: ").append(toIndentedString(role)).append("\n");
        sb.append("    emotion: ").append(toIndentedString(emotion)).append("\n");
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
        AnalysisInfo analysisInfo = (AnalysisInfo) o;
        return Objects.equals(this.role, analysisInfo.role) && Objects.equals(this.emotion, analysisInfo.emotion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(role, emotion);
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
