/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.workflow.convert.adapt.difyadapter.models;

import com.openjiuwen.studio.agent.manager.workflow.convert.adapt.difyadapter.enums.DifyEnvVariablesType;
import lombok.Data;

@Data
public class DifyEnvironmentVariableVO {
    private String name;

    private Object value;

    private DifyEnvVariablesType valueType;

    private String description;

    private String refKey;
}
