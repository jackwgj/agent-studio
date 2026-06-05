/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * domain info
 */
@ApiModel(description = "domain info")

@Validated

public class IamTokenInfoTokenUserDomain implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private String id = null;

    @JsonProperty("name")
    private String name = null;

    public String getId() {
        return id;
    }

    public IamTokenInfoTokenUserDomain setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public IamTokenInfoTokenUserDomain setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class IamTokenInfoTokenUserDomain {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
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
        IamTokenInfoTokenUserDomain iamTokenInfoTokenUserDomain = (IamTokenInfoTokenUserDomain) o;
        return Objects.equals(this.id, iamTokenInfoTokenUserDomain.id) && Objects.equals(this.name,
            iamTokenInfoTokenUserDomain.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
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
