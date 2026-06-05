/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.models.knowledge;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.agent.common.dto.knowledge.ModelExtendConfig;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 修改模型请求体
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ModifyModelReq {
    private String detail;

    private String endpoint;

    @JsonProperty("extend_config")
    private ModelExtendConfig extendConfig;
}
