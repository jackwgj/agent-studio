/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.license.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * LcsResourceDto
 *
 */
@Data
@Accessors(chain = true)
public class LcsResourceDto {
    /**
     * 资源项名称，即BBOM编码
     */
    @JsonProperty("resource_name")
    private String resourceName;

    /**
     * 申请资源量
     */
    @JsonProperty("resource_request_num")
    private Integer resourceRequestNum;
}
