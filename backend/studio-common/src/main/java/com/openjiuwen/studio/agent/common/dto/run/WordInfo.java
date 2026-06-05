/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.run;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

import org.springframework.validation.annotation.Validated;

import java.util.Objects;

/**
 * WordInfo。
 */
@ApiModel(description = "WordInfo。")
@Validated
public class WordInfo {
    @JsonProperty("start_time")
    private Integer startTime = null;

    @JsonProperty("end_time")
    private Integer endTime = null;

    @JsonProperty("word")
    private String word = null;

    public Integer getStartTime() {
        return startTime;
    }

    public WordInfo setStartTime(Integer startTime) {
        this.startTime = startTime;
        return this;
    }

    public Integer getEndTime() {
        return endTime;
    }

    public WordInfo setEndTime(Integer endTime) {
        this.endTime = endTime;
        return this;
    }

    public String getWord() {
        return word;
    }

    public WordInfo setWord(String word) {
        this.word = word;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("class WordInfo {\n");
        builder.append("    startTime: ").append(toIndentedString(startTime)).append("\n");
        builder.append("    endTime: ").append(toIndentedString(endTime)).append("\n");
        builder.append("    word: ").append(toIndentedString(word)).append("\n");
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
        WordInfo wordInfo = (WordInfo) obj;
        return Objects.equals(this.startTime, wordInfo.startTime) && Objects.equals(this.endTime, wordInfo.endTime)
            && Objects.equals(this.word, wordInfo.word);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startTime, endTime, word);
    }

    private String toIndentedString(Object obj) {
        if (obj == null) {
            return "null";
        }
        return obj.toString().replace("\n", "\n    ");
    }
}
