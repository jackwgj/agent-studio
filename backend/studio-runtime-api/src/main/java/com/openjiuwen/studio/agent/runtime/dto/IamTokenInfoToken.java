/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * IamTokenInfoToken
 */

@Validated

public class IamTokenInfoToken implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("user")
    @Valid
    private IamTokenInfoTokenUser user = null;

    @JsonProperty("project")
    @Valid
    private IamTokenInfoTokenProject project = null;

    @JsonProperty("expires_at")
    private String expiresAt = null;

    public IamTokenInfoTokenUser getUser() {
        return user;
    }

    public IamTokenInfoToken setUser(IamTokenInfoTokenUser user) {
        this.user = user;
        return this;
    }

    public IamTokenInfoTokenProject getProject() {
        return project;
    }

    public IamTokenInfoToken setProject(IamTokenInfoTokenProject project) {
        this.project = project;
        return this;
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public IamTokenInfoToken setExpiresAt(String expiresAt) {
        this.expiresAt = expiresAt;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class IamTokenInfoToken {\n");

        sb.append("    user: ").append(toIndentedString(user)).append("\n");
        sb.append("    project: ").append(toIndentedString(project)).append("\n");
        sb.append("    expiresAt: ").append(toIndentedString(expiresAt)).append("\n");
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
        IamTokenInfoToken iamTokenInfoToken = (IamTokenInfoToken) o;
        return Objects.equals(this.user, iamTokenInfoToken.user) && Objects.equals(this.project,
            iamTokenInfoToken.project) && Objects.equals(this.expiresAt, iamTokenInfoToken.expiresAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, project, expiresAt);
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
