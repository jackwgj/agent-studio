/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * PluginIAMAuthInfo
 */

@Validated

public class PluginIAMAuthInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("iam_url")
    @Length(min = 1, max = 255)
    private String iamUrl = null;

    @JsonProperty("iam_domain")
    @Length(max = 255)
    private String iamDomain = null;

    @JsonProperty("iam_project")
    @Length(max = 255)
    private String iamProject = null;

    @JsonProperty("iam_user")
    @Length(max = 255)
    private String iamUser = null;

    @JsonProperty("iam_password")
    @Length(max = 255)
    private String iamPassword = null;

    @JsonProperty("iam_ak")
    @Length(max = 255)
    private String iamAk = null;

    @JsonProperty("iam_sk")
    @Length(max = 255)
    private String iamSk = null;

    public String getIamUrl() {
        return iamUrl;
    }

    public PluginIAMAuthInfo setIamUrl(String iamUrl) {
        this.iamUrl = iamUrl;
        return this;
    }

    public String getIamDomain() {
        return iamDomain;
    }

    public PluginIAMAuthInfo setIamDomain(String iamDomain) {
        this.iamDomain = iamDomain;
        return this;
    }

    public String getIamProject() {
        return iamProject;
    }

    public PluginIAMAuthInfo setIamProject(String iamProject) {
        this.iamProject = iamProject;
        return this;
    }

    public String getIamUser() {
        return iamUser;
    }

    public PluginIAMAuthInfo setIamUser(String iamUser) {
        this.iamUser = iamUser;
        return this;
    }

    public String getIamPassword() {
        return iamPassword;
    }

    public PluginIAMAuthInfo setIamPassword(String iamPassword) {
        this.iamPassword = iamPassword;
        return this;
    }

    public String getIamAk() {
        return iamAk;
    }

    public PluginIAMAuthInfo setIamAk(String iamAk) {
        this.iamAk = iamAk;
        return this;
    }

    public String getIamSk() {
        return iamSk;
    }

    public PluginIAMAuthInfo setIamSk(String iamSk) {
        this.iamSk = iamSk;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PluginIAMAuthInfo {\n");

        sb.append("    iamUrl: ").append(toIndentedString(iamUrl)).append("\n");
        sb.append("    iamDomain: ").append(toIndentedString(iamDomain)).append("\n");
        sb.append("    iamProject: ").append(toIndentedString(iamProject)).append("\n");
        sb.append("    iamUser: ").append(toIndentedString(iamUser)).append("\n");
        sb.append("    iamPassword: ").append(toIndentedString(iamPassword)).append("\n");
        sb.append("    iamAk: ").append(toIndentedString(iamAk)).append("\n");
        sb.append("    iamSk: ").append(toIndentedString(iamSk)).append("\n");
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
        PluginIAMAuthInfo pluginIAMAuthInfo = (PluginIAMAuthInfo) o;
        return Objects.equals(this.iamUrl, pluginIAMAuthInfo.iamUrl) && Objects.equals(this.iamDomain,
            pluginIAMAuthInfo.iamDomain) && Objects.equals(this.iamProject, pluginIAMAuthInfo.iamProject)
            && Objects.equals(this.iamUser, pluginIAMAuthInfo.iamUser) && Objects.equals(this.iamPassword,
            pluginIAMAuthInfo.iamPassword) && Objects.equals(this.iamAk, pluginIAMAuthInfo.iamAk) && Objects.equals(
            this.iamSk, pluginIAMAuthInfo.iamSk);
    }

    @Override
    public int hashCode() {
        return Objects.hash(iamUrl, iamDomain, iamProject, iamUser, iamPassword, iamAk, iamSk);
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
