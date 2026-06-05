/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.model.ops;

import com.openjiuwen.studio.agent.runtime.dto.JiuwenEvent;

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description:
 **/
@Data
public class TraceInfo {
    /**
     * 租户 id
     */
    private String domainId;

    /**
     * 项目 id
     */
    private String projectId;

    /**
     * 工作空间 id
     */
    private String workspaceId;

    /**
     * agent id
     */
    private String agentId;

    /**
     * 会话 id
     */
    private String conversationId;

    /**
     * 等同trace id
     */
    private String executionId;

    /**
     * 用户 id
     */
    private String userId;

    /**
     * trace上报模式
     */
    private String traceMode;

    /**
     * 资源分类
     */
    private String platform;

    /**
     * 根span id
     */
    private String rootSpanId;

    /**
     * 中断后的流程中，首节点的输入，用于userInput节点的输入
     */
    private String firstInput;

    /**
     * 中断后的流程中，最后一个节点的输出，用于userInput节点的输出
     */
    private String lastOutput;

    /**
     * 上次中断时间
     */
    private long lastBreakTime;

    /**
     * 控制器节点是否已上报
     */
    private boolean controllerNodeUpload;

    /**
     * key: nodeId
     * value: spanId
     */
    private Map<String, String> spanIdMap = new HashMap<>();

    /**
     * jiuwen Yu
     */
    private List<JiuwenEvent> jiuwenEventList;
}
