/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * 检索结果。
 */
@ApiModel(description = "检索结果。")

@Validated

public class RetrievalResultInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("file_id")
    @Length(min = 1, max = 64)
    private String fileId = null;

    @JsonProperty("title")
    private String title = null;

    @JsonProperty("chunk_id")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$")
    @Length(min = 1, max = 64)
    private String chunkId = null;

    @JsonProperty("content")
    @Length(min = 1, max = 65535)
    private String content = null;

    @JsonProperty("similarity")
    private Float similarity = null;

    @JsonProperty("knowledge_base_id")
    @Length(min = 1, max = 64)
    private String knowledgeBaseId = null;

    @JsonProperty("image_ids")
    @Valid
    @Size()
    private List<@Length() String> imageIds = null;

    public String getFileId() {
        return fileId;
    }

    public RetrievalResultInfo setFileId(String fileId) {
        this.fileId = fileId;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public RetrievalResultInfo setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getChunkId() {
        return chunkId;
    }

    public RetrievalResultInfo setChunkId(String chunkId) {
        this.chunkId = chunkId;
        return this;
    }

    public String getContent() {
        return content;
    }

    public RetrievalResultInfo setContent(String content) {
        this.content = content;
        return this;
    }

    public Float getSimilarity() {
        return similarity;
    }

    public RetrievalResultInfo setSimilarity(Float similarity) {
        this.similarity = similarity;
        return this;
    }

    public String getKnowledgeBaseId() {
        return knowledgeBaseId;
    }

    public RetrievalResultInfo setKnowledgeBaseId(String knowledgeBaseId) {
        this.knowledgeBaseId = knowledgeBaseId;
        return this;
    }

    public List<String> getImageIds() {
        return imageIds;
    }

    public RetrievalResultInfo setImageIds(List<String> imageIds) {
        this.imageIds = imageIds;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class RetrievalResultInfo {\n");

        sb.append("    fileId: ").append(toIndentedString(fileId)).append("\n");
        sb.append("    title: ").append(toIndentedString(title)).append("\n");
        sb.append("    chunkId: ").append(toIndentedString(chunkId)).append("\n");
        sb.append("    content: ").append(toIndentedString(content)).append("\n");
        sb.append("    similarity: ").append(toIndentedString(similarity)).append("\n");
        sb.append("    knowledgeBaseId: ").append(toIndentedString(knowledgeBaseId)).append("\n");
        sb.append("    imageIds: ").append(toIndentedString(imageIds)).append("\n");
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
        RetrievalResultInfo retrievalResultInfo = (RetrievalResultInfo) o;
        return Objects.equals(this.fileId, retrievalResultInfo.fileId) && Objects.equals(this.title,
            retrievalResultInfo.title) && Objects.equals(this.chunkId, retrievalResultInfo.chunkId) && Objects.equals(
            this.content, retrievalResultInfo.content) && Objects.equals(this.similarity,
            retrievalResultInfo.similarity) && Objects.equals(this.knowledgeBaseId, retrievalResultInfo.knowledgeBaseId)
            && Objects.equals(this.imageIds, retrievalResultInfo.imageIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileId, title, chunkId, content, similarity, knowledgeBaseId, imageIds);
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
