/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 知识检索结果片段信息
 */
@Data
@Accessors(chain = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class RagResultReference implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("knowledge_base_id")
    private String knowledgeBaseId = null;

    @JsonProperty("knowledge_base_type")
    private String knowledgeBaseType = null;

    @JsonProperty("file_id")
    private String fileId = null;

    @JsonProperty("content")
    private String content = null;

    @JsonProperty("document_name")
    private String documentName = null;

    @JsonProperty("subtitle")
    private String subtitle = null;

    @JsonProperty("score")
    private Float score = null;

    @JsonProperty("serial_number")
    private Long serialNumber = null;

    @JsonProperty("retrieval_id")
    private String retrievalId = null;

    @JsonProperty("type")
    private TypeEnum type = null;

    public enum TypeEnum {
        DOC("doc"),

        FAQ("faq");

        private String value;

        TypeEnum(String value) {
            this.value = value;
        }

        @JsonCreator
        public static TypeEnum fromValue(String text) {
            for (TypeEnum b : TypeEnum.values()) {
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
