/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.prompt.engineering.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 功能描述 资源信息
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResource {

    @JsonProperty("resource_id")
    private String resourceId;

    @JsonProperty("resource_type")
    private String resourceType;

    @JsonProperty("resource_spec_code")
    private String resourceSpecCode;

    @JsonProperty("product_code")
    private String productCode;

    @JsonProperty("resource_charging_mode")
    private String resourceChargingMode;

    @JsonProperty("status")
    private String status;

    @JsonProperty("create_time")
    private Date createTime;

    @JsonProperty("update_time")
    private Date updateTime;
}
