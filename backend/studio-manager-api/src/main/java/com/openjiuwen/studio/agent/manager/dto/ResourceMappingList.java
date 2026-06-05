/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * 绑定关系列表。
 */
@ApiModel(description = "绑定关系列表。")

@Validated

public class ResourceMappingList implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("count")
    private Long count = null;

    @JsonProperty("resource_list")
    @Valid
    @Size()
    private List<ResourceMapping> resourceList = null;

    public Long getCount() {
        return count;
    }

    public ResourceMappingList setCount(Long count) {
        this.count = count;
        return this;
    }

    public List<ResourceMapping> getResourceList() {
        return resourceList;
    }

    public ResourceMappingList setResourceList(List<ResourceMapping> resourceList) {
        this.resourceList = resourceList;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ResourceMappingList {\n");

        sb.append("    count: ").append(toIndentedString(count)).append("\n");
        sb.append("    resourceList: ").append(toIndentedString(resourceList)).append("\n");
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
        ResourceMappingList resourceMappingList = (ResourceMappingList) o;
        return Objects.equals(this.count, resourceMappingList.count) && Objects.equals(this.resourceList,
            resourceMappingList.resourceList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(count, resourceList);
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
