/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * SwaggerMeta
 */

@Validated

public class SwaggerMeta implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("root_key")
    @Length(max = 64)
    private String rootKey = null;

    @JsonProperty("origin_list_meta")
    @Valid
    @Size()
    private List<Object> originListMeta = null;

    @JsonProperty("origin_map_meta")
    @Valid
    @Size()
    private Map<String, Object> originMapMeta = null;

    @JsonProperty("origin_xml_meta")
    private String originXmlMeta = null;

    public String getRootKey() {
        return rootKey;
    }

    public SwaggerMeta setRootKey(String rootKey) {
        this.rootKey = rootKey;
        return this;
    }

    public List<Object> getOriginListMeta() {
        return originListMeta;
    }

    public SwaggerMeta setOriginListMeta(List<Object> originListMeta) {
        this.originListMeta = originListMeta;
        return this;
    }

    public Map<String, Object> getOriginMapMeta() {
        return originMapMeta;
    }

    public SwaggerMeta setOriginMapMeta(Map<String, Object> originMapMeta) {
        this.originMapMeta = originMapMeta;
        return this;
    }

    public String getOriginXmlMeta() {
        return originXmlMeta;
    }

    public SwaggerMeta setOriginXmlMeta(String originXmlMeta) {
        this.originXmlMeta = originXmlMeta;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class SwaggerMeta {\n");

        sb.append("    rootKey: ").append(toIndentedString(rootKey)).append("\n");
        sb.append("    originListMeta: ").append(toIndentedString(originListMeta)).append("\n");
        sb.append("    originMapMeta: ").append(toIndentedString(originMapMeta)).append("\n");
        sb.append("    originXmlMeta: ").append(toIndentedString(originXmlMeta)).append("\n");
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
        SwaggerMeta swaggerMeta = (SwaggerMeta) o;
        return Objects.equals(this.rootKey, swaggerMeta.rootKey) && Objects.equals(this.originListMeta,
            swaggerMeta.originListMeta) && Objects.equals(this.originMapMeta, swaggerMeta.originMapMeta)
            && Objects.equals(this.originXmlMeta, swaggerMeta.originXmlMeta);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rootKey, originListMeta, originMapMeta, originXmlMeta);
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
