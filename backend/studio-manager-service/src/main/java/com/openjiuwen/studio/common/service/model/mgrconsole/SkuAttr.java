/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.common.service.model.mgrconsole;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * 属性
 */
@Data
public class SkuAttr {

    @JsonProperty("attr_code")
    private String attrCode;

    @JsonProperty("attr_value")
    private String attrValue;

    @JsonProperty("default_value")
    private String defaultValue;

    @JsonProperty("type")
    private String type;
}
