/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.conversation.domain.model;

import com.openjiuwen.studio.conversation.domain.model.valueobject.ExecutionRef;
import com.openjiuwen.studio.conversation.domain.model.valueobject.FileRef;
import com.openjiuwen.studio.conversation.domain.model.valueobject.TokenUsage;
import com.openjiuwen.studio.conversation.domain.model.valueobject.ToolRef;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * 会话消息实体（主 agent 消息与子 agent 消息统一建模，持久化拆两张表是实现细节）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationMessage {
    /**
     * 消息角色：user/assistant/tool
     */
    private String role;

    /**
     * 消息正文（user问题/assistant回答/tool结果）
     */
    private String content;

    /**
     * 工具引用（仅role=tool）
     */
    private ToolRef toolRef;

    /**
     * 文件引用
     */
    private List<FileRef> fileRefs;

    /** 执行上下文（run_id / parent_run_id / agent_id / execution_type）。 */
    private ExecutionRef executionRef;

    private String workflowId;
    private String nodeId;

    /**
     * Canonical ConversationEvent 可选携带的业务顺序元数据（来自事件 envelope 的 index）。
     * 该字段允许为空，不是时间戳；当前前端实时/历史展示仍以事件到达/创建时间顺序为准，
     * 不应将其作为默认或强制排序字段。
     */
    private Long eventIndex;

    /**
     * Token 用量（assistant 消息）
     */
    private TokenUsage tokenUsage;

    /**
     * canonical 事件类型：run_start/run_end/reasoning/message/tool_call/tool_result/error 等
     * （按轮持久化，role 区分内容）
     */
    private String event;

    /**
     * 模型部署id（溯源）
     */
    private String modelDeploymentId;

    /**
     * 创建时间（DB 托管，读取时回填）
     */
    private Date createdAt;
}
