/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.workflow.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 任务执行结果类
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NodeExecuteResult {
    /**
     * 执行id
     */
    private String instanceId;

    /**
     * 执行状态
     */
    @Builder.Default
    private NodeExecutionStatus nodeExecutionStatus = NodeExecutionStatus.RUNNING;

    /**
     * 内部执行入参
     */
    @Builder.Default
    private Map<String, Object> inputs = new HashMap<>();

    /**
     * 内部执行返回值
     */
    @Builder.Default
    private Map<String, Object> outputs = new HashMap<>();

    /**
     * metadata字段，如token等信息
     */
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    /**
     * 错误信息，节点执行失败时返回值
     */
    private String errorMsg;
}
