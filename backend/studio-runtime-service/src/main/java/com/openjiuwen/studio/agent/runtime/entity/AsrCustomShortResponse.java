/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;

@JsonIgnoreProperties(
        ignoreUnknown = true
)
public class AsrCustomShortResponse {
    @JsonProperty("trace_id")
    private String traceId;
    @JsonProperty("result")
    private Result result;

    public AsrCustomShortResponse() {
    }

    /** @deprecated */
    @Deprecated
    @JsonIgnore
    public String getText() {
        return this.result.text;
    }

    /** @deprecated */
    @Deprecated
    @JsonIgnore
    public float getScore() {
        return this.result.score;
    }

    @JsonProperty("trace_id")
    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    @JsonProperty("result")
    public void setResult(Result result) {
        this.result = result;
    }

    public String getTraceId() {
        return this.traceId;
    }

    public Result getResult() {
        return this.result;
    }

    @JsonIgnoreProperties(
            ignoreUnknown = true
    )
    @JsonInclude(Include.NON_NULL)
    public static class Result {
        @JsonProperty("text")
        private String text;
        @JsonProperty("score")
        private float score;
        @JsonProperty("word_info")
        private List<WordInfo> wordInfos;

        public Result() {
        }

        @JsonProperty("text")
        public void setText(String text) {
            this.text = text;
        }

        @JsonProperty("score")
        public void setScore(float score) {
            this.score = score;
        }

        @JsonProperty("word_info")
        public void setWordInfos(List<WordInfo> wordInfos) {
            this.wordInfos = wordInfos;
        }

        public String getText() {
            return this.text;
        }

        public float getScore() {
            return this.score;
        }

        public List<WordInfo> getWordInfos() {
            return this.wordInfos;
        }
    }

    @JsonIgnoreProperties(
            ignoreUnknown = true
    )
    public static class WordInfo {
        @JsonProperty("start_time")
        private int startTime;
        @JsonProperty("end_time")
        private int endTime;
        @JsonProperty("word")
        private String word;

        public WordInfo() {
        }

        @JsonProperty("start_time")
        public void setStartTime(int startTime) {
            this.startTime = startTime;
        }

        @JsonProperty("end_time")
        public void setEndTime(int endTime) {
            this.endTime = endTime;
        }

        @JsonProperty("word")
        public void setWord(String word) {
            this.word = word;
        }

        public int getStartTime() {
            return this.startTime;
        }

        public int getEndTime() {
            return this.endTime;
        }

        public String getWord() {
            return this.word;
        }
    }
}
