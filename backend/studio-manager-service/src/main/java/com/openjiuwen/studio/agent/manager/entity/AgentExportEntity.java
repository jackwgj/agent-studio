/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.agent.manager.bo.SkillDetails;
import com.openjiuwen.studio.agent.manager.dto.AgentInfo;
import com.openjiuwen.studio.agent.manager.dto.ControllerVO;
import com.openjiuwen.studio.agent.manager.dto.McpServerInfo;
import com.openjiuwen.studio.agent.manager.dto.Scene;

import lombok.Data;

import java.util.List;

@Data
public class AgentExportEntity {
    /**
     * agent元数据
     */
    private Agent metadata;

    /**
     * 多agent DSL
     */
    private ControllerVO dsl;

    /**
     * 导出类型
     */
    @JsonProperty("import_type")
    private String importType;

    /**
     * 插件元数据
     */
    private List<ToolEntity> plugins;

    /**
     * 工作流导出封装对象
     */
    private List<com.openjiuwen.studio.agent.manager.entity.WorkflowExportEntity> workflows;

    /**
     * mcp服务元数据
     */
    @JsonProperty("mcp_servers")
    private List<McpServerInfo> mcpServers;

    /**
     * skills
     */
    private List<SkillDetails> skills;

    /**
     * 子控制器
     */
    private List<AgentExportEntity> agents;

    /**
     * 子控制器版本信息
     */
    @JsonProperty("release_version")
    private ReleaseVersion releaseVersion;

    /**
     * 资源共享信息
     */
    @JsonProperty("share_info")
    private ShareInfo shareInfo;

    @JsonProperty("share_reference")
    private List<MappingEntity> shareResourceReferenceList;

    @JsonProperty("signature")
    private String signature;

    /**
     * 动态规划模式下场景信息，未存在数据库中，所以需要特殊处理
     */
    private List<Scene> scenes;

    /**
     * 子单智能体
     */
    private List<AgentExportEntity> singleAgents;

    private AgentInfo singleAgentDsl;
}
