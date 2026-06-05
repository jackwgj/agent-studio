/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class ProjectCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        // 获取配置中的system.permission.enabled属性值
        String enabled = context.getEnvironment().getProperty("system.permission.enabled");
        // 检查该属性是否为true，忽略大小写
        return Boolean.parseBoolean(enabled);
    }
}
