package com.openjiuwen.studio.agent.space.api.vo.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.util.List;

@Data
public class QueryManagerAgentResp {
    private int count;

    @JsonProperty("agent_list")
    private List<AgentListBean> agentList;

    @Data
    public static class AgentListBean {
        @JsonProperty("agent_id")
        private String agentId;

        @JsonProperty("project_id")
        private String projectId;

        private String name;

        private String type;

        private String description;

        private String icon;

        private Object details;

        private String instructions;

        @JsonProperty("model_deployment_id")
        private String modelDeploymentId;

        @JsonProperty("model_name")
        private String modelName;

        @JsonProperty("model_endpoint")
        private String modelEndpoint;

        @JsonProperty("model_version")
        private String modelVersion;

        @JsonProperty("model_type")
        private String modelType;

        private String model;

        @JsonProperty("model_config")
        private Object modelConfig;

        @JsonProperty("knowledge_retrieve_policy")
        private Object knowledgeRetrievePolicy;

        private String prologue;

        @JsonProperty("additional_questions_config")
        private Object additionalQuestionsConfig;

        @JsonProperty("voice_interaction")
        private Object voiceInteraction;

        private String status;

        private String url;

        private String creator;

        @JsonProperty("creator_id")
        private String creatorId;

        @JsonProperty("create_time")
        private String createTime;

        @JsonProperty("update_time")
        private String updateTime;

        @JsonProperty("publish_time")
        private String publishTime;

        @JsonProperty("workflow_switch_enabled")
        private boolean workflowSwitchEnabled;

        @JsonProperty("scheduling_mode")
        private String schedulingMode;

        private List<String> tags;

        private Object tools;

        private Object workflows;

        @JsonProperty("mcp_servers")
        private Object mcpServers;

        @JsonProperty("knowledge_repos")
        private Object knowledgeRepos;

        @JsonProperty("trigger_list")
        private Object triggerList;

        @JsonProperty("memory_variables")
        private Object memoryVariables;

        @JsonProperty("suggest_queries")
        private List<String> suggestQueries;
    }
}
