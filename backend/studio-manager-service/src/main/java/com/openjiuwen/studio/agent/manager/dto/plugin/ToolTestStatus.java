/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.dto.plugin;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ToolTestStatus {
    @JsonProperty("tool_id")
    private String toolId;

    /**
     * 工具测试状态，0：失败；1：成功；2：未知
     */
    @JsonProperty("test_status")
    private Integer testStatus;
}
