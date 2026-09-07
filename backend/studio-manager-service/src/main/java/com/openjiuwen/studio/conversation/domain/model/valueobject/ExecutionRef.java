/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.conversation.domain.model.valueobject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 执行上下文值对象（消息归属）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionRef {
    /** 当前运行实例业务 ID。 */
    private String runId;

    /** 直接父运行实例业务 ID，根运行为空。 */
    private String parentRunId;

    /** Agent 配置或成员身份。 */
    private String agentId;

    /** agent 或 workflow。 */
    private String executionType;
}
