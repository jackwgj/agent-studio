/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import com.openjiuwen.studio.agent.common.dto.TriggerConfig;
import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 修改智能体请求体。
 */
@ApiModel(description = "修改智能体请求体。")

@Validated

public class ModifyAgentReq implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("name")
    @Pattern(
        regexp = "^[\\u4e00-\\u9fa5a-zA-Z0-9_\\-（）()！!](?:[\\u4e00-\\u9fa5a-zA-Z0-9_\\-（）()！! ]*[\\u4e00-\\u9fa5a-zA-Z0-9_\\-（）()！!])?$")
    @Length(min = 2, max = 64)
    private String name = null;

    @JsonProperty("type")
    private TypeEnum type = null;

    @JsonProperty("description")
    @Length(min = 1, max = 256)
    private String description = null;

    @JsonProperty("icon")
    private String icon = null;

    @JsonProperty("details")
    @Valid
    @NotNull
    private ControllerVO details = null;

    @JsonProperty("instructions")
    @Length(max = 10000)
    private String instructions = null;

    @JsonProperty("model_deployment_id")
    @Length(max = 128)
    private String modelDeploymentId = null;

    @JsonProperty("model_name")
    @Length(max = 256)
    private String modelName = null;

    @JsonProperty("model_endpoint")
    private String modelEndpoint = null;

    @JsonProperty("model_version")
    private String modelVersion = null;

    @JsonProperty("model_config")
    @Valid
    private ModelConfig modelConfig = null;

    @JsonProperty("model_type")
    @Length(max = 32)
    private String modelType = null;

    @JsonProperty("strategy_flag")
    private Boolean strategyFlag = null;

    @JsonProperty("model")
    @Length(max = 64)
    private String model = null;

    @JsonProperty("plan_qa_independent")
    private Boolean planQaIndependent = null;

    @JsonProperty("plan_model")
    @Length(max = 64)
    private String planModel = null;

    @JsonProperty("plan_model_deployment_id")
    @Length(max = 128)
    private String planModelDeploymentId = null;

    @JsonProperty("plan_model_name")
    @Length(max = 256)
    private String planModelName = null;

    @JsonProperty("plan_model_config")
    @Valid
    private ModelConfig planModelConfig = null;

    @JsonProperty("plan_model_type")
    @Length(max = 32)
    private String planModelType = null;

    @JsonProperty("tools")
    @Valid
    @Size()
    private List<@Length() String> tools = null;

    @JsonProperty("tool_details")
    @Valid
    @Size()
    private List<AgentToolDetail> toolDetails = null;

    @JsonProperty("workflows")
    @Valid
    @Size()
    private List<@Length() String> workflows = null;

    @JsonProperty("workflow_details")
    @Valid
    @Size()
    private List<AgentWorkflowDetail> workflowDetails = null;

    @JsonProperty("mcp_servers")
    @Valid
    @Size()
    private List<@Length() String> mcpServers = null;

    @JsonProperty("mcp_choose_tools")
    @Valid
    @Size()
    private Map<String, List<String>> mcpChooseTools = null;

    @JsonProperty("mcp_details")
    @Valid
    @Size()
    private List<AgentMcpDetail> mcpDetails = null;

    @JsonProperty("knowledge_repos")
    @Valid
    @Size()
    private List<@Length() String> knowledgeRepos = null;

    @JsonProperty("trigger_list")
    @Valid
    @Size()
    private List<TriggerConfig> triggerList = null;

    @JsonProperty("memory_variables")
    @Valid
    @Size()
    private List<MemoryVariable> memoryVariables = null;

    @JsonProperty("prologue")
    @Length(max = 512)
    private String prologue = null;

    @JsonProperty("is_template")
    private Boolean isTemplate = null;

    @JsonProperty("search_engine")
    @Valid
    private SearchEngine searchEngine = null;

    @JsonProperty("suggest_queries")
    @Valid
    @Size()
    private List<@Length(max = 512) String> suggestQueries = null;

    @JsonProperty("additional_questions_config")
    @Valid
    private AdditionalQuestionsConfig additionalQuestionsConfig = null;

    @JsonProperty("voice_interaction")
    @Valid
    private VoiceInteraction voiceInteraction = null;

    @JsonProperty("knowledge_retrieve_policy")
    @Valid
    private KnowledgeRetrievePolicy knowledgeRetrievePolicy = null;

    @JsonProperty("agent_variables")
    @Valid
    @Size(max = 30)
    private List<AgentVariable> agentVariables = null;

    @JsonProperty("input_variables")
    @Valid
    @Size(max = 30)
    private List<InputVariable> inputVariables = null;

    @JsonProperty("memory_config")
    @Valid
    private AgentMemoryConfig memoryConfig = null;

    @JsonProperty("content_review")
    @Valid
    @Size()
    private Map<String, Object> contentReview = null;

    @JsonProperty("safety_barrier")
    private Boolean safetyBarrier = null;

    @JsonProperty("status")
    private Integer status = null;

    @JsonProperty("creator")
    private String creator = null;

    @JsonProperty("scheduling_mode")
    private SchedulingModeEnum schedulingMode = null;

    @JsonProperty("workflow_switch_enabled")
    private Boolean workflowSwitchEnabled = null;

    @JsonProperty("create_time")
    private Date createTime = null;

    @JsonProperty("update_time")
    private Date updateTime = null;

    @JsonProperty("publish_time")
    private Date publishTime = null;

    @JsonProperty("reference")
    @Valid
    private Reference reference = null;

    @JsonProperty("scenes")
    @Valid
    @Size()
    private List<Scene> scenes = null;

    @JsonProperty("sub_type")
    private String subType = null;

    @JsonProperty("skills")
    @Valid
    @Size()
    private List<SkillReference> skills = null;

    @JsonProperty("skill_details")
    @Valid
    @Size()
    private List<AgentSkillDetail> skillDetails = null;

    @JsonProperty("writing_template")
    private String writingTemplate = null;

    public String getName() {
        return name;
    }

    public ModifyAgentReq setName(String name) {
        this.name = name;
        return this;
    }

    public TypeEnum getType() {
        return type;
    }

    public ModifyAgentReq setType(TypeEnum type) {
        this.type = type;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public ModifyAgentReq setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getIcon() {
        return icon;
    }

    public ModifyAgentReq setIcon(String icon) {
        this.icon = icon;
        return this;
    }

    public ControllerVO getDetails() {
        return details;
    }

    public ModifyAgentReq setDetails(ControllerVO details) {
        this.details = details;
        return this;
    }

    public String getInstructions() {
        return instructions;
    }

    public ModifyAgentReq setInstructions(String instructions) {
        this.instructions = instructions;
        return this;
    }

    public String getModelDeploymentId() {
        return modelDeploymentId;
    }

    public ModifyAgentReq setModelDeploymentId(String modelDeploymentId) {
        this.modelDeploymentId = modelDeploymentId;
        return this;
    }

    public String getModelName() {
        return modelName;
    }

    public ModifyAgentReq setModelName(String modelName) {
        this.modelName = modelName;
        return this;
    }

    public String getModelEndpoint() {
        return modelEndpoint;
    }

    public ModifyAgentReq setModelEndpoint(String modelEndpoint) {
        this.modelEndpoint = modelEndpoint;
        return this;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public ModifyAgentReq setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
        return this;
    }

    public ModelConfig getModelConfig() {
        return modelConfig;
    }

    public ModifyAgentReq setModelConfig(ModelConfig modelConfig) {
        this.modelConfig = modelConfig;
        return this;
    }

    public String getModelType() {
        return modelType;
    }

    public ModifyAgentReq setModelType(String modelType) {
        this.modelType = modelType;
        return this;
    }

    public ModifyAgentReq setStrategyFlag(Boolean strategyFlag) {
        this.strategyFlag = strategyFlag;
        return this;
    }

    public Boolean isStrategyFlag() {
        return strategyFlag;
    }

    public String getModel() {
        return model;
    }

    public ModifyAgentReq setModel(String model) {
        this.model = model;
        return this;
    }

    public ModifyAgentReq setPlanQaIndependent(Boolean planQaIndependent) {
        this.planQaIndependent = planQaIndependent;
        return this;
    }

    public Boolean isPlanQaIndependent() {
        return planQaIndependent;
    }

    public String getPlanModel() {
        return planModel;
    }

    public ModifyAgentReq setPlanModel(String planModel) {
        this.planModel = planModel;
        return this;
    }

    public String getPlanModelDeploymentId() {
        return planModelDeploymentId;
    }

    public ModifyAgentReq setPlanModelDeploymentId(String planModelDeploymentId) {
        this.planModelDeploymentId = planModelDeploymentId;
        return this;
    }

    public String getPlanModelName() {
        return planModelName;
    }

    public ModifyAgentReq setPlanModelName(String planModelName) {
        this.planModelName = planModelName;
        return this;
    }

    public ModelConfig getPlanModelConfig() {
        return planModelConfig;
    }

    public ModifyAgentReq setPlanModelConfig(ModelConfig planModelConfig) {
        this.planModelConfig = planModelConfig;
        return this;
    }

    public String getPlanModelType() {
        return planModelType;
    }

    public ModifyAgentReq setPlanModelType(String planModelType) {
        this.planModelType = planModelType;
        return this;
    }

    public List<String> getTools() {
        return tools;
    }

    public ModifyAgentReq setTools(List<String> tools) {
        this.tools = tools;
        return this;
    }

    public List<AgentToolDetail> getToolDetails() {
        return toolDetails;
    }

    public ModifyAgentReq setToolDetails(List<AgentToolDetail> toolDetails) {
        this.toolDetails = toolDetails;
        return this;
    }

    public List<String> getWorkflows() {
        return workflows;
    }

    public ModifyAgentReq setWorkflows(List<String> workflows) {
        this.workflows = workflows;
        return this;
    }

    public List<AgentWorkflowDetail> getWorkflowDetails() {
        return workflowDetails;
    }

    public ModifyAgentReq setWorkflowDetails(List<AgentWorkflowDetail> workflowDetails) {
        this.workflowDetails = workflowDetails;
        return this;
    }

    public List<String> getMcpServers() {
        return mcpServers;
    }

    public ModifyAgentReq setMcpServers(List<String> mcpServers) {
        this.mcpServers = mcpServers;
        return this;
    }

    public Map<String, List<String>> getMcpChooseTools() {
        return mcpChooseTools;
    }

    public ModifyAgentReq setMcpChooseTools(Map<String, List<String>> mcpChooseTools) {
        this.mcpChooseTools = mcpChooseTools;
        return this;
    }

    public List<AgentMcpDetail> getMcpDetails() {
        return mcpDetails;
    }

    public ModifyAgentReq setMcpDetails(List<AgentMcpDetail> mcpDetails) {
        this.mcpDetails = mcpDetails;
        return this;
    }

    public List<String> getKnowledgeRepos() {
        return knowledgeRepos;
    }

    public ModifyAgentReq setKnowledgeRepos(List<String> knowledgeRepos) {
        this.knowledgeRepos = knowledgeRepos;
        return this;
    }

    public List<TriggerConfig> getTriggerList() {
        return triggerList;
    }

    public ModifyAgentReq setTriggerList(List<TriggerConfig> triggerList) {
        this.triggerList = triggerList;
        return this;
    }

    public List<MemoryVariable> getMemoryVariables() {
        return memoryVariables;
    }

    public ModifyAgentReq setMemoryVariables(List<MemoryVariable> memoryVariables) {
        this.memoryVariables = memoryVariables;
        return this;
    }

    public String getPrologue() {
        return prologue;
    }

    public ModifyAgentReq setPrologue(String prologue) {
        this.prologue = prologue;
        return this;
    }

    public ModifyAgentReq setIsTemplate(Boolean isTemplate) {
        this.isTemplate = isTemplate;
        return this;
    }

    public Boolean isIsTemplate() {
        return isTemplate;
    }

    public SearchEngine getSearchEngine() {
        return searchEngine;
    }

    public ModifyAgentReq setSearchEngine(SearchEngine searchEngine) {
        this.searchEngine = searchEngine;
        return this;
    }

    public List<String> getSuggestQueries() {
        return suggestQueries;
    }

    public ModifyAgentReq setSuggestQueries(List<String> suggestQueries) {
        this.suggestQueries = suggestQueries;
        return this;
    }

    public AdditionalQuestionsConfig getAdditionalQuestionsConfig() {
        return additionalQuestionsConfig;
    }

    public ModifyAgentReq setAdditionalQuestionsConfig(AdditionalQuestionsConfig additionalQuestionsConfig) {
        this.additionalQuestionsConfig = additionalQuestionsConfig;
        return this;
    }

    public VoiceInteraction getVoiceInteraction() {
        return voiceInteraction;
    }

    public ModifyAgentReq setVoiceInteraction(VoiceInteraction voiceInteraction) {
        this.voiceInteraction = voiceInteraction;
        return this;
    }

    public KnowledgeRetrievePolicy getKnowledgeRetrievePolicy() {
        return knowledgeRetrievePolicy;
    }

    public ModifyAgentReq setKnowledgeRetrievePolicy(KnowledgeRetrievePolicy knowledgeRetrievePolicy) {
        this.knowledgeRetrievePolicy = knowledgeRetrievePolicy;
        return this;
    }

    public List<AgentVariable> getAgentVariables() {
        return agentVariables;
    }

    public ModifyAgentReq setAgentVariables(List<AgentVariable> agentVariables) {
        this.agentVariables = agentVariables;
        return this;
    }

    public List<InputVariable> getInputVariables() {
        return inputVariables;
    }

    public ModifyAgentReq setInputVariables(List<InputVariable> inputVariables) {
        this.inputVariables = inputVariables;
        return this;
    }

    public AgentMemoryConfig getMemoryConfig() {
        return memoryConfig;
    }

    public ModifyAgentReq setMemoryConfig(AgentMemoryConfig memoryConfig) {
        this.memoryConfig = memoryConfig;
        return this;
    }

    public Map<String, Object> getContentReview() {
        return contentReview;
    }

    public ModifyAgentReq setContentReview(Map<String, Object> contentReview) {
        this.contentReview = contentReview;
        return this;
    }

    public ModifyAgentReq setSafetyBarrier(Boolean safetyBarrier) {
        this.safetyBarrier = safetyBarrier;
        return this;
    }

    public Boolean isSafetyBarrier() {
        return safetyBarrier;
    }

    public Integer getStatus() {
        return status;
    }

    public ModifyAgentReq setStatus(Integer status) {
        this.status = status;
        return this;
    }

    public String getCreator() {
        return creator;
    }

    public ModifyAgentReq setCreator(String creator) {
        this.creator = creator;
        return this;
    }

    public SchedulingModeEnum getSchedulingMode() {
        return schedulingMode;
    }

    public ModifyAgentReq setSchedulingMode(SchedulingModeEnum schedulingMode) {
        this.schedulingMode = schedulingMode;
        return this;
    }

    public ModifyAgentReq setWorkflowSwitchEnabled(Boolean workflowSwitchEnabled) {
        this.workflowSwitchEnabled = workflowSwitchEnabled;
        return this;
    }

    public Boolean isWorkflowSwitchEnabled() {
        return workflowSwitchEnabled;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public ModifyAgentReq setCreateTime(Date createTime) {
        this.createTime = createTime;
        return this;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public ModifyAgentReq setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
        return this;
    }

    public Date getPublishTime() {
        return publishTime;
    }

    public ModifyAgentReq setPublishTime(Date publishTime) {
        this.publishTime = publishTime;
        return this;
    }

    public Reference getReference() {
        return reference;
    }

    public ModifyAgentReq setReference(Reference reference) {
        this.reference = reference;
        return this;
    }

    public List<Scene> getScenes() {
        return scenes;
    }

    public ModifyAgentReq setScenes(List<Scene> scenes) {
        this.scenes = scenes;
        return this;
    }

    public String getSubType() {
        return subType;
    }

    public ModifyAgentReq setSubType(String subType) {
        this.subType = subType;
        return this;
    }

    public List<SkillReference> getSkills() {
        return skills;
    }

    public ModifyAgentReq setSkills(List<SkillReference> skills) {
        this.skills = skills;
        return this;
    }

    public List<AgentSkillDetail> getSkillDetails() {
        return skillDetails;
    }

    public ModifyAgentReq setSkillDetails(List<AgentSkillDetail> skillDetails) {
        this.skillDetails = skillDetails;
        return this;
    }

    public ModifyAgentReq setWritingTemplate(String writingTemplate) {
        this.writingTemplate = writingTemplate;
        return this;
    }
    public String getWritingTemplate() {
        return writingTemplate;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ModifyAgentReq {\n");

        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    icon: ").append(toIndentedString(icon)).append("\n");
        sb.append("    details: ").append(toIndentedString(details)).append("\n");
        sb.append("    instructions: ").append(toIndentedString(instructions)).append("\n");
        sb.append("    modelDeploymentId: ").append(toIndentedString(modelDeploymentId)).append("\n");
        sb.append("    modelName: ").append(toIndentedString(modelName)).append("\n");
        sb.append("    modelEndpoint: ").append(toIndentedString(modelEndpoint)).append("\n");
        sb.append("    modelVersion: ").append(toIndentedString(modelVersion)).append("\n");
        sb.append("    modelConfig: ").append(toIndentedString(modelConfig)).append("\n");
        sb.append("    modelType: ").append(toIndentedString(modelType)).append("\n");
        sb.append("    strategyFlag: ").append(toIndentedString(strategyFlag)).append("\n");
        sb.append("    model: ").append(toIndentedString(model)).append("\n");
        sb.append("    planQaIndependent: ").append(toIndentedString(planQaIndependent)).append("\n");
        sb.append("    planModel: ").append(toIndentedString(planModel)).append("\n");
        sb.append("    planModelDeploymentId: ").append(toIndentedString(planModelDeploymentId)).append("\n");
        sb.append("    planModelName: ").append(toIndentedString(planModelName)).append("\n");
        sb.append("    planModelConfig: ").append(toIndentedString(planModelConfig)).append("\n");
        sb.append("    planModelType: ").append(toIndentedString(planModelType)).append("\n");
        sb.append("    tools: ").append(toIndentedString(tools)).append("\n");
        sb.append("    toolDetails: ").append(toIndentedString(toolDetails)).append("\n");
        sb.append("    workflows: ").append(toIndentedString(workflows)).append("\n");
        sb.append("    workflowDetails: ").append(toIndentedString(workflowDetails)).append("\n");
        sb.append("    mcpServers: ").append(toIndentedString(mcpServers)).append("\n");
        sb.append("    mcpChooseTools: ").append(toIndentedString(mcpChooseTools)).append("\n");
        sb.append("    mcpDetails: ").append(toIndentedString(mcpDetails)).append("\n");
        sb.append("    knowledgeRepos: ").append(toIndentedString(knowledgeRepos)).append("\n");
        sb.append("    triggerList: ").append(toIndentedString(triggerList)).append("\n");
        sb.append("    memoryVariables: ").append(toIndentedString(memoryVariables)).append("\n");
        sb.append("    prologue: ").append(toIndentedString(prologue)).append("\n");
        sb.append("    isTemplate: ").append(toIndentedString(isTemplate)).append("\n");
        sb.append("    searchEngine: ").append(toIndentedString(searchEngine)).append("\n");
        sb.append("    suggestQueries: ").append(toIndentedString(suggestQueries)).append("\n");
        sb.append("    additionalQuestionsConfig: ").append(toIndentedString(additionalQuestionsConfig)).append("\n");
        sb.append("    voiceInteraction: ").append(toIndentedString(voiceInteraction)).append("\n");
        sb.append("    knowledgeRetrievePolicy: ").append(toIndentedString(knowledgeRetrievePolicy)).append("\n");
        sb.append("    agentVariables: ").append(toIndentedString(agentVariables)).append("\n");
        sb.append("    inputVariables: ").append(toIndentedString(inputVariables)).append("\n");
        sb.append("    memoryConfig: ").append(toIndentedString(memoryConfig)).append("\n");
        sb.append("    contentReview: ").append(toIndentedString(contentReview)).append("\n");
        sb.append("    safetyBarrier: ").append(toIndentedString(safetyBarrier)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    creator: ").append(toIndentedString(creator)).append("\n");
        sb.append("    schedulingMode: ").append(toIndentedString(schedulingMode)).append("\n");
        sb.append("    workflowSwitchEnabled: ").append(toIndentedString(workflowSwitchEnabled)).append("\n");
        sb.append("    createTime: ").append(toIndentedString(createTime)).append("\n");
        sb.append("    updateTime: ").append(toIndentedString(updateTime)).append("\n");
        sb.append("    publishTime: ").append(toIndentedString(publishTime)).append("\n");
        sb.append("    reference: ").append(toIndentedString(reference)).append("\n");
        sb.append("    scenes: ").append(toIndentedString(scenes)).append("\n");
        sb.append("    subType: ").append(toIndentedString(subType)).append("\n");
        sb.append("    skills: ").append(toIndentedString(skills)).append("\n");
        sb.append("    skillDetails: ").append(toIndentedString(skillDetails)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ModifyAgentReq modifyAgentReq = (ModifyAgentReq) o;
        return Objects.equals(this.name, modifyAgentReq.name) && Objects.equals(this.type, modifyAgentReq.type)
            && Objects.equals(this.description, modifyAgentReq.description) && Objects.equals(this.icon,
            modifyAgentReq.icon) && Objects.equals(this.details, modifyAgentReq.details) && Objects.equals(
            this.instructions, modifyAgentReq.instructions) && Objects.equals(this.modelDeploymentId,
            modifyAgentReq.modelDeploymentId) && Objects.equals(this.modelName, modifyAgentReq.modelName)
            && Objects.equals(this.modelEndpoint, modifyAgentReq.modelEndpoint) && Objects.equals(this.modelVersion,
            modifyAgentReq.modelVersion) && Objects.equals(this.modelConfig, modifyAgentReq.modelConfig)
            && Objects.equals(this.modelType, modifyAgentReq.modelType) && Objects.equals(this.strategyFlag,
            modifyAgentReq.strategyFlag) && Objects.equals(this.model, modifyAgentReq.model) && Objects.equals(
            this.planQaIndependent, modifyAgentReq.planQaIndependent) && Objects.equals(this.planModel,
            modifyAgentReq.planModel) && Objects.equals(this.planModelDeploymentId,
            modifyAgentReq.planModelDeploymentId) && Objects.equals(this.planModelName, modifyAgentReq.planModelName)
            && Objects.equals(this.planModelConfig, modifyAgentReq.planModelConfig) && Objects.equals(
            this.planModelType, modifyAgentReq.planModelType) && Objects.equals(this.tools, modifyAgentReq.tools)
            && Objects.equals(this.toolDetails, modifyAgentReq.toolDetails) && Objects.equals(this.workflows,
            modifyAgentReq.workflows) && Objects.equals(this.workflowDetails, modifyAgentReq.workflowDetails)
            && Objects.equals(this.mcpServers, modifyAgentReq.mcpServers) && Objects.equals(this.mcpChooseTools,
            modifyAgentReq.mcpChooseTools) && Objects.equals(this.mcpDetails, modifyAgentReq.mcpDetails)
            && Objects.equals(this.knowledgeRepos, modifyAgentReq.knowledgeRepos) && Objects.equals(this.triggerList,
            modifyAgentReq.triggerList) && Objects.equals(this.memoryVariables, modifyAgentReq.memoryVariables)
            && Objects.equals(this.prologue, modifyAgentReq.prologue) && Objects.equals(this.isTemplate,
            modifyAgentReq.isTemplate) && Objects.equals(this.searchEngine, modifyAgentReq.searchEngine)
            && Objects.equals(this.suggestQueries, modifyAgentReq.suggestQueries) && Objects.equals(
            this.additionalQuestionsConfig, modifyAgentReq.additionalQuestionsConfig) && Objects.equals(
            this.voiceInteraction, modifyAgentReq.voiceInteraction) && Objects.equals(this.knowledgeRetrievePolicy,
            modifyAgentReq.knowledgeRetrievePolicy) && Objects.equals(this.agentVariables,
            modifyAgentReq.agentVariables) && Objects.equals(this.inputVariables, modifyAgentReq.inputVariables)
            && Objects.equals(this.memoryConfig, modifyAgentReq.memoryConfig) && Objects.equals(this.contentReview,
            modifyAgentReq.contentReview) && Objects.equals(this.safetyBarrier, modifyAgentReq.safetyBarrier)
            && Objects.equals(this.status, modifyAgentReq.status) && Objects.equals(this.creator,
            modifyAgentReq.creator) && Objects.equals(this.schedulingMode, modifyAgentReq.schedulingMode)
            && Objects.equals(this.workflowSwitchEnabled, modifyAgentReq.workflowSwitchEnabled) && Objects.equals(
            this.createTime, modifyAgentReq.createTime) && Objects.equals(this.updateTime, modifyAgentReq.updateTime)
            && Objects.equals(this.publishTime, modifyAgentReq.publishTime) && Objects.equals(this.reference,
            modifyAgentReq.reference) && Objects.equals(this.scenes, modifyAgentReq.scenes) && Objects.equals(
            this.subType, modifyAgentReq.subType) && Objects.equals(this.skills, modifyAgentReq.skills)
            && Objects.equals(this.skillDetails, modifyAgentReq.skillDetails);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, description, icon, details, instructions, modelDeploymentId, modelName,
            modelEndpoint, modelVersion, modelConfig, modelType, strategyFlag, model, planQaIndependent, planModel,
            planModelDeploymentId, planModelName, planModelConfig, planModelType, tools, toolDetails, workflows,
            workflowDetails, mcpServers, mcpChooseTools, mcpDetails, knowledgeRepos, triggerList, memoryVariables,
            prologue, isTemplate, searchEngine, suggestQueries, additionalQuestionsConfig, voiceInteraction,
            knowledgeRetrievePolicy, agentVariables, inputVariables, memoryConfig, contentReview, safetyBarrier, status,
            creator, schedulingMode, workflowSwitchEnabled, createTime, updateTime, publishTime, reference, scenes,
            subType, skills, skillDetails);
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

    /**
     * 智能体类型。
     */
    public enum TypeEnum {
        AGENT("agent"),

        CONTROLLER("controller"),

        PLANEXECUTE("planexecute");

        private String value;

        TypeEnum(String value) {
            this.value = value;
        }

        @JsonCreator
        public static TypeEnum fromValue(String text) {
            for (TypeEnum b : TypeEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }
    }

    /**
     * 智能体调度模式，默认取值ReAct，表示模型优先；可选RAG，表示知识库优先；ToolUse，表示工具优先。
     */
    public enum SchedulingModeEnum {
        REACT("ReAct"),

        RAG("RAG"),

        TOOLUSE("ToolUse");

        private String value;

        SchedulingModeEnum(String value) {
            this.value = value;
        }

        @JsonCreator
        public static SchedulingModeEnum fromValue(String text) {
            for (SchedulingModeEnum b : SchedulingModeEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }
    }
}
