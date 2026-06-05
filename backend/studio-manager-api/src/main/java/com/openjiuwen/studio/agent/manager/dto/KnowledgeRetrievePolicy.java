/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.constraints.Range;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * 知识检索策略。
 */
@ApiModel(description = "知识检索策略。")

@Validated

public class KnowledgeRetrievePolicy implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("search_mode")
    private SearchModeEnum searchMode = SearchModeEnum.DOC;

    @JsonProperty("top_k")
    @Range(min = 1L, max = 50L)
    private Integer topK = 5;

    @JsonProperty("recall_threshold")
    private Float recallThreshold = 0.5f;

    @JsonProperty("faq_threshold")
    private Float faqThreshold = 0.9f;

    @JsonProperty("need_extras_faq_search")
    private Boolean needExtrasFaqSearch = false;

    @JsonProperty("show_source")
    private Boolean showSource = null;

    @JsonProperty("retrieve_image")
    private Boolean retrieveImage = false;

    @JsonProperty("extra_params")
    @Valid
    @Size(max = 50)
    private List<ExtraParamsInfo> extraParams = null;

    public SearchModeEnum getSearchMode() {
        return searchMode;
    }

    public KnowledgeRetrievePolicy setSearchMode(SearchModeEnum searchMode) {
        this.searchMode = searchMode;
        return this;
    }

    public Integer getTopK() {
        return topK;
    }

    public KnowledgeRetrievePolicy setTopK(Integer topK) {
        this.topK = topK;
        return this;
    }

    public Float getRecallThreshold() {
        return recallThreshold;
    }

    public KnowledgeRetrievePolicy setRecallThreshold(Float recallThreshold) {
        this.recallThreshold = recallThreshold;
        return this;
    }

    public Float getFaqThreshold() {
        return faqThreshold;
    }

    public KnowledgeRetrievePolicy setFaqThreshold(Float faqThreshold) {
        this.faqThreshold = faqThreshold;
        return this;
    }

    public KnowledgeRetrievePolicy setNeedExtrasFaqSearch(Boolean needExtrasFaqSearch) {
        this.needExtrasFaqSearch = needExtrasFaqSearch;
        return this;
    }

    public Boolean isNeedExtrasFaqSearch() {
        return needExtrasFaqSearch;
    }

    public KnowledgeRetrievePolicy setShowSource(Boolean showSource) {
        this.showSource = showSource;
        return this;
    }

    public Boolean isShowSource() {
        return showSource;
    }

    public KnowledgeRetrievePolicy setRetrieveImage(Boolean retrieveImage) {
        this.retrieveImage = retrieveImage;
        return this;
    }

    public Boolean isRetrieveImage() {
        return retrieveImage;
    }

    public List<ExtraParamsInfo> getExtraParams() {
        return extraParams;
    }

    public KnowledgeRetrievePolicy setExtraParams(List<ExtraParamsInfo> extraParams) {
        this.extraParams = extraParams;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class KnowledgeRetrievePolicy {\n");

        sb.append("    searchMode: ").append(toIndentedString(searchMode)).append("\n");
        sb.append("    topK: ").append(toIndentedString(topK)).append("\n");
        sb.append("    recallThreshold: ").append(toIndentedString(recallThreshold)).append("\n");
        sb.append("    faqThreshold: ").append(toIndentedString(faqThreshold)).append("\n");
        sb.append("    needExtrasFaqSearch: ").append(toIndentedString(needExtrasFaqSearch)).append("\n");
        sb.append("    showSource: ").append(toIndentedString(showSource)).append("\n");
        sb.append("    retrieveImage: ").append(toIndentedString(retrieveImage)).append("\n");
        sb.append("    extraParams: ").append(toIndentedString(extraParams)).append("\n");
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
        KnowledgeRetrievePolicy knowledgeRetrievePolicy = (KnowledgeRetrievePolicy) o;
        return Objects.equals(this.searchMode, knowledgeRetrievePolicy.searchMode) && Objects.equals(this.topK,
            knowledgeRetrievePolicy.topK) && Objects.equals(this.recallThreshold,
            knowledgeRetrievePolicy.recallThreshold) && Objects.equals(this.faqThreshold,
            knowledgeRetrievePolicy.faqThreshold) && Objects.equals(this.needExtrasFaqSearch,
            knowledgeRetrievePolicy.needExtrasFaqSearch) && Objects.equals(this.showSource,
            knowledgeRetrievePolicy.showSource) && Objects.equals(this.retrieveImage,
            knowledgeRetrievePolicy.retrieveImage) && Objects.equals(this.extraParams,
            knowledgeRetrievePolicy.extraParams);
    }

    @Override
    public int hashCode() {
        return Objects.hash(searchMode, topK, recallThreshold, faqThreshold, needExtrasFaqSearch, showSource,
            retrieveImage, extraParams);
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

    /**
     * 知识库检索模式，对应三种取值： - doc：语义检索，使用向量检索技术 - keyword：关键词检索，使用倒排检索技术 - mix：混合检索，使用向量检索和关键词检索混合检索
     */
    public enum SearchModeEnum {
        DOC("doc"),

        KEYWORD("keyword"),

        MIX("mix");

        private String value;

        SearchModeEnum(String value) {
            this.value = value;
        }

        @JsonCreator
        public static SearchModeEnum fromValue(String text) {
            for (SearchModeEnum b : SearchModeEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }
    }
}
