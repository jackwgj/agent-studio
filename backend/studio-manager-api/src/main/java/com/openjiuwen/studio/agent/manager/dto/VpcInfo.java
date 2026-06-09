/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * vpc信息
 */
@ApiModel(description = "vpc信息")

@Validated

public class VpcInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private String id = null;

    @JsonProperty("name")
    private String name = null;

    @JsonProperty("description")
    private String description = null;

    @JsonProperty("cidr")
    private String cidr = null;

    @JsonProperty("status")
    private String status = null;

    public String getId() {
        return id;
    }

    public VpcInfo setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public VpcInfo setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public VpcInfo setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getCidr() {
        return cidr;
    }

    public VpcInfo setCidr(String cidr) {
        this.cidr = cidr;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public VpcInfo setStatus(String status) {
        this.status = status;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class VpcInfo {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    cidr: ").append(toIndentedString(cidr)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
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
        VpcInfo vpcInfo = (VpcInfo) o;
        return Objects.equals(this.id, vpcInfo.id) && Objects.equals(this.name, vpcInfo.name) && Objects.equals(
            this.description, vpcInfo.description) && Objects.equals(this.cidr, vpcInfo.cidr) && Objects.equals(
            this.status, vpcInfo.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, cidr, status);
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
