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
 * HisSgov
 */

@Validated

public class HisSgov implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("app_id")
    @Length(min = 1, max = 255)
    private String appId = null;

    @JsonProperty("credential")
    @Length(min = 1)
    private String credential = null;

    @JsonProperty("sgov_url")
    private String sgovUrl = null;

    public String getAppId() {
        return appId;
    }

    public HisSgov setAppId(String appId) {
        this.appId = appId;
        return this;
    }

    public String getCredential() {
        return credential;
    }

    public HisSgov setCredential(String credential) {
        this.credential = credential;
        return this;
    }

    public String getSgovUrl() {
        return sgovUrl;
    }

    public HisSgov setSgovUrl(String sgovUrl) {
        this.sgovUrl = sgovUrl;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class HisSgov {\n");

        sb.append("    appId: ").append(toIndentedString(appId)).append("\n");
        sb.append("    credential: ").append(toIndentedString(credential)).append("\n");
        sb.append("    sgovUrl: ").append(toIndentedString(sgovUrl)).append("\n");
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
        HisSgov hisSgov = (HisSgov) o;
        return Objects.equals(this.appId, hisSgov.appId) && Objects.equals(this.credential, hisSgov.credential)
            && Objects.equals(this.sgovUrl, hisSgov.sgovUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appId, credential, sgovUrl);
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
