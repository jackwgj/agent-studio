/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.agent.node;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.util.Map;
import java.util.Objects;

/**
 * 数据源连接信息
 */
@ApiModel(description = "数据源连接信息")
@Validated
public class DatasourceConnectionInfo {
    @JsonProperty("host")
    @NotBlank
    @Length(max = 64)
    private String host = null;

    @JsonProperty("port")
    @Length(max = 16)
    private String port = null;

    @JsonProperty("ssl_enabled")
    @NotNull
    private Boolean sslEnabled = null;

    @JsonProperty("database_name")
    @NotBlank
    @Length(max = 64)
    private String databaseName = null;

    @JsonProperty("user")
    @NotBlank
    @Length(max = 64)
    private String user = null;

    @JsonProperty("password")
    @NotBlank
    private String password = null;

    @JsonProperty("metadata")
    @Valid
    @Size()
    private Map<String, Object> metadata = null;

    @JsonProperty("sql_version")
    @Length(max = 32)
    private String sqlVersion = null;

    public String getHost() {
        return host;
    }

    public DatasourceConnectionInfo setHost(String host) {
        this.host = host;
        return this;
    }

    public String getPort() {
        return port;
    }

    public DatasourceConnectionInfo setPort(String port) {
        this.port = port;
        return this;
    }

    public DatasourceConnectionInfo setSslEnabled(Boolean sslEnabled) {
        this.sslEnabled = sslEnabled;
        return this;
    }

    public Boolean isSslEnabled() {
        return sslEnabled;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public DatasourceConnectionInfo setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
        return this;
    }

    public String getUser() {
        return user;
    }

    public DatasourceConnectionInfo setUser(String user) {
        this.user = user;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public DatasourceConnectionInfo setPassword(String password) {
        this.password = password;
        return this;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public DatasourceConnectionInfo setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
        return this;
    }

    public String getSqlVersion() {
        return sqlVersion;
    }

    public DatasourceConnectionInfo setSqlVersion(String sqlVersion) {
        this.sqlVersion = sqlVersion;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class DatasourceConnectionInfo {\n");

        sb.append("    host: ").append(toIndentedString(host)).append("\n");
        sb.append("    port: ").append(toIndentedString(port)).append("\n");
        sb.append("    sslEnabled: ").append(toIndentedString(sslEnabled)).append("\n");
        sb.append("    databaseName: ").append(toIndentedString(databaseName)).append("\n");
        sb.append("    user: ").append(toIndentedString(user)).append("\n");
        sb.append("    password: ").append(toIndentedString(password)).append("\n");
        sb.append("    metadata: ").append(toIndentedString(metadata)).append("\n");
        sb.append("    sqlVersion: ").append(toIndentedString(sqlVersion)).append("\n");
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
        DatasourceConnectionInfo datasourceConnectionInfo = (DatasourceConnectionInfo) o;
        return Objects.equals(this.host, datasourceConnectionInfo.host) && Objects.equals(this.port,
            datasourceConnectionInfo.port) && Objects.equals(this.sslEnabled, datasourceConnectionInfo.sslEnabled)
            && Objects.equals(this.databaseName, datasourceConnectionInfo.databaseName) && Objects.equals(this.user,
            datasourceConnectionInfo.user) && Objects.equals(this.password, datasourceConnectionInfo.password)
            && Objects.equals(this.metadata, datasourceConnectionInfo.metadata) && Objects.equals(this.sqlVersion,
            datasourceConnectionInfo.sqlVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port, sslEnabled, databaseName, user, password, metadata, sqlVersion);
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
