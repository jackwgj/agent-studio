/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.workflow.jiuwen.models;

import lombok.Data;

import java.util.Map;

/**
 * JiuWen code配置类型
 *
 */
@Data
public class CodeConfig {
    private Map<String, String> code;
}
