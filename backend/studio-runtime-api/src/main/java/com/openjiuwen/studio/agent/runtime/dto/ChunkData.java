/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * **参数解释**： 切片明细。  **取值范围**： 不涉及。
 */
@ApiModel(description = "**参数解释**： 切片明细。  **取值范围**： 不涉及。")

@Validated

public class ChunkData implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("document")
    private String document = null;

    @JsonProperty("chunk_fragments")
    @Valid
    @Size()
    private Map<@Length() String, @Length() String> chunkFragments = null;

    @JsonProperty("similarity")
    private Float similarity = null;

    public String getDocument() {
        return document;
    }

    public ChunkData setDocument(String document) {
        this.document = document;
        return this;
    }

    public Map<String, String> getChunkFragments() {
        return chunkFragments;
    }

    public ChunkData setChunkFragments(Map<String, String> chunkFragments) {
        this.chunkFragments = chunkFragments;
        return this;
    }

    public Float getSimilarity() {
        return similarity;
    }

    public ChunkData setSimilarity(Float similarity) {
        this.similarity = similarity;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ChunkData {\n");

        sb.append("    document: ").append(toIndentedString(document)).append("\n");
        sb.append("    chunkFragments: ").append(toIndentedString(chunkFragments)).append("\n");
        sb.append("    similarity: ").append(toIndentedString(similarity)).append("\n");
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
        ChunkData chunkData = (ChunkData) o;
        return Objects.equals(this.document, chunkData.document) && Objects.equals(this.chunkFragments,
            chunkData.chunkFragments) && Objects.equals(this.similarity, chunkData.similarity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(document, chunkFragments, similarity);
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
