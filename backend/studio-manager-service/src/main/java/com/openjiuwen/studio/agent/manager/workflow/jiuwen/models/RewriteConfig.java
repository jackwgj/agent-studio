/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.workflow.jiuwen.models;

import lombok.Data;

import java.util.Map;

/**
 * 改写接口配置类型
 *
 */
@Data
public class RewriteConfig {

    private String type;

    private Integer index;

    private boolean enabled;

    private Map<String, Object> args;
}
