/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.agent.common.annotation.ValidKnowledgeBaseName;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * 修改第三方知识库连接的请求体
 */
@ApiModel(description = "修改第三方知识库连接的请求体")

@Validated

public class UpdateThirdPartyKnowledgeBaseConnectionRequestBody implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("name")
    @ValidKnowledgeBaseName
    @Length(max = 50)
    private String name = null;

    @JsonProperty("description")
    @Length(max = 100)
    private String description = null;

    @JsonProperty("icon")
    private String icon = null;

    @JsonProperty("params")
    @Valid
    @Size()
    private List<ConnectionParamInfo> params = null;

    public String getName() {
        return name;
    }

    public UpdateThirdPartyKnowledgeBaseConnectionRequestBody setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public UpdateThirdPartyKnowledgeBaseConnectionRequestBody setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getIcon() {
        return icon;
    }

    public UpdateThirdPartyKnowledgeBaseConnectionRequestBody setIcon(String icon) {
        this.icon = icon;
        return this;
    }

    public List<ConnectionParamInfo> getParams() {
        return params;
    }

    public UpdateThirdPartyKnowledgeBaseConnectionRequestBody setParams(List<ConnectionParamInfo> params) {
        this.params = params;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class UpdateThirdPartyKnowledgeBaseConnectionRequestBody {\n");

        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    icon: ").append(toIndentedString(icon)).append("\n");
        sb.append("    params: ").append(toIndentedString(params)).append("\n");
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
        UpdateThirdPartyKnowledgeBaseConnectionRequestBody updateThirdPartyKnowledgeBaseConnectionRequestBody
            = (UpdateThirdPartyKnowledgeBaseConnectionRequestBody) o;
        return Objects.equals(this.name, updateThirdPartyKnowledgeBaseConnectionRequestBody.name) && Objects.equals(
            this.description, updateThirdPartyKnowledgeBaseConnectionRequestBody.description) && Objects.equals(
            this.icon, updateThirdPartyKnowledgeBaseConnectionRequestBody.icon) && Objects.equals(this.params,
            updateThirdPartyKnowledgeBaseConnectionRequestBody.params);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, icon, params);
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
