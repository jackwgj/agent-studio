/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.agent.common.annotation.ValidKnowledgeBaseName;
import com.openjiuwen.studio.agent.common.dto.knowledge.ParseConf;
import com.openjiuwen.studio.agent.common.dto.knowledge.SplitConf;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * 创建知识库请求体
 */
@ApiModel(description = "创建知识库请求体")

@Validated

public class CreateKnowledgeRepoRequestBody implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("display_name")
    @ValidKnowledgeBaseName
    @NotBlank
    @Length(min = 1, max = 50)
    private String displayName = null;

    @JsonProperty("description")
    @NotBlank
    @Length(max = 100)
    private String description = null;

    @JsonProperty("embedding_model")
    @Valid
    private ModelConf embeddingModel = null;

    @JsonProperty("rerank_model")
    @Valid
    private ModelConf rerankModel = null;

    @JsonProperty("parse_conf")
    @Valid
    private ParseConf parseConf = null;

    @JsonProperty("split_conf")
    @Valid
    private SplitConf splitConf = null;

    @JsonProperty("icon")
    @Length(max = 1024000)
    private String icon = null;

    @JsonProperty("rag_chunk_parser_conf")
    @Valid
    private RagChunkParserConf ragChunkParserConf = null;

    public String getDisplayName() {
        return displayName;
    }

    public CreateKnowledgeRepoRequestBody setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public CreateKnowledgeRepoRequestBody setDescription(String description) {
        this.description = description;
        return this;
    }

    public ModelConf getEmbeddingModel() {
        return embeddingModel;
    }

    public CreateKnowledgeRepoRequestBody setEmbeddingModel(ModelConf embeddingModel) {
        this.embeddingModel = embeddingModel;
        return this;
    }

    public ModelConf getRerankModel() {
        return rerankModel;
    }

    public CreateKnowledgeRepoRequestBody setRerankModel(ModelConf rerankModel) {
        this.rerankModel = rerankModel;
        return this;
    }

    public ParseConf getParseConf() {
        return parseConf;
    }

    public CreateKnowledgeRepoRequestBody setParseConf(ParseConf parseConf) {
        this.parseConf = parseConf;
        return this;
    }

    public SplitConf getSplitConf() {
        return splitConf;
    }

    public CreateKnowledgeRepoRequestBody setSplitConf(SplitConf splitConf) {
        this.splitConf = splitConf;
        return this;
    }

    public String getIcon() {
        return icon;
    }

    public CreateKnowledgeRepoRequestBody setIcon(String icon) {
        this.icon = icon;
        return this;
    }

    public RagChunkParserConf getRagChunkParserConf() {
        return ragChunkParserConf;
    }

    public CreateKnowledgeRepoRequestBody setRagChunkParserConf(RagChunkParserConf ragChunkParserConf) {
        this.ragChunkParserConf = ragChunkParserConf;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class CreateKnowledgeRepoRequestBody {\n");

        sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    embeddingModel: ").append(toIndentedString(embeddingModel)).append("\n");
        sb.append("    rerankModel: ").append(toIndentedString(rerankModel)).append("\n");
        sb.append("    parseConf: ").append(toIndentedString(parseConf)).append("\n");
        sb.append("    splitConf: ").append(toIndentedString(splitConf)).append("\n");
        sb.append("    icon: ").append(toIndentedString(icon)).append("\n");
        sb.append("    ragChunkParserConf: ").append(toIndentedString(ragChunkParserConf)).append("\n");
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
        CreateKnowledgeRepoRequestBody createKnowledgeRepoRequestBody = (CreateKnowledgeRepoRequestBody) o;
        return Objects.equals(this.displayName, createKnowledgeRepoRequestBody.displayName) && Objects.equals(
            this.description, createKnowledgeRepoRequestBody.description) && Objects.equals(this.embeddingModel,
            createKnowledgeRepoRequestBody.embeddingModel) && Objects.equals(this.rerankModel,
            createKnowledgeRepoRequestBody.rerankModel) && Objects.equals(this.parseConf,
            createKnowledgeRepoRequestBody.parseConf) && Objects.equals(this.splitConf,
            createKnowledgeRepoRequestBody.splitConf) && Objects.equals(this.icon, createKnowledgeRepoRequestBody.icon)
            && Objects.equals(this.ragChunkParserConf, createKnowledgeRepoRequestBody.ragChunkParserConf);
    }

    @Override
    public int hashCode() {
        return Objects.hash(displayName, description, embeddingModel, rerankModel, parseConf, splitConf, icon,
            ragChunkParserConf);
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
