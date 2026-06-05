/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.models.assistant;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.agent.manager.dto.ModelConfig;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Assistant对象，可以通过大模型完成工具的选择和调用
 *
 */
@Data
@Setter
@Getter
public class Assistant {
    @JsonProperty("assistant_id")
    private String assistantId;

    @JsonProperty("project_id")
    private String projectId;

    @JsonProperty("workspace_id")
    private String workspaceId;

    private String name;

    private String description;

    private String url;

    @JsonProperty("model_deployment_id")
    private String modelDeploymentId;

    @JsonProperty("model_name")
    private String modelName;

    @JsonProperty("model_endpoint")
    private String modelEndpoint;

    @JsonProperty("model_version")
    private String modelVersion;

    @JsonProperty("model_config")
    private ModelConfig modelConfig;

    private String instructions;

    @JsonProperty("key_messages")
    private List<String> keyMessages;

    private List<ToolReference> tools;

    @JsonProperty("knowledge_repos")
    private List<KnowledgeReference> knowledgeRepos;

    @JsonProperty("tool_retrieve_policy")
    private ToolRetrievePolicy toolRetrievePolicy;

    @JsonProperty("history_messages_policy")
    private HistoryMessagesPolicy historyMessagesPolicy;

    private String metadata;
}
