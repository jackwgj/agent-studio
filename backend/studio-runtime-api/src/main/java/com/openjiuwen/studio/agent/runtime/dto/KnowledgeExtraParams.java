/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * KnowledgeExtraParams
 */

@Validated

public class KnowledgeExtraParams implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("key")
    @Length(max = 1024)
    private String key = null;

    @JsonProperty("value")
    @Length(max = 1024)
    private String value = null;

    public String getKey() {
        return key;
    }

    public KnowledgeExtraParams setKey(String key) {
        this.key = key;
        return this;
    }

    public String getValue() {
        return value;
    }

    public KnowledgeExtraParams setValue(String value) {
        this.value = value;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class KnowledgeExtraParams {\n");

        sb.append("    key: ").append(toIndentedString(key)).append("\n");
        sb.append("    value: ").append(toIndentedString(value)).append("\n");
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
        KnowledgeExtraParams knowledgeExtraParams = (KnowledgeExtraParams) o;
        return Objects.equals(this.key, knowledgeExtraParams.key) && Objects.equals(this.value,
            knowledgeExtraParams.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
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
