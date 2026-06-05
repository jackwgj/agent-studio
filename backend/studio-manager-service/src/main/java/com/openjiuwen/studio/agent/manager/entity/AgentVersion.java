/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.agent.manager.dto.AdditionalQuestionsConfig;
import com.openjiuwen.studio.agent.manager.dto.AgentInfo;
import com.openjiuwen.studio.agent.common.dto.TriggerConfig;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 功能描述 Agent版本数据库实体
 *
 */
@Data
@NoArgsConstructor
public class AgentVersion {
    /**
     * agent版本唯一标识
     */
    @JsonProperty("version_id")
    private String versionId;

    /**
     * agent唯一标识
     */
    @JsonProperty("agent_id")
    private String agentId;

    /**
     * 租户唯一标识
     */
    @JsonProperty("project_id")
    private String projectId;

    /**
     * agent名称
     */
    private String name;

    /**
     * agent描述
     */
    private String description;

    /**
     * 标签
     */
    private List<String> tags;

    /**
     * agent图标
     */
    private String icon;

    /**
     * agent图标名称
     */
    @JsonProperty("icon_name")
    private String iconName;

    /**
     * agent使用的系统指令
     */
    private String instructions;

    /**
     * agent使用触发器
     */
    @JsonProperty("trigger_list")
    private List<TriggerConfig> triggerList;

    /**
     * agent开场白
     */
    private String prologue;

    /**
     * agent推荐问
     */
    @JsonProperty("suggest_queries")
    private List<String> suggestQueries;

    /**
     * 追问配置
     */
    @JsonProperty("additional_questions_config")
    private AdditionalQuestionsConfig additionalQuestionsConfig;

    @JsonProperty("ir_path")
    private String irPath;

    @JsonProperty("dsl_path")
    private String dslPath;

    /**
     * agent版本上线状态
     */
    private Boolean isOnline;

    /**
     * 应用创建者
     */
    private String creator;

    /**
     * agent创建时间
     */
    @JsonProperty("created_on")
    private Date createdOn;

    /**
     * agent更新时间
     */
    @JsonProperty("updated_on")
    private Date updatedOn;

    /**
     * agent发布时间
     */
    @JsonProperty("published_on")
    private Date publishedOn;

    /**
     * 构造函数
     *
     * @param agent agent
     */
    public AgentVersion(Agent agent) {
        this.versionId = UUID.randomUUID().toString();
        this.agentId = agent.getAgentId();
        this.projectId = agent.getProjectId();
        this.name = agent.getName();
        this.description = agent.getDescription();
        this.icon = agent.getIcon();
        this.instructions = agent.getInstructions();
        this.triggerList = agent.getTriggerList();
        this.prologue = agent.getPrologue();
        this.suggestQueries = agent.getSuggestQueries();
        this.additionalQuestionsConfig = agent.getAdditionalQuestionsConfig();
        this.isOnline = true;
        this.creator = agent.getCreator();
        this.createdOn = agent.getCreatedOn();
        this.updatedOn = agent.getUpdatedOn();
        this.publishedOn = new Date(System.currentTimeMillis());
    }

    /**
     * 转换为DTO
     *
     * @return AgentInfo
     */
    public AgentInfo convertToDto() {
        AgentInfo agentInfo = new AgentInfo();
        agentInfo.setAgentId(this.getAgentId());
        agentInfo.setProjectId(this.getProjectId());
        agentInfo.setName(this.getName());
        agentInfo.setDescription(this.getDescription());
        agentInfo.setIcon(this.getIcon());
        agentInfo.setInstructions(this.getInstructions());
        agentInfo.setTriggerList(this.getTriggerList());
        agentInfo.setPrologue(this.getPrologue());
        agentInfo.setSuggestQueries(this.getSuggestQueries());
        agentInfo.setAdditionalQuestionsConfig(this.getAdditionalQuestionsConfig());
        agentInfo.setCreator(this.getCreator());
        agentInfo.setCreateTime(this.getCreatedOn());
        agentInfo.setUpdateTime(this.getUpdatedOn());
        agentInfo.setPublishTime(this.getPublishedOn());
        return agentInfo;
    }
}
