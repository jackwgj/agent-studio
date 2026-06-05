/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 手动创建模板请求体
 */
@ApiModel(description = "手动创建模板请求体")

@Validated
public class ManualPePromptTemplateDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    @Pattern(regexp = "(^$)|^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")
    private String id = null;

    @JsonProperty("name")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5a-zA-Z][\\u4e00-\\u9fa5\\w-]{0,18}[\\u4e00-\\u9fa5a-zA-Z0-9]$")
    @NotBlank
    private String name = null;

    @JsonProperty("content")
    @Length(max = 10000)
    private String content = null;

    @JsonProperty("source")
    @Pattern(regexp = "PLAYGROUND|EXPERIMENT|CANDIDATE|HORIZONTAL|NO_SOURCE|PRESET")
    private String source = null;

    @JsonProperty("variables")
    @Length(max = 255)
    private String variables = null;

    @JsonProperty("tag_list")
    @Valid
    @NotNull
    @Size(max = 500)
    private List<@Length() String> tagList = new ArrayList<String>();

    @JsonProperty("description")
    @Length(max = 100)
    private String description = null;

    @JsonProperty("industry_id")
    @Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")
    @NotBlank
    private String industryId = null;

    public String getId() {
        return id;
    }

    public ManualPePromptTemplateDto setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public ManualPePromptTemplateDto setName(String name) {
        this.name = name;
        return this;
    }

    public String getContent() {
        return content;
    }

    public ManualPePromptTemplateDto setContent(String content) {
        this.content = content;
        return this;
    }

    public String getSource() {
        return source;
    }

    public ManualPePromptTemplateDto setSource(String source) {
        this.source = source;
        return this;
    }

    public String getVariables() {
        return variables;
    }

    public ManualPePromptTemplateDto setVariables(String variables) {
        this.variables = variables;
        return this;
    }

    public List<String> getTagList() {
        return tagList;
    }

    public ManualPePromptTemplateDto setTagList(List<String> tagList) {
        this.tagList = tagList;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ManualPePromptTemplateDto setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getIndustryId() {
        return industryId;
    }

    public ManualPePromptTemplateDto setIndustryId(String industryId) {
        this.industryId = industryId;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ManualPePromptTemplateDto {\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    content: ").append(toIndentedString(content)).append("\n");
        sb.append("    source: ").append(toIndentedString(source)).append("\n");
        sb.append("    variables: ").append(toIndentedString(variables)).append("\n");
        sb.append("    tagList: ").append(toIndentedString(tagList)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    industryId: ").append(toIndentedString(industryId)).append("\n");
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
        ManualPePromptTemplateDto manualPePromptTemplateDto = (ManualPePromptTemplateDto) o;
        return Objects.equals(this.id, manualPePromptTemplateDto.id) && Objects.equals(this.name,
            manualPePromptTemplateDto.name) && Objects.equals(this.content, manualPePromptTemplateDto.content)
            && Objects.equals(this.source, manualPePromptTemplateDto.source) && Objects.equals(this.variables,
            manualPePromptTemplateDto.variables) && Objects.equals(this.tagList, manualPePromptTemplateDto.tagList)
            && Objects.equals(this.description, manualPePromptTemplateDto.description) && Objects.equals(
            this.industryId, manualPePromptTemplateDto.industryId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, content, source, variables, tagList, description, industryId);
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
