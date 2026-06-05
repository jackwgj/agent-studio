/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

/**
 * 数据库状态检查返回值
 *
 */
@Data
@Builder
public class DatasourceStatusCheckRsp {
    private String status;

    @JsonProperty("error_message")
    private String errorMessage;
}
