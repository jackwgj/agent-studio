/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.rce.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 数据源执行响应体
 */

@Data
@Builder
public class DatasourceExecuteRsp {

    @JsonProperty("output_list")
    private List<Object> outputList;

    @JsonProperty("row_num")
    private Integer rowNum;
}
