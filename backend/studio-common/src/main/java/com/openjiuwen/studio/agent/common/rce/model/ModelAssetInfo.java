/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.rce.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * 功能描述 模型资产信息
 *
 */
@Data
public class ModelAssetInfo {
    /**
     * 资产ID
     */
    @JsonProperty("asset_id")
    private String assetId;

    /**
     * 资产类型
     */
    @JsonProperty("asset_type")
    private String assetType;

    /**
     * 资产子类型，资产类型的子类型，描述了资产的具体类型。
     */
    @JsonProperty("sub_asset_type")
    private String subAssetType;

    /**
     * 资产标签
     */
    @JsonProperty("asset_tag")
    private String assetTag;

    /**
     * 资产编码
     */
    @JsonProperty("asset_code")
    private String assetCode;

    /**
     * 模型诺亚版本号，部署页面展示版本
     */
    @JsonProperty("external_version")
    private String externalVersion;

    /**
     * 模型内部版本号
     */
    @JsonProperty("asset_version")
    private String assetVersion;
}
