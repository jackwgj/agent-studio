/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.common.service.model.skuinst;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class LicenseReport {

    /**
     * 操作类型：add/delete,不填充即为Add
     */
    @JsonProperty("oper_type")
    private String operType;

    @JsonProperty("resource_id")
    private String resourceId;

    @JsonProperty("attr_code")
    private String attrCode; // sku_attr_code

    @JsonProperty("quantity")
    private String quantity;
}
