/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto.skuinst;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
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
