/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * user info
 */
@ApiModel(description = "user info")

@Validated

public class IamTokenInfoTokenUser implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private String id = null;

    @JsonProperty("name")
    private String name = null;

    @JsonProperty("domain")
    @Valid
    private IamTokenInfoTokenUserDomain domain = null;

    public String getId() {
        return id;
    }

    public IamTokenInfoTokenUser setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public IamTokenInfoTokenUser setName(String name) {
        this.name = name;
        return this;
    }

    public IamTokenInfoTokenUserDomain getDomain() {
        return domain;
    }

    public IamTokenInfoTokenUser setDomain(IamTokenInfoTokenUserDomain domain) {
        this.domain = domain;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class IamTokenInfoTokenUser {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    domain: ").append(toIndentedString(domain)).append("\n");
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
        IamTokenInfoTokenUser iamTokenInfoTokenUser = (IamTokenInfoTokenUser) o;
        return Objects.equals(this.id, iamTokenInfoTokenUser.id) && Objects.equals(this.name,
            iamTokenInfoTokenUser.name) && Objects.equals(this.domain, iamTokenInfoTokenUser.domain);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, domain);
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
