/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.run;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Objects;

/**
 * Result。
 */
@ApiModel(description = "Result。")
@Validated
public class Result {
    @JsonProperty("text")
    @NotBlank
    private String text = null;

    @JsonProperty("score")
    @NotNull
    private Float score = null;

    @JsonProperty("word_info")
    @Valid
    @Size()
    private List<WordInfo> wordInfo = null;

    public String getText() {
        return text;
    }

    public Result setText(String text) {
        this.text = text;
        return this;
    }

    public Float getScore() {
        return score;
    }

    public Result setScore(Float score) {
        this.score = score;
        return this;
    }

    public List<WordInfo> getWordInfo() {
        return wordInfo;
    }

    public Result setWordInfo(List<WordInfo> wordInfo) {
        this.wordInfo = wordInfo;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("class Result {\n");

        builder.append("    text: ").append(toIndentedString(text)).append("\n");
        builder.append("    score: ").append(toIndentedString(score)).append("\n");
        builder.append("    wordInfo: ").append(toIndentedString(wordInfo)).append("\n");
        builder.append("}");
        return builder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Result result = (Result) obj;
        return Objects.equals(this.text, result.text) && Objects.equals(this.score, result.score) && Objects.equals(
            this.wordInfo, result.wordInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, score, wordInfo);
    }

    private String toIndentedString(Object obj) {
        if (obj == null) {
            return "null";
        }
        return obj.toString().replace("\n", "\n    ");
    }
}
