/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.license.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * ItepCheckResourceReq
 *
 */
@Data
@Accessors(chain = true)
public class ItepCheckResourceReq {
    /**
     * 资源项名称，即BBOM编码
     */
    @JsonProperty("item_name")
    private String itemName;
}
