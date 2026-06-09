/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * PluginsMsg
 */

@Validated

public class PluginsMsg implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    @Length(max = 64)
    private String id = null;

    @JsonProperty("name")
    @Length(max = 64)
    private String name = null;

    public String getId() {
        return id;
    }

    public PluginsMsg setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public PluginsMsg setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PluginsMsg {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
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
        PluginsMsg pluginsMsg = (PluginsMsg) o;
        return Objects.equals(this.id, pluginsMsg.id) && Objects.equals(this.name, pluginsMsg.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
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
