/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
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

public class CommonWorkspaceReq implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    @Pattern(regexp = "^[a-zA-Z0-9_()\\-]+$")
    @Length(min = 1, max = 64)
    private String id = null;

    @JsonProperty("name")
    @Length(min = 1, max = 64)
    private String name = null;

    @JsonProperty("icon")
    private String icon = null;

    @JsonProperty("description")
    private String description = null;

    @JsonProperty("type")
    private String type = null;

    public String getId() {
        return id;
    }

    public CommonWorkspaceReq setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public CommonWorkspaceReq setName(String name) {
        this.name = name;
        return this;
    }

    public String getIcon() {
        return icon;
    }

    public CommonWorkspaceReq setIcon(String icon) {
        this.icon = icon;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public CommonWorkspaceReq setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getType() {
        return type;
    }

    public CommonWorkspaceReq setType(String type) {
        this.type = type;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class CommonWorkspaceReq {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    icon: ").append(toIndentedString(icon)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
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
        CommonWorkspaceReq commonWorkspaceReq = (CommonWorkspaceReq) o;
        return Objects.equals(this.id, commonWorkspaceReq.id) && Objects.equals(this.name, commonWorkspaceReq.name)
            && Objects.equals(this.icon, commonWorkspaceReq.icon) && Objects.equals(this.description,
            commonWorkspaceReq.description) && Objects.equals(this.type, commonWorkspaceReq.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, icon, description, type);
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
