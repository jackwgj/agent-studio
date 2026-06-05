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
