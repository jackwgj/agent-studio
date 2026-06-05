/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.workflow.jiuwen.models;

import com.openjiuwen.studio.agent.manager.dto.WorkflowFieldVO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SetVariableConfig配置类型
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SetVariableConfig {
    private WorkflowFieldVO left;

    private WorkflowFieldVO right;
}
