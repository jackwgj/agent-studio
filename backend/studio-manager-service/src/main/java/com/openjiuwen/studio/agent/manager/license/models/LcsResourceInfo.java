/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.license.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ItepCheckResourceRsp
 *
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LcsResourceInfo {
    /**
     * 资源项名称，即BBOM编码
     */
    @JsonProperty("item_name")
    private String itemName;

    /**
     * 已使用资源数量
     */
    @JsonProperty("item_used_num")
    private Integer itemUsedNum;

    /**
     * 资源项总量
     */
    @JsonProperty("item_total_num")
    private Integer itemTotalNum;
}
