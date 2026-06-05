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
 * OcrResultInfo
 */

@Validated

public class OcrResultInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("words_block_count")
    private Integer wordsBlockCount = null;

    @JsonProperty("words_block_list")
    @Valid
    @Size()
    private List<OcrResultInfoWordsBlockList> wordsBlockList = null;

    public Integer getWordsBlockCount() {
        return wordsBlockCount;
    }

    public OcrResultInfo setWordsBlockCount(Integer wordsBlockCount) {
        this.wordsBlockCount = wordsBlockCount;
        return this;
    }

    public List<OcrResultInfoWordsBlockList> getWordsBlockList() {
        return wordsBlockList;
    }

    public OcrResultInfo setWordsBlockList(List<OcrResultInfoWordsBlockList> wordsBlockList) {
        this.wordsBlockList = wordsBlockList;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class OcrResultInfo {\n");

        sb.append("    wordsBlockCount: ").append(toIndentedString(wordsBlockCount)).append("\n");
        sb.append("    wordsBlockList: ").append(toIndentedString(wordsBlockList)).append("\n");
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
        OcrResultInfo ocrResultInfo = (OcrResultInfo) o;
        return Objects.equals(this.wordsBlockCount, ocrResultInfo.wordsBlockCount) && Objects.equals(
            this.wordsBlockList, ocrResultInfo.wordsBlockList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(wordsBlockCount, wordsBlockList);
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
