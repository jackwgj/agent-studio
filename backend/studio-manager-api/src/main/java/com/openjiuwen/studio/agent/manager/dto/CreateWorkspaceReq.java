/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * 创建工作空间请求体。
 */
@ApiModel(description = "创建工作空间请求体。")

@Validated

public class CreateWorkspaceReq implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    @Pattern(regexp = "^[a-zA-Z0-9_()\\-]+$")
    @Length(min = 1, max = 64)
    private String id = null;

    @JsonProperty("name")
    @Pattern(
        regexp = "^[\\\\u4e00-\\\\u9fa5a-zA-Z0-9_\\\\-（）()！!](?:[\\\\u4e00-\\\\u9fa5a-zA-Z0-9_\\\\-（）()！! ]*[\\\\u4e00-\\\\u9fa5a-zA-Z0-9_\\\\-（）()！!])?$")
    @NotBlank
    @Length(min = 1, max = 64)
    private String name = null;

    @JsonProperty("icon")
    private String icon = null;

    @JsonProperty("description")
    @Length(max = 256)
    private String description = null;

    @JsonProperty("type")
    private String type = null;

    @JsonProperty("externalMappingInfo")
    @Valid
    private ExternalMappingInfo externalMappingInfo = null;

    public String getId() {
        return id;
    }

    public CreateWorkspaceReq setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public CreateWorkspaceReq setName(String name) {
        this.name = name;
        return this;
    }

    public String getIcon() {
        return icon;
    }

    public CreateWorkspaceReq setIcon(String icon) {
        this.icon = icon;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public CreateWorkspaceReq setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getType() {
        return type;
    }

    public CreateWorkspaceReq setType(String type) {
        this.type = type;
        return this;
    }

    public ExternalMappingInfo getExternalMappingInfo() {
        return externalMappingInfo;
    }

    public CreateWorkspaceReq setExternalMappingInfo(ExternalMappingInfo externalMappingInfo) {
        this.externalMappingInfo = externalMappingInfo;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class CreateWorkspaceReq {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    icon: ").append(toIndentedString(icon)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    externalMappingInfo: ").append(toIndentedString(externalMappingInfo)).append("\n");
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
        CreateWorkspaceReq createWorkspaceReq = (CreateWorkspaceReq) o;
        return Objects.equals(this.id, createWorkspaceReq.id) && Objects.equals(this.name, createWorkspaceReq.name)
            && Objects.equals(this.icon, createWorkspaceReq.icon) && Objects.equals(this.description,
            createWorkspaceReq.description) && Objects.equals(this.type, createWorkspaceReq.type) && Objects.equals(
            this.externalMappingInfo, createWorkspaceReq.externalMappingInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, icon, description, type, externalMappingInfo);
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
