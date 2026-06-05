/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * 删除结构化信息请求
 */
@ApiModel(description = "删除结构化信息请求")

@Validated

public class StructuredInfoRequestDelete implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$")
    @NotBlank
    @Length(max = 64)
    private String id = null;

    @JsonProperty("name")
    @Length(min = 2, max = 64)
    private String name = null;

    @JsonProperty("category")
    @Length(max = 64)
    private String category = null;

    @JsonProperty("content")
    @Valid
    @Size(max = 10)
    private Map<String, Object> content = null;

    @JsonProperty("visibility")
    private VisibilityEnum visibility = null;

    public String getId() {
        return id;
    }

    public StructuredInfoRequestDelete setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public StructuredInfoRequestDelete setName(String name) {
        this.name = name;
        return this;
    }

    public String getCategory() {
        return category;
    }

    public StructuredInfoRequestDelete setCategory(String category) {
        this.category = category;
        return this;
    }

    public Map<String, Object> getContent() {
        return content;
    }

    public StructuredInfoRequestDelete setContent(Map<String, Object> content) {
        this.content = content;
        return this;
    }

    public VisibilityEnum getVisibility() {
        return visibility;
    }

    public StructuredInfoRequestDelete setVisibility(VisibilityEnum visibility) {
        this.visibility = visibility;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class StructuredInfoRequestDelete {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    category: ").append(toIndentedString(category)).append("\n");
        sb.append("    content: ").append(toIndentedString(content)).append("\n");
        sb.append("    visibility: ").append(toIndentedString(visibility)).append("\n");
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
        StructuredInfoRequestDelete structuredInfoRequestDelete = (StructuredInfoRequestDelete) o;
        return Objects.equals(this.id, structuredInfoRequestDelete.id) && Objects.equals(this.name,
            structuredInfoRequestDelete.name) && Objects.equals(this.category, structuredInfoRequestDelete.category)
            && Objects.equals(this.content, structuredInfoRequestDelete.content) && Objects.equals(this.visibility,
            structuredInfoRequestDelete.visibility);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, category, content, visibility);
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
     * 结构体可见范围（可选）
     */
    public enum VisibilityEnum {
        PERSONAL("personal"),

        TENANT("tenant"),

        WORKSPACE("workspace");

        private String value;

        VisibilityEnum(String value) {
            this.value = value;
        }

        @JsonCreator
        public static VisibilityEnum fromValue(String text) {
            for (VisibilityEnum b : VisibilityEnum.values()) {
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
