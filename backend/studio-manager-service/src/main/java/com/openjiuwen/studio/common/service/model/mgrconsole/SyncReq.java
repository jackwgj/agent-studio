/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.common.service.model.mgrconsole;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.common.service.enums.ResourceStatus;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SyncReq {

    @JsonProperty("resource_id")
    @Size(max = 100)
    private String resourceId;

    @JsonProperty("parent_id")
    @Size(max = 100)
    private String parentId;

    @JsonProperty("domain_id")
    @Size(max = 100)
    private String domainId;

    @JsonProperty("domain_name")
    @Size(max = 100)
    private String domainName;

    @JsonProperty("subscriber_user_id")
    @Size(max = 100)
    private String subscriberUserId;

    @JsonProperty("instance_id")
    @Size(max = 100)
    private String instanceId;

    @JsonProperty("subproduct_code")
    @Size(max = 100)
    private String subproductCode;

    @JsonProperty("sku_code")
    @Size(max = 100)
    private String skuCode;

    @JsonProperty("cbc_order_id")
    @Size(max = 100)
    private String cbcOrderId;

    @JsonProperty("subproduct_id")
    @Size(max = 100)
    private String subproductId;

    @JsonProperty("cloud_service_type")
    @Size(max = 100)
    private String cloudServiceType;

    @JsonProperty("region_id")
    @Size(max = 100)
    private String regionId;

    @JsonProperty("project_id")
    @Size(max = 100)
    private String projectId;

    @JsonProperty("resource_status")
    private ResourceStatus resourceStatus;

    @JsonProperty("tenant_type")
    private String tenantType;

    @JsonProperty("attribute_list")
    @Size(max = 1000)
    private List<SkuAttr> attributeList;

    public List<SkuAttr> getAttributeList() {
        return attributeList == null ? null : new ArrayList<>(attributeList);
    }

    public void setAttributeList(List<SkuAttr> attributeList) {
        this.attributeList = attributeList == null ? null : new ArrayList<>(attributeList);
    }
}
