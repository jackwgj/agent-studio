/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.entity;

import com.openjiuwen.studio.agent.manager.dto.AdditionalQuestionsConfig;
import com.openjiuwen.studio.agent.manager.dto.AgentMemoryConfig;
import com.openjiuwen.studio.agent.manager.dto.AgentVariable;
import com.openjiuwen.studio.agent.manager.dto.KnowledgeRetrievePolicy;
import com.openjiuwen.studio.agent.manager.dto.MemoryVariable;
import com.openjiuwen.studio.agent.manager.dto.ModelConfig;
import com.openjiuwen.studio.agent.common.dto.TriggerConfig;
import com.openjiuwen.studio.agent.manager.dto.VoiceInteraction;
import com.openjiuwen.studio.agent.manager.mapper.handler.AdditionalQuestionsConfigHandler;
import com.openjiuwen.studio.agent.manager.mapper.handler.AgentMemoryConfigHandler;
import com.openjiuwen.studio.agent.manager.mapper.handler.AgentVariableArrayHandler;
import com.openjiuwen.studio.agent.manager.mapper.handler.JsonHandlerKnowledgeRetrievePolicy;
import com.openjiuwen.studio.agent.manager.mapper.handler.ListHandler;
import com.openjiuwen.studio.agent.manager.mapper.handler.MemoryVariableArrayHandler;
import com.openjiuwen.studio.agent.manager.mapper.handler.ModelConfigHandler;
import com.openjiuwen.studio.agent.manager.mapper.handler.TriggerConfigArrayHandler;
import com.openjiuwen.studio.agent.manager.mapper.handler.VoiceInteractionHandler;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * 已删除的Agent资源表
 *
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name = "t_history_agent")
public class HistoryAgentEntity {
    /**
     * 已删除Agent资源表唯一标识
     */
    @Id
    @Column(name = "history_id")
    private String historyId;

    /**
     * agent唯一标识
     */
    @Column(name = "agent_id")
    private String agentId;

    /**
     * 租户唯一标识
     */
    @Column(name = "project_id")
    private String projectId;

    /**
     * 溯源ID
     */
    @Column(name = "trace_id")
    private String traceId;

    @Column(name = "deleted")
    private Integer deleted;

    /**
     * 项目空间ID
     */
    @Column(name = "workspace_id")
    private String workspaceId;

    /**
     * 项目空间ID
     */
    @Column(name = "domain_id")
    private String domainId;

    /**
     * agent名称
     */
    private String name;

    /**
     * agent描述
     */
    private String description;

    /**
     * agent图标
     */
    private String icon;

    /**
     * agent关联的模型deployment_id
     */
    @Column(name = "model_deployment_id")
    private String modelDeploymentId;

    /**
     * agent关联的模型名称
     */
    @Column(name = "model_name")
    private String modelName;

    /**
     * agent关联的模型参数
     */
    @Convert(converter = ModelConfigHandler.class)
    @Column(name = "model_config")
    private ModelConfig modelConfig;

    /**
     * agent关联的模型类型
     */
    @Column(name = "model_type")
    private String modelType;

    /**
     * agent关联的模型资产名称
     */
    private String model;

    /**
     * 规划模型是否独立配置
     */
    @Column(name = "plan_qa_independent")
    private Boolean planQaIndependent;

    /**
     * 规划模型资产名称
     */
    @Column(name = "plan_model")
    private String planModel;

    /**
     * 规划模型deployment_id
     */
    @Column(name = "plan_model_deployment_id")
    private String planModelDeploymentId;

    /**
     * 规划模型名称
     */
    @Column(name = "plan_model_name")
    private String planModelName;

    /**
     * 规划模型参数
     */
    @Convert(converter = ModelConfigHandler.class)
    @Column(name = "plan_model_config")
    private ModelConfig planModelConfig;

    /**
     * 规划模型类型
     */
    @Column(name = "plan_model_type")
    private String planModelType;

    /**
     * agent使用的系统指令
     */
    @Column(name = "instructions", columnDefinition = "TEXT")
    private String instructions;

    /**
     * agent使用触发器
     */
    @Convert(converter = TriggerConfigArrayHandler.class)
    @Column(name = "trigger_list", columnDefinition = "TEXT")
    private List<TriggerConfig> triggerList;

    /**
     * agent使用的记忆变量
     */
    @Convert(converter = MemoryVariableArrayHandler.class)
    @Column(name = "memory_variables", columnDefinition = "TEXT")
    private List<MemoryVariable> memoryVariables;

    /**
     * agent开场白
     */
    private String prologue;

    /**
     * agent推荐问
     */
    @Convert(converter = ListHandler.class)
    @Column(name = "suggest_queries", columnDefinition = "TEXT")
    private List<String> suggestQueries;

    /**
     * 追问配置
     */
    @Convert(converter = AdditionalQuestionsConfigHandler.class)
    @Column(name = "additional_questions_config", columnDefinition = "TEXT")
    private AdditionalQuestionsConfig additionalQuestionsConfig;

    /**
     * 语音配置
     */
    @Convert(converter = VoiceInteractionHandler.class)
    @Column(name = "voice_interaction", columnDefinition = "TEXT")
    private VoiceInteraction voiceInteraction;

    /**
     * 知识检索策略
     */
    @Convert(converter = JsonHandlerKnowledgeRetrievePolicy.class)
    @Column(name = "knowledge_retrieve_policy", columnDefinition = "TEXT")
    private KnowledgeRetrievePolicy knowledgeRetrievePolicy;

    /**
     * 内容审核
     */
    @Column(name = "content_review")
    private String contentReview;

    /**
     * 安全护栏
     */
    @Column(name = "safety_barrier")
    private Boolean safetyBarrier;

    @Column(name = "dsl_path")
    private String dslPath;

    @Column(name = "ir_path")
    private String irPath;

    private String type;

    /**
     * agent子类型（如：deepresearch）
     */
    @Column(name = "sub_type")
    private String subType;

    /**
     * agent状态
     */
    private String status;

    /**
     * 应用创建者
     */
    private String creator;

    /**
     * 应用创建者ID
     */
    @Column(name = "creator_id")
    private String creatorId;

    /**
     * 工作流跳转
     */
    @Column(name = "workflow_switch_enabled")
    private Boolean workflowSwitchEnabled;

    /**
     * agent调度模式，ReAct、RAG
     */
    @Column(name = "scheduling_mode")
    private String schedulingMode;

    /**
     * agent创建时间
     */
    @Column(name = "created_on")
    private Date createdOn;

    /**
     * agent更新时间
     */
    @Column(name = "updated_on")
    private Date updatedOn;

    /**
     * agent发布时间
     */
    @Column(name = "published_on")
    private Date publishedOn;

    /**
     * agent图标名称
     */
    @Column(name = "icon_name")
    private String iconName;

    /**
     * Agent定义的变量列表
     */
    @Convert(converter = AgentVariableArrayHandler.class)
    @Column(name = "agent_variables", columnDefinition = "TEXT")
    private List<AgentVariable> agentVariables;

    /**
     * 记忆相关的配置
     */
    @Convert(converter = AgentMemoryConfigHandler.class)
    @Column(name = "memory_config", columnDefinition = "TEXT")
    private AgentMemoryConfig memoryConfig;

    /**
     * 是否已共享,(0=否，1=是)
     */
    @Column(name = "is_share")
    private Integer isShare;
}
