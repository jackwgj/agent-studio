/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * OcrResultInfoWordsBlockList
 */

@Validated

public class OcrResultInfoWordsBlockList implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("words")
    private String words = null;

    @JsonProperty("confidence")
    private Float confidence = null;

    public String getWords() {
        return words;
    }

    public OcrResultInfoWordsBlockList setWords(String words) {
        this.words = words;
        return this;
    }

    public Float getConfidence() {
        return confidence;
    }

    public OcrResultInfoWordsBlockList setConfidence(Float confidence) {
        this.confidence = confidence;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class OcrResultInfoWordsBlockList {\n");

        sb.append("    words: ").append(toIndentedString(words)).append("\n");
        sb.append("    confidence: ").append(toIndentedString(confidence)).append("\n");
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
        OcrResultInfoWordsBlockList ocrResultInfoWordsBlockList = (OcrResultInfoWordsBlockList) o;
        return Objects.equals(this.words, ocrResultInfoWordsBlockList.words) && Objects.equals(this.confidence,
            ocrResultInfoWordsBlockList.confidence);
    }

    @Override
    public int hashCode() {
        return Objects.hash(words, confidence);
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
