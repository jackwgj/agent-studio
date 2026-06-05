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
 * HisIamInfo
 */

@Validated

public class HisIamInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("iam_url")
    private String iamUrl = null;

    @JsonProperty("iam_account")
    @Length(min = 1, max = 255)
    private String iamAccount = null;

    @JsonProperty("iam_project")
    @Length(min = 1, max = 255)
    private String iamProject = null;

    @JsonProperty("iam_secret")
    @Length(min = 1)
    private String iamSecret = null;

    @JsonProperty("iam_enterprise")
    @Length(min = 1, max = 255)
    private String iamEnterprise = null;

    public String getIamUrl() {
        return iamUrl;
    }

    public HisIamInfo setIamUrl(String iamUrl) {
        this.iamUrl = iamUrl;
        return this;
    }

    public String getIamAccount() {
        return iamAccount;
    }

    public HisIamInfo setIamAccount(String iamAccount) {
        this.iamAccount = iamAccount;
        return this;
    }

    public String getIamProject() {
        return iamProject;
    }

    public HisIamInfo setIamProject(String iamProject) {
        this.iamProject = iamProject;
        return this;
    }

    public String getIamSecret() {
        return iamSecret;
    }

    public HisIamInfo setIamSecret(String iamSecret) {
        this.iamSecret = iamSecret;
        return this;
    }

    public String getIamEnterprise() {
        return iamEnterprise;
    }

    public HisIamInfo setIamEnterprise(String iamEnterprise) {
        this.iamEnterprise = iamEnterprise;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class HisIamInfo {\n");

        sb.append("    iamUrl: ").append(toIndentedString(iamUrl)).append("\n");
        sb.append("    iamAccount: ").append(toIndentedString(iamAccount)).append("\n");
        sb.append("    iamProject: ").append(toIndentedString(iamProject)).append("\n");
        sb.append("    iamSecret: ").append(toIndentedString(iamSecret)).append("\n");
        sb.append("    iamEnterprise: ").append(toIndentedString(iamEnterprise)).append("\n");
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
        HisIamInfo hisIamInfo = (HisIamInfo) o;
        return Objects.equals(this.iamUrl, hisIamInfo.iamUrl) && Objects.equals(this.iamAccount, hisIamInfo.iamAccount)
            && Objects.equals(this.iamProject, hisIamInfo.iamProject) && Objects.equals(this.iamSecret,
            hisIamInfo.iamSecret) && Objects.equals(this.iamEnterprise, hisIamInfo.iamEnterprise);
    }

    @Override
    public int hashCode() {
        return Objects.hash(iamUrl, iamAccount, iamProject, iamSecret, iamEnterprise);
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
