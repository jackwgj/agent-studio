/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.common.service.model.skuinst;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class LicenseInst {

    private String id;

    @JsonProperty("resource_id")
    private String resourceId;

    @JsonProperty("sku_code")
    private String skuCode;

    @JsonProperty("attr_code")
    private String attrCode;

    @JsonProperty("current_value")
    private String currentValue;

    @JsonProperty("max_value")
    private String maxValue;

    private String type;

    private String status;
}
