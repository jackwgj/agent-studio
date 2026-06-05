/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * 工作流节点config对象。
 */
@ApiModel(description = "工作流节点config对象。")

@Validated

public class SubControllerNodeConfigVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private String id = null;

    @JsonProperty("intent")
    @Valid
    private WorkflowNodeConfigVOIntent intent = null;

    @JsonProperty("name")
    private String name = null;

    @JsonProperty("version_id")
    private String versionId = null;

    @JsonProperty("version_name")
    private String versionName = null;

    @JsonProperty("description")
    private String description = null;

    @JsonProperty("model")
    @Valid
    private ModelConfigVO model = null;

    @JsonProperty("top_p")
    private Float topP = null;

    @JsonProperty("temperature")
    private Float temperature = null;

    @JsonProperty("enable_history")
    private Boolean enableHistory = null;

    @JsonProperty("max_iteration")
    private Integer maxIteration = null;

    @JsonProperty("workflows")
    @Valid
    @Size()
    private List<ControllerNodeConfigVOWorkflows> workflows = null;

    @JsonProperty("agents")
    @Valid
    @Size()
    private List<SubControllerNodeConfigVOAgents> agents = null;

    @JsonProperty("specify_workflow_order")
    private Boolean specifyWorkflowOrder = null;

    @JsonProperty("global_intents")
    @Valid
    @Size()
    private List<ControllerNodeConfigVOGlobalIntents> globalIntents = null;

    @JsonProperty("prompt")
    private String prompt = null;

    @JsonProperty("chat_history_max_turn")
    private Integer chatHistoryMaxTurn = null;

    public String getId() {
        return id;
    }

    public SubControllerNodeConfigVO setId(String id) {
        this.id = id;
        return this;
    }

    public WorkflowNodeConfigVOIntent getIntent() {
        return intent;
    }

    public SubControllerNodeConfigVO setIntent(WorkflowNodeConfigVOIntent intent) {
        this.intent = intent;
        return this;
    }

    public String getName() {
        return name;
    }

    public SubControllerNodeConfigVO setName(String name) {
        this.name = name;
        return this;
    }

    public String getVersionId() {
        return versionId;
    }

    public SubControllerNodeConfigVO setVersionId(String versionId) {
        this.versionId = versionId;
        return this;
    }

    public String getVersionName() {
        return versionName;
    }

    public SubControllerNodeConfigVO setVersionName(String versionName) {
        this.versionName = versionName;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public SubControllerNodeConfigVO setDescription(String description) {
        this.description = description;
        return this;
    }

    public ModelConfigVO getModel() {
        return model;
    }

    public SubControllerNodeConfigVO setModel(ModelConfigVO model) {
        this.model = model;
        return this;
    }

    public Float getTopP() {
        return topP;
    }

    public SubControllerNodeConfigVO setTopP(Float topP) {
        this.topP = topP;
        return this;
    }

    public Float getTemperature() {
        return temperature;
    }

    public SubControllerNodeConfigVO setTemperature(Float temperature) {
        this.temperature = temperature;
        return this;
    }

    public SubControllerNodeConfigVO setEnableHistory(Boolean enableHistory) {
        this.enableHistory = enableHistory;
        return this;
    }

    public Boolean isEnableHistory() {
        return enableHistory;
    }

    public Integer getMaxIteration() {
        return maxIteration;
    }

    public SubControllerNodeConfigVO setMaxIteration(Integer maxIteration) {
        this.maxIteration = maxIteration;
        return this;
    }

    public List<ControllerNodeConfigVOWorkflows> getWorkflows() {
        return workflows;
    }

    public SubControllerNodeConfigVO setWorkflows(List<ControllerNodeConfigVOWorkflows> workflows) {
        this.workflows = workflows;
        return this;
    }

    public List<SubControllerNodeConfigVOAgents> getAgents() {
        return agents;
    }

    public SubControllerNodeConfigVO setAgents(List<SubControllerNodeConfigVOAgents> agents) {
        this.agents = agents;
        return this;
    }

    public SubControllerNodeConfigVO setSpecifyWorkflowOrder(Boolean specifyWorkflowOrder) {
        this.specifyWorkflowOrder = specifyWorkflowOrder;
        return this;
    }

    public Boolean isSpecifyWorkflowOrder() {
        return specifyWorkflowOrder;
    }

    public List<ControllerNodeConfigVOGlobalIntents> getGlobalIntents() {
        return globalIntents;
    }

    public SubControllerNodeConfigVO setGlobalIntents(List<ControllerNodeConfigVOGlobalIntents> globalIntents) {
        this.globalIntents = globalIntents;
        return this;
    }

    public String getPrompt() {
        return prompt;
    }

    public SubControllerNodeConfigVO setPrompt(String prompt) {
        this.prompt = prompt;
        return this;
    }

    public Integer getChatHistoryMaxTurn() {
        return chatHistoryMaxTurn;
    }

    public SubControllerNodeConfigVO setChatHistoryMaxTurn(Integer chatHistoryMaxTurn) {
        this.chatHistoryMaxTurn = chatHistoryMaxTurn;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class SubControllerNodeConfigVO {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    intent: ").append(toIndentedString(intent)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    versionId: ").append(toIndentedString(versionId)).append("\n");
        sb.append("    versionName: ").append(toIndentedString(versionName)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    model: ").append(toIndentedString(model)).append("\n");
        sb.append("    topP: ").append(toIndentedString(topP)).append("\n");
        sb.append("    temperature: ").append(toIndentedString(temperature)).append("\n");
        sb.append("    enableHistory: ").append(toIndentedString(enableHistory)).append("\n");
        sb.append("    maxIteration: ").append(toIndentedString(maxIteration)).append("\n");
        sb.append("    workflows: ").append(toIndentedString(workflows)).append("\n");
        sb.append("    agents: ").append(toIndentedString(agents)).append("\n");
        sb.append("    specifyWorkflowOrder: ").append(toIndentedString(specifyWorkflowOrder)).append("\n");
        sb.append("    globalIntents: ").append(toIndentedString(globalIntents)).append("\n");
        sb.append("    prompt: ").append(toIndentedString(prompt)).append("\n");
        sb.append("    chatHistoryMaxTurn: ").append(toIndentedString(chatHistoryMaxTurn)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SubControllerNodeConfigVO subControllerNodeConfigVO = (SubControllerNodeConfigVO) o;
        return Objects.equals(this.id, subControllerNodeConfigVO.id) && Objects.equals(this.intent,
            subControllerNodeConfigVO.intent) && Objects.equals(this.name, subControllerNodeConfigVO.name)
            && Objects.equals(this.versionId, subControllerNodeConfigVO.versionId) && Objects.equals(this.versionName,
            subControllerNodeConfigVO.versionName) && Objects.equals(this.description,
            subControllerNodeConfigVO.description) && Objects.equals(this.model, subControllerNodeConfigVO.model)
            && Objects.equals(this.topP, subControllerNodeConfigVO.topP) && Objects.equals(this.temperature,
            subControllerNodeConfigVO.temperature) && Objects.equals(this.enableHistory,
            subControllerNodeConfigVO.enableHistory) && Objects.equals(this.maxIteration,
            subControllerNodeConfigVO.maxIteration) && Objects.equals(this.workflows,
            subControllerNodeConfigVO.workflows) && Objects.equals(this.agents, subControllerNodeConfigVO.agents)
            && Objects.equals(this.specifyWorkflowOrder, subControllerNodeConfigVO.specifyWorkflowOrder)
            && Objects.equals(this.globalIntents, subControllerNodeConfigVO.globalIntents) && Objects.equals(
            this.prompt, subControllerNodeConfigVO.prompt) && Objects.equals(this.chatHistoryMaxTurn,
            subControllerNodeConfigVO.chatHistoryMaxTurn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, intent, name, versionId, versionName, description, model, topP, temperature,
            enableHistory, maxIteration, workflows, agents, specifyWorkflowOrder, globalIntents, prompt,
            chatHistoryMaxTurn);
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
