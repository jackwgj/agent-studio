/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.workflow.models;

import lombok.Data;

/**
 * 大模型节点prompt配置
 *
 */
@Data
public class WorkflowPromptConfig {
    private Integer history;

    private String template;
}
