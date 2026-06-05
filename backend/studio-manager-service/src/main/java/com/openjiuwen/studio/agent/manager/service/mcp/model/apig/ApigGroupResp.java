/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service.mcp.model.apig;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.huaweicloud.sdk.apig.v2.model.ApiGroupInfo;

import lombok.Data;

import java.util.List;

/**
 * APIG 查询专享版实例列表响应类
 *
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApigGroupResp {
    @JsonProperty("size")
    private Integer size;

    @JsonProperty("total")
    private long total;

    @JsonProperty("groups")
    private List<ApiGroupInfo> groups;
}
