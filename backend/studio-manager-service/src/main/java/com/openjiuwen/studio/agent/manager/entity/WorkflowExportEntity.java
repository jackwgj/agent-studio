/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.agent.manager.dto.McpServerInfo;
import com.openjiuwen.studio.agent.manager.dto.WorkflowVO;
import lombok.Data;

import java.util.List;

@Data
public class WorkflowExportEntity {
    private WorkflowVO dsl;

    private WorkflowEntity metadata;

    @JsonProperty("import_type")
    private String importType;

    private List<ToolEntity> plugins;

    @JsonProperty("sub_workflows")
    private List<WorkflowExportEntity> subWorkflows;

    @JsonProperty("mcp_servers")
    private List<McpServerInfo> mcpServers;

    /**
     * 子工作流版本信息
     */
    @JsonProperty("release_version")
    private ReleaseVersion releaseVersion;

    @JsonProperty("share_info")
    private ShareInfo shareInfo;

    @JsonProperty("share_reference")
    private List<MappingEntity> shareResourceReferenceList;

    @JsonProperty("signature")
    private String signature;
}
