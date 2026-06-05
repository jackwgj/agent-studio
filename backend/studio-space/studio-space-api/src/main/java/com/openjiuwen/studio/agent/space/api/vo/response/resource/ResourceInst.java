package com.openjiuwen.studio.agent.space.api.vo.response.resource;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.util.List;

@Data
public class ResourceInst {
    @JsonProperty("resource_id")
    private String resourceId;

    @JsonProperty("parent_id")
    private String parentId;

    @JsonProperty("sku_code")
    private String skuCode;

    @JsonProperty("status")
    private String status;

    @JsonProperty("attr_list")
    private List<LicenseInst> attrList;
}
