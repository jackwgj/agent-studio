/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * OauthInfo
 */

@Validated

public class OauthInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("endpoint_url")
    private String endpointUrl = null;

    @JsonProperty("client_id")
    private String clientId = null;

    @JsonProperty("client_secret")
    private String clientSecret = null;

    @JsonProperty("scope")
    private String scope = null;

    public String getEndpointUrl() {
        return endpointUrl;
    }

    public OauthInfo setEndpointUrl(String endpointUrl) {
        this.endpointUrl = endpointUrl;
        return this;
    }

    public String getClientId() {
        return clientId;
    }

    public OauthInfo setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public OauthInfo setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }

    public String getScope() {
        return scope;
    }

    public OauthInfo setScope(String scope) {
        this.scope = scope;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class OauthInfo {\n");

        sb.append("    endpointUrl: ").append(toIndentedString(endpointUrl)).append("\n");
        sb.append("    clientId: ").append(toIndentedString(clientId)).append("\n");
        sb.append("    clientSecret: ").append(toIndentedString(clientSecret)).append("\n");
        sb.append("    scope: ").append(toIndentedString(scope)).append("\n");
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
        OauthInfo oauthInfo = (OauthInfo) o;
        return Objects.equals(this.endpointUrl, oauthInfo.endpointUrl) && Objects.equals(this.clientId,
            oauthInfo.clientId) && Objects.equals(this.clientSecret, oauthInfo.clientSecret) && Objects.equals(
            this.scope, oauthInfo.scope);
    }

    @Override
    public int hashCode() {
        return Objects.hash(endpointUrl, clientId, clientSecret, scope);
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
