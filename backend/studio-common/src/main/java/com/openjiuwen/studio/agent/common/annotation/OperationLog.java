/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.annotation;

import com.openjiuwen.studio.agent.common.enums.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 审计日志注解，标记需要记录审计日志的 Service 方法。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationLog {

    /**
     * 操作类型，如 CREATE/UPDATE/DELETE/READ/EXPORT/IMPORT。
     */
    OperationType operationType();

    /**
     * 资源类型描述，如 "Agent"、"Workflow"、"Tool"。
     */
    String resourceType();

    /**
     * 操作描述信息，如 "创建智能体"。
     */
    String description();

    /**
     * 资源ID，支持两种格式：
     * 1. 参数名字：如 "agentId" 表示从 agentId 参数获取
     * 2. 对象属性路径：如 "body.id" 表示从 body 参数的 id 属性获取
     * 3. 数组下标访问：如 "body.ids[0]" 表示从 body 参数的 ids 数组获取第一个元素
     * 默认为空字符串，表示不提取资源ID。
     */
    String resourceId() default "";

    /**
     * 资源名称，获取方式同 resourceId。
     * 用于记录资源的显示名称，如智能体名称、工作流名称等。
     * 默认为空字符串。
     */
    String resourceName() default "";
}