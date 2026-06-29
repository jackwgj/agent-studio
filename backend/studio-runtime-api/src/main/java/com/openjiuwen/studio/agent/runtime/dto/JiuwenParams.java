/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * JiuwenParams
 */

@Validated

public class JiuwenParams implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("conversationHistory")
    @Valid
    @Size()
    private List<ConversationHistory> conversationHistory = null;

    @JsonProperty("pluginConfigs")
    @Valid
    @Size()
    private List<JiuwenPluginConfig> pluginConfigs = null;

    @JsonProperty("globalVariables")
    @Valid
    @Size()
    private Map<String, Object> globalVariables = null;

    @JsonProperty("llmExtraConfigs")
    @Valid
    private JiuwenParamsLlmExtraConfigs llmExtraConfigs = null;

    @JsonProperty("toolSwitchDict")
    @Valid
    @Size()
    private Map<String, Boolean> toolSwitchDict = null;

    @JsonProperty("workflowSequence")
    @Valid
    @Size()
    private List<@Length() String> workflowSequence = null;

    @JsonProperty("activeWorkflows")
    @Valid
    @Size()
    private List<@Length() String> activeWorkflows = null;

    @JsonProperty("intent")
    private String intent = null;

    @JsonProperty("files")
    @Valid
    @Size()
    private List<Object> files = null;

    @JsonProperty("enableHistory")
    private Boolean enableHistory = null;

    @JsonProperty("environmentVariables")
    @Valid
    @Size()
    private Map<String, Object> environmentVariables = null;

    @JsonProperty("secretEnvKeys")
    private List<String> secretEnvKeys = null;

    @JsonProperty("appId")
    private String appId = null;

    @JsonProperty("enableMemoryRetrieve")
    private Boolean enableMemoryRetrieve = false;

    @JsonProperty("enableMemoryExtract")
    private Boolean enableMemoryExtract = false;

    @JsonProperty("sysOperationCard")
    @Valid
    private JiuwenParamsSysOperationCard sysOperationCard = null;

    @JsonProperty("message")
    private String message = null;

    @JsonProperty("interruptFeedback")
    private String interruptFeedback = null;

    @JsonProperty("fileUrl")
    private String fileUrl = null;

    @JsonProperty("fileName")
    private String fileName = null;

    @JsonProperty("isTemplate")
    private Boolean isTemplate = false;

    public JiuwenParams setInterruptFeedback(String interruptFeedback) {
        this.interruptFeedback = interruptFeedback;
        return this;
    }
    public String getInterruptFeedback() {
        return interruptFeedback;
    }

    public JiuwenParams setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
        return this;
    }
    public String getFileUrl() {
        return fileUrl;
    }

    public JiuwenParams setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }
    public String getFileName() {
        return fileName;
    }

    public JiuwenParams setIsTemplate(Boolean isTemplate) {
        this.isTemplate = isTemplate;
        return this;
    }
    public Boolean isIsTemplate() {
        return isTemplate;
    }


    public List<ConversationHistory> getConversationHistory() {
        return conversationHistory;
    }

    public JiuwenParams setConversationHistory(List<ConversationHistory> conversationHistory) {
        this.conversationHistory = conversationHistory;
        return this;
    }

    public List<JiuwenPluginConfig> getPluginConfigs() {
        return pluginConfigs;
    }

    public JiuwenParams setPluginConfigs(List<JiuwenPluginConfig> pluginConfigs) {
        this.pluginConfigs = pluginConfigs;
        return this;
    }

    public Map<String, Object> getGlobalVariables() {
        return globalVariables;
    }

    public JiuwenParams setGlobalVariables(Map<String, Object> globalVariables) {
        this.globalVariables = globalVariables;
        return this;
    }

    public JiuwenParamsLlmExtraConfigs getLlmExtraConfigs() {
        return llmExtraConfigs;
    }

    public JiuwenParams setLlmExtraConfigs(JiuwenParamsLlmExtraConfigs llmExtraConfigs) {
        this.llmExtraConfigs = llmExtraConfigs;
        return this;
    }

    public Map<String, Boolean> getToolSwitchDict() {
        return toolSwitchDict;
    }

    public JiuwenParams setToolSwitchDict(Map<String, Boolean> toolSwitchDict) {
        this.toolSwitchDict = toolSwitchDict;
        return this;
    }

    public List<String> getWorkflowSequence() {
        return workflowSequence;
    }

    public JiuwenParams setWorkflowSequence(List<String> workflowSequence) {
        this.workflowSequence = workflowSequence;
        return this;
    }

    public List<String> getActiveWorkflows() {
        return activeWorkflows;
    }

    public JiuwenParams setActiveWorkflows(List<String> activeWorkflows) {
        this.activeWorkflows = activeWorkflows;
        return this;
    }

    public String getIntent() {
        return intent;
    }

    public JiuwenParams setIntent(String intent) {
        this.intent = intent;
        return this;
    }

    public List<Object> getFiles() {
        return files;
    }

    public JiuwenParams setFiles(List<Object> files) {
        this.files = files;
        return this;
    }

    public JiuwenParams setEnableHistory(Boolean enableHistory) {
        this.enableHistory = enableHistory;
        return this;
    }

    public Boolean isEnableHistory() {
        return enableHistory;
    }

    public Map<String, Object> getEnvironmentVariables() {
        return environmentVariables;
    }

    public JiuwenParams setEnvironmentVariables(Map<String, Object> environmentVariables) {
        this.environmentVariables = environmentVariables;
        return this;
    }

    public List<String> getSecretEnvKeys() {
        return secretEnvKeys;
    }

    public JiuwenParams setSecretEnvKeys(List<String> secretEnvKeys) {
        this.secretEnvKeys = secretEnvKeys;
        return this;
    }

    public String getAppId() {
        return appId;
    }

    public JiuwenParams setAppId(String appId) {
        this.appId = appId;
        return this;
    }

    public JiuwenParams setEnableMemoryRetrieve(Boolean enableMemoryRetrieve) {
        this.enableMemoryRetrieve = enableMemoryRetrieve;
        return this;
    }

    public Boolean isEnableMemoryRetrieve() {
        return enableMemoryRetrieve;
    }

    public JiuwenParams setEnableMemoryExtract(Boolean enableMemoryExtract) {
        this.enableMemoryExtract = enableMemoryExtract;
        return this;
    }

    public Boolean isEnableMemoryExtract() {
        return enableMemoryExtract;
    }

    public JiuwenParamsSysOperationCard getSysOperationCard() {
        return sysOperationCard;
    }

    public JiuwenParams setSysOperationCard(JiuwenParamsSysOperationCard sysOperationCard) {
        this.sysOperationCard = sysOperationCard;
        return this;
    }

    public JiuwenParams setMessage(String message) {
        this.message = message;
        return this;
    }
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class JiuwenParams {\n");

        sb.append("    conversationHistory: ").append(toIndentedString(conversationHistory)).append("\n");
        sb.append("    pluginConfigs: ").append(toIndentedString(pluginConfigs)).append("\n");
        sb.append("    globalVariables: ").append(toIndentedString(globalVariables)).append("\n");
        sb.append("    llmExtraConfigs: ").append(toIndentedString(llmExtraConfigs)).append("\n");
        sb.append("    toolSwitchDict: ").append(toIndentedString(toolSwitchDict)).append("\n");
        sb.append("    workflowSequence: ").append(toIndentedString(workflowSequence)).append("\n");
        sb.append("    activeWorkflows: ").append(toIndentedString(activeWorkflows)).append("\n");
        sb.append("    intent: ").append(toIndentedString(intent)).append("\n");
        sb.append("    files: ").append(toIndentedString(files)).append("\n");
        sb.append("    enableHistory: ").append(toIndentedString(enableHistory)).append("\n");
        sb.append("    environmentVariables: ").append(toIndentedString(environmentVariables)).append("\n");
        sb.append("    secretEnvKeys: ").append(toIndentedString(secretEnvKeys)).append("\n");
        sb.append("    appId: ").append(toIndentedString(appId)).append("\n");
        sb.append("    enableMemoryRetrieve: ").append(toIndentedString(enableMemoryRetrieve)).append("\n");
        sb.append("    enableMemoryExtract: ").append(toIndentedString(enableMemoryExtract)).append("\n");
        sb.append("    sysOperationCard: ").append(toIndentedString(sysOperationCard)).append("\n");
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
        JiuwenParams jiuwenParams = (JiuwenParams) o;
        return Objects.equals(this.conversationHistory, jiuwenParams.conversationHistory) && Objects.equals(
            this.pluginConfigs, jiuwenParams.pluginConfigs) && Objects.equals(this.globalVariables,
            jiuwenParams.globalVariables) && Objects.equals(this.llmExtraConfigs, jiuwenParams.llmExtraConfigs)
            && Objects.equals(this.toolSwitchDict, jiuwenParams.toolSwitchDict) && Objects.equals(this.workflowSequence,
            jiuwenParams.workflowSequence) && Objects.equals(this.activeWorkflows, jiuwenParams.activeWorkflows)
            && Objects.equals(this.intent, jiuwenParams.intent) && Objects.equals(this.files, jiuwenParams.files)
            && Objects.equals(this.enableHistory, jiuwenParams.enableHistory) && Objects.equals(
            this.environmentVariables, jiuwenParams.environmentVariables) && Objects.equals(this.secretEnvKeys,
            jiuwenParams.secretEnvKeys) && Objects.equals(this.appId,
            jiuwenParams.appId) && Objects.equals(this.enableMemoryRetrieve, jiuwenParams.enableMemoryRetrieve)
            && Objects.equals(this.enableMemoryExtract, jiuwenParams.enableMemoryExtract) && Objects.equals(
            this.sysOperationCard, jiuwenParams.sysOperationCard);
    }

    @Override
    public int hashCode() {
        return Objects.hash(conversationHistory, pluginConfigs, globalVariables, llmExtraConfigs, toolSwitchDict,
            workflowSequence, activeWorkflows, intent, files, enableHistory, environmentVariables, secretEnvKeys, appId,
            enableMemoryRetrieve, enableMemoryExtract, sysOperationCard);
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
}
