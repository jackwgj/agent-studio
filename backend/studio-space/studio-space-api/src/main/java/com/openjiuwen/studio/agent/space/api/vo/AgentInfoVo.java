package com.openjiuwen.studio.agent.space.api.vo;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.agent.space.dao.entity.BaseProperties;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.sql.Timestamp;
import java.util.List;

/**
 * Created by Fang Zhen on 2024/5/13.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AgentInfoVo extends BaseProperties {
    private String id;

    private String name; //

    private String type;

    private String description;

    private String logo; // 生成方法

    private String instructions;

    private List<AgentTool> tools;

    @JsonProperty("mcp_servers")
    private List<McpServerRuntimeVo> mcpServers;

    private List<Dataset> datasets;

    private String prologue;

    @JsonProperty("agent_work_type")
    private String agentWorkType;

    @JsonProperty("file_box")
    private JSONObject fileBox;

    @JsonProperty("file_infos")
    private List<FileInfo> fileInfos;

    @JsonProperty("suggest_query")
    private List<String> suggestQuery;

    @JsonProperty("model_id")
    private String modelId;

    @JsonProperty("model_setting")
    private ModelSetting modelSetting;

    @JsonProperty("is_enable")
    private Boolean isEnable;

    @JsonProperty("agent_uri")
    private String agentUri;

    @JsonProperty("web_uri")
    private String webUri;

    private List<AgentLinkFlowEntity> flows;

    @JsonProperty("rag_flows")
    private List<AgentLinkFlowEntity> ragFlows;

    private String apiKey;

    @JsonProperty("workflow_settings")
    private FlowSetting workflowSetting;

    @JsonProperty("knowledge_setting")
    private KnowledgeSetting knowledgeSetting;

    @JsonProperty("qa_model_id")
    @TableField("qa_model_id")
    private String qaModelId;

    @JsonProperty("qa_model_setting")
    @TableField("qa_model_setting")
    private ModelSetting qaModelSetting;

    @JsonProperty("fragment_memory_settings")
    private FragmentMemorySettings fragmentMemorySettings;

    @JsonProperty("variable_settings")
    private VariableSettings variableSettings;

    @Data
    @Builder
    public static class FileInfo {
        @JsonProperty("id")
        private String fileId;

        @JsonProperty("name")
        private String fileName;

        @JsonProperty("size")
        private long fileSize;

        @JsonProperty("last_updated_time")
        private Timestamp lastUpdatedTime;
    }

    @JsonProperty("audio_settings")
    private AudioSettings audioSettings;

    @JsonProperty("instance_size")
    private String instanceSize;

    @JsonProperty("instance_amount")
    private String instanceAmount;

    // 控制台对应的资源ID
    private String resourceId;

    @JsonProperty("asset_status")
    private String assetStatus;

    @JsonProperty("schedule")
    private ScheduleVo scheduleVo;
}
