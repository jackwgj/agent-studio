/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.conversation.infrastructure.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 对话式工作台主agent消息表实体（一行 = 一条消息，工具调用也是消息）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_conversation_run")
public class ConversationRunEntity {
    /**
     * 代理主键
     */
    @JsonProperty("id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 业务主键（一次输入输出轮次，=引擎execution_id）
     */
    @JsonProperty("run_id")
    private String runId;

    @JsonProperty("parent_run_id")
    private String parentRunId;

    /**
     * 会话ID
     */
    @JsonProperty("conversation_id")
    private String conversationId;

    /**
     * 消息角色：user/assistant/tool（引擎透传）
     */
    @JsonProperty("role")
    private String role;

    /**
     * 消息正文（user问题/assistant回答/tool结果）
     */
    @JsonProperty("content")
    private String content;

    /**
     * 工具标识（仅role=tool），=t_tool.tool_id
     */
    @JsonProperty("tool_id")
    private String toolId;

    @JsonProperty("tool_name")
    private String toolName;

    /**
     * 工具调用请求参数json（仅role=tool）
     */
    @JsonProperty("tool_args")
    private String toolArgs;

    /**
     * 文件引用json数组
     */
    @JsonProperty("file_ids")
    private String fileIds;

    /** 统一事件类型。 */
    @JsonProperty("event")
    private String event;

    /** Agent 或 Workflow。 */
    @JsonProperty("execution_type")
    private String executionType;

    @JsonProperty("workflow_id")
    private String workflowId;

    @JsonProperty("node_id")
    private String nodeId;

    @JsonProperty("event_index")
    private Long eventIndex;


    /**
     * 主agent（溯源）
     */
    @JsonProperty("agent_id")
    private String agentId;

    /**
     * 模型部署id，=t_model_service.ID
     */
    @JsonProperty("model_deployment_id")
    private String modelDeploymentId;

    @JsonProperty("total_tokens")
    private String totalTokens;

    @JsonProperty("prompt_tokens")
    private String promptTokens;

    @JsonProperty("completion_tokens")
    private String completionTokens;

    @JsonProperty("project_id")
    private String projectId;

    @JsonProperty("workspace_id")
    private String workspaceId;

    @JsonProperty("domain_id")
    private String domainId;

    @JsonProperty("creator")
    private String creator;

    @JsonProperty("creator_id")
    private String creatorId;

    @JsonProperty("updater")
    private String updater;

    @JsonProperty("updater_id")
    private String updaterId;

    @JsonProperty("created_on")
    private Date createdOn;

    @JsonProperty("updated_on")
    private Date updatedOn;

    @JsonProperty("deleted")
    private Integer deleted;
}
