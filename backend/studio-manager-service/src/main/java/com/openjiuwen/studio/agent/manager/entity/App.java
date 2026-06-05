/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.agent.manager.dto.AppInfo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 功能描述 App数据库实体
 *
 */
@Data
@NoArgsConstructor
public class App {
    /**
     * 应用唯一标识
     */
    @JsonProperty("app_id")
    private String appId;

    /**
     * 租户唯一标识
     */
    @JsonProperty("project_id")
    private String projectId;

    /**
     * 空间唯一标识
     */
    @JsonProperty("workspace_id")
    private String workspaceId;

    /**
     * 应用名称
     */
    private String name;

    /**
     * 应用描述
     */
    private String description;

    /**
     * 应用图标
     */
    private String icon;

    /**
     * 应用图标名称
     */
    @JsonProperty("icon_name")
    private String iconName;

    /**
     * 应用绑定标签列表
     */
    @JsonProperty("tags")
    private List<String> tags;

    /**
     * 应用类型
     */
    @JsonProperty("app_type")
    private String appType;

    /**
     * 应用来源标识
     */
    @JsonProperty("resource_id")
    private String resourceId;

    /**
     * 应用来源类型
     */
    @JsonProperty("resource_type")
    private String resourceType;

    /**
     * 应用创建者
     */
    private String creator;

    /**
     * 工作流类型
     */
    @JsonProperty("workflow_type")
    private String workflowType;

    /**
     * 工作流输入参数
     */
    @JsonProperty("input_params")
    private String inputParams;

    /**
     * 工作流输出参数
     */
    @JsonProperty("output_params")
    private String outputParams;

    /**
     * 工作流开场白
     */
    @JsonProperty("prologue")
    private String prologue;

    /**
     * 工作流推荐问
     */
    @JsonProperty("suggest_queries")
    private List<String> suggestQueries;

    /**
     * 应用发布时间
     */
    @JsonProperty("published_on")
    private Date publishedOn;

    /**
     * 构造函数
     *
     * @param agent agent
     */
    public App(com.openjiuwen.studio.agent.manager.entity.Agent agent) {
        this.appId = UUID.randomUUID().toString();
        this.projectId = agent.getProjectId();
        this.workspaceId = agent.getWorkspaceId();
        this.name = agent.getName();
        this.description = agent.getDescription();
        this.icon = agent.getIcon();
        this.appType = "chat";
        this.resourceId = agent.getAgentId();
        this.resourceType = "agent";
        this.creator = agent.getCreator();
        this.publishedOn = new Date(System.currentTimeMillis());
    }

    /**
     * 构造函数
     *
     * @param workflow workflow
     */
    public App(WorkflowEntity workflow) {
        this.appId = UUID.randomUUID().toString();
        this.projectId = workflow.getProjectId();
        this.name = workflow.getName();
        this.description = workflow.getDescription();
        this.icon = workflow.getAvatar();
        this.appType = "scene";
        this.resourceId = workflow.getId();
        this.resourceType = "workflow";
        this.workflowType = workflow.getWorkflowType();
        this.creator = workflow.getCreatedBy();
        this.publishedOn = new Date(System.currentTimeMillis());
    }

    /**
     * 转换Dto
     *
     * @return appInfo appInfo
     */
    public AppInfo convertToDto() {
        AppInfo appInfo = new AppInfo();
        appInfo.setAppId(this.getAppId());
        appInfo.setProjectId(this.getProjectId());
        appInfo.setName(this.getName());
        appInfo.setDescription(this.getDescription());
        appInfo.setIcon(this.getIcon());
        appInfo.setTags(this.getTags());
        appInfo.setAppType(this.getAppType());
        appInfo.setResourceId(this.getResourceId());
        appInfo.setResourceType(this.getResourceType());
        appInfo.setWorkflowType(this.getWorkflowType());
        appInfo.setCreator(this.getCreator());
        appInfo.setPublishTime(this.getPublishedOn());
        return appInfo;
    }
}
