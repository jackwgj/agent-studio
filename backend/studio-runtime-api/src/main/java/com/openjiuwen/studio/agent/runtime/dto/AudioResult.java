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
 * AudioResult
 */

@Validated

public class AudioResult implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("text")
    private String text = null;

    @JsonProperty("analysis_info")
    @Valid
    private AnalysisInfo analysisInfo = null;

    public String getText() {
        return text;
    }

    public AudioResult setText(String text) {
        this.text = text;
        return this;
    }

    public AnalysisInfo getAnalysisInfo() {
        return analysisInfo;
    }

    public AudioResult setAnalysisInfo(AnalysisInfo analysisInfo) {
        this.analysisInfo = analysisInfo;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AudioResult {\n");

        sb.append("    text: ").append(toIndentedString(text)).append("\n");
        sb.append("    analysisInfo: ").append(toIndentedString(analysisInfo)).append("\n");
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
        AudioResult audioResult = (AudioResult) o;
        return Objects.equals(this.text, audioResult.text) && Objects.equals(this.analysisInfo,
            audioResult.analysisInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, analysisInfo);
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
