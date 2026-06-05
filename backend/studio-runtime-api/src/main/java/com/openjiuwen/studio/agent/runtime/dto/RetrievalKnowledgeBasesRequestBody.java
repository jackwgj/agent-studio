/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;

import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class RetrievalKnowledgeBasesRequestBody {

    private String query;

    private List<KnowledgeIdTags> knowledgeBases;

    private String scope;

    private Integer topK;

    private Float recallThreshold;

    private Float faqThreshold;

    private SearchModeEnum searchMode;

    private String type;

    private String source;

    private boolean needExtrasFaqSearch;

    private boolean retrieveImage;

    public enum SearchModeEnum {
        DOC("doc"),
        KEYWORD("keyword"),
        MIX("mix"),
        FAQ("faq");

        private String value;

        SearchModeEnum(String value) {
            this.value = value;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
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
    }
}