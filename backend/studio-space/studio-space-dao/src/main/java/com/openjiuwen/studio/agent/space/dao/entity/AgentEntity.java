package com.openjiuwen.studio.agent.space.dao.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Created by Fang Zhen on 2024/5/13.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AgentEntity extends BaseProperties {
    private String id;

    private String name;

    // type=2为问答问数
    private Integer type;

    private String description;

    private String logo; // 生成方法

    private String instructions;

    @JsonProperty("agent_work_type")
    private String agentWorkType;

    private List<String> tools;

    private List<String> mcps;

    private List<String> datasets;

    private String prologue;

    @JsonProperty("suggest_query")
    private List<String> suggestQuery;

    @JsonProperty("model_id")
    private String modelId;

    @JsonProperty("model_setting")
    private String modelSetting;

    private Boolean isEnable = false;

    @JsonProperty("agent_uri")
    private String agentUri;

    @JsonProperty("web_uri")
    private String webUri;

    private List<String> flows;

    @JsonProperty("rag_flows")
    private List<String> ragFlows;

    @JsonProperty("workflow_settings")
    private String workflowSetting;

    @JsonProperty("knowledge_setting")
    private String knowledgeSetting;

    @JsonProperty("qa_model_id")
    private String qaModelId;

    @JsonProperty("qa_model_setting")
    private String qaModelSetting;

    @JsonProperty("fragment_memory_settings")
    private String fragmentMemorySettings;

    @JsonProperty("variable_settings")
    private String variableSettings;

    @JsonProperty("file_box")
    private String fileBox;

    @JsonProperty("audio_settings")
    private String audioSettings;

    @JsonProperty("asset_status")
    private String assetStatus;

    /**
     * apiKey使用巫山加密
     */
    @JsonProperty("api_key")
    private String apiKey;

    @JsonProperty("agent_code")
    private String agentCode;
}
