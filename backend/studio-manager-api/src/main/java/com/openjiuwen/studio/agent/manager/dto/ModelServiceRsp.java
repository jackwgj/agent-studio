/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Pattern;

import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Objects;

/**
 * ModelServiceRsp
 */

@Validated

public class ModelServiceRsp implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    @Pattern(regexp = "^[a-zA-Z0-9_-]{1,80}$")
    private String id = null;

    @JsonProperty("provider_id")
    @Pattern(regexp = "^[a-zA-Z0-9_-]{1,80}$")
    private String providerId = null;

    @JsonProperty("type")
    private String type = null;

    @JsonProperty("service_name")
    @Pattern(
        regexp = "^[\\u4e00-\\u9fa5a-zA-Z0-9](?:[\\u4e00-\\u9fa5a-zA-Z0-9_.\\/:|\\ -]{0,62}[\\u4e00-\\u9fa5a-zA-Z0-9_-])?$")
    private String serviceName = null;

    @JsonProperty("service_key")
    private String serviceKey = null;

    @JsonProperty("model_name")
    @Pattern(
        regexp = "^[\\u4e00-\\u9fa5a-zA-Z0-9](?:[\\u4e00-\\u9fa5a-zA-Z0-9_.\\/:|\\ -]{0,62}[\\u4e00-\\u9fa5a-zA-Z0-9_-])?$")
    private String modelName = null;

    @JsonProperty("logo")
    private String logo = null;

    @JsonProperty("provider_name")
    private String providerName = null;

    @JsonProperty("provider_name_en")
    private String providerNameEn = null;

    @JsonProperty("is_reasoning")
    private Boolean isReasoning = null;

    @JsonProperty("is_support_close_reasoning")
    private Boolean isSupportCloseReasoning = null;

    @JsonProperty("is_network")
    private Boolean isNetwork = null;

    @JsonProperty("is_public")
    private Boolean isPublic = null;

    @JsonProperty("model_type")
    private String modelType = null;

    @JsonProperty("model_tags")
    private String modelTags = null;

    @JsonProperty("model_description")
    private String modelDescription = null;

    @JsonProperty("model_description_en")
    private String modelDescriptionEn = null;

    @JsonProperty("model_deploy_type")
    private String modelDeployType = null;

    @JsonProperty("domain_id")
    private String domainId = null;

    @JsonProperty("project_id")
    @Pattern(regexp = "^[a-zA-Z0-9_-]{1,40}$")
    private String projectId = null;

    @JsonProperty("workspace_id")
    @Pattern(regexp = "^[a-zA-Z0-9_-]{1,40}$")
    private String workspaceId = null;

    @JsonProperty("created_by_user")
    private String createdByUser = null;

    @JsonProperty("created_date")
    private Long createdDate = null;

    @JsonProperty("last_updated_date")
    private Long lastUpdatedDate = null;

    @JsonProperty("api_url")
    private String apiUrl = null;

    @JsonProperty("is_support_function")
    private Boolean isSupportFunction = null;

    @JsonProperty("interface_protocol")
    private String interfaceProtocol = null;

    @JsonProperty("auth_metadata_id")
    private String authMetadataId = null;

    @JsonProperty("auth_id")
    private String authId = null;

    @JsonProperty("publish_status")
    private String publishStatus = null;

    @JsonProperty("is_support_stream")
    private Boolean isSupportStream = null;

    @JsonProperty("throttling_policy")
    private Integer throttlingPolicy = null;

    @JsonProperty("status")
    private String status = null;

    @JsonProperty("disclaimer")
    private String disclaimer = null;

    @JsonProperty("is_subscribed")
    private Boolean isSubscribed = null;

    @JsonProperty("context_length")
    private String contextLength = null;

    @JsonProperty("model_series")
    private String modelSeries = null;

    @JsonProperty("is_in_free_trial")
    private Boolean isInFreeTrial = false;

    @JsonProperty("template_id")
    private String templateId = null;

    @JsonProperty("is_support_deep_thinking")
    private Boolean isSupportDeepThinking = null;

    @JsonProperty("is_preparing_for_offline")
    private Boolean isPreparingForOffline = false;

    @JsonProperty("resident_model_id")
    private String residentModelId = null;

    @JsonProperty("is_auth_configured")
    private Boolean isAuthConfigured = null;

    public String getId() {
        return id;
    }

    public ModelServiceRsp setId(String id) {
        this.id = id;
        return this;
    }

    public String getProviderId() {
        return providerId;
    }

    public ModelServiceRsp setProviderId(String providerId) {
        this.providerId = providerId;
        return this;
    }

    public String getType() {
        return type;
    }

    public ModelServiceRsp setType(String type) {
        this.type = type;
        return this;
    }

    public String getServiceName() {
        return serviceName;
    }

    public ModelServiceRsp setServiceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    public String getServiceKey() {
        return serviceKey;
    }

    public ModelServiceRsp setServiceKey(String serviceKey) {
        this.serviceKey = serviceKey;
        return this;
    }

    public String getModelName() {
        return modelName;
    }

    public ModelServiceRsp setModelName(String modelName) {
        this.modelName = modelName;
        return this;
    }

    public String getLogo() {
        return logo;
    }

    public ModelServiceRsp setLogo(String logo) {
        this.logo = logo;
        return this;
    }

    public String getProviderName() {
        return providerName;
    }

    public ModelServiceRsp setProviderName(String providerName) {
        this.providerName = providerName;
        return this;
    }

    public String getProviderNameEn() {
        return providerNameEn;
    }

    public ModelServiceRsp setProviderNameEn(String providerNameEn) {
        this.providerNameEn = providerNameEn;
        return this;
    }

    public ModelServiceRsp setIsReasoning(Boolean isReasoning) {
        this.isReasoning = isReasoning;
        return this;
    }

    public Boolean isIsReasoning() {
        return isReasoning;
    }

    public ModelServiceRsp setIsSupportCloseReasoning(Boolean isSupportCloseReasoning) {
        this.isSupportCloseReasoning = isSupportCloseReasoning;
        return this;
    }

    public Boolean isIsSupportCloseReasoning() {
        return isSupportCloseReasoning;
    }

    public ModelServiceRsp setIsNetwork(Boolean isNetwork) {
        this.isNetwork = isNetwork;
        return this;
    }

    public Boolean isIsNetwork() {
        return isNetwork;
    }

    public ModelServiceRsp setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
        return this;
    }

    public Boolean isIsPublic() {
        return isPublic;
    }

    public String getModelType() {
        return modelType;
    }

    public ModelServiceRsp setModelType(String modelType) {
        this.modelType = modelType;
        return this;
    }

    public String getModelTags() {
        return modelTags;
    }

    public ModelServiceRsp setModelTags(String modelTags) {
        this.modelTags = modelTags;
        return this;
    }

    public String getModelDescription() {
        return modelDescription;
    }

    public ModelServiceRsp setModelDescription(String modelDescription) {
        this.modelDescription = modelDescription;
        return this;
    }

    public String getModelDescriptionEn() {
        return modelDescriptionEn;
    }

    public ModelServiceRsp setModelDescriptionEn(String modelDescriptionEn) {
        this.modelDescriptionEn = modelDescriptionEn;
        return this;
    }

    public String getModelDeployType() {
        return modelDeployType;
    }

    public ModelServiceRsp setModelDeployType(String modelDeployType) {
        this.modelDeployType = modelDeployType;
        return this;
    }

    public String getDomainId() {
        return domainId;
    }

    public ModelServiceRsp setDomainId(String domainId) {
        this.domainId = domainId;
        return this;
    }

    public String getProjectId() {
        return projectId;
    }

    public ModelServiceRsp setProjectId(String projectId) {
        this.projectId = projectId;
        return this;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public ModelServiceRsp setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
        return this;
    }

    public String getCreatedByUser() {
        return createdByUser;
    }

    public ModelServiceRsp setCreatedByUser(String createdByUser) {
        this.createdByUser = createdByUser;
        return this;
    }

    public Long getCreatedDate() {
        return createdDate;
    }

    public ModelServiceRsp setCreatedDate(Long createdDate) {
        this.createdDate = createdDate;
        return this;
    }

    public Long getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public ModelServiceRsp setLastUpdatedDate(Long lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
        return this;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public ModelServiceRsp setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
        return this;
    }

    public ModelServiceRsp setIsSupportFunction(Boolean isSupportFunction) {
        this.isSupportFunction = isSupportFunction;
        return this;
    }

    public Boolean isIsSupportFunction() {
        return isSupportFunction;
    }

    public String getInterfaceProtocol() {
        return interfaceProtocol;
    }

    public ModelServiceRsp setInterfaceProtocol(String interfaceProtocol) {
        this.interfaceProtocol = interfaceProtocol;
        return this;
    }

    public String getAuthMetadataId() {
        return authMetadataId;
    }

    public ModelServiceRsp setAuthMetadataId(String authMetadataId) {
        this.authMetadataId = authMetadataId;
        return this;
    }

    public String getAuthId() {
        return authId;
    }

    public ModelServiceRsp setAuthId(String authId) {
        this.authId = authId;
        return this;
    }

    public String getPublishStatus() {
        return publishStatus;
    }

    public ModelServiceRsp setPublishStatus(String publishStatus) {
        this.publishStatus = publishStatus;
        return this;
    }

    public ModelServiceRsp setIsSupportStream(Boolean isSupportStream) {
        this.isSupportStream = isSupportStream;
        return this;
    }

    public Boolean isIsSupportStream() {
        return isSupportStream;
    }

    public Integer getThrottlingPolicy() {
        return throttlingPolicy;
    }

    public ModelServiceRsp setThrottlingPolicy(Integer throttlingPolicy) {
        this.throttlingPolicy = throttlingPolicy;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public ModelServiceRsp setStatus(String status) {
        this.status = status;
        return this;
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public ModelServiceRsp setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
        return this;
    }

    public ModelServiceRsp setIsSubscribed(Boolean isSubscribed) {
        this.isSubscribed = isSubscribed;
        return this;
    }

    public Boolean isIsSubscribed() {
        return isSubscribed;
    }

    public String getContextLength() {
        return contextLength;
    }

    public ModelServiceRsp setContextLength(String contextLength) {
        this.contextLength = contextLength;
        return this;
    }

    public String getModelSeries() {
        return modelSeries;
    }

    public ModelServiceRsp setModelSeries(String modelSeries) {
        this.modelSeries = modelSeries;
        return this;
    }

    public ModelServiceRsp setIsInFreeTrial(Boolean isInFreeTrial) {
        this.isInFreeTrial = isInFreeTrial;
        return this;
    }

    public Boolean isIsInFreeTrial() {
        return isInFreeTrial;
    }

    public String getTemplateId() {
        return templateId;
    }

    public ModelServiceRsp setTemplateId(String templateId) {
        this.templateId = templateId;
        return this;
    }

    public ModelServiceRsp setIsSupportDeepThinking(Boolean isSupportDeepThinking) {
        this.isSupportDeepThinking = isSupportDeepThinking;
        return this;
    }

    public Boolean isIsSupportDeepThinking() {
        return isSupportDeepThinking;
    }

    public ModelServiceRsp setIsPreparingForOffline(Boolean isPreparingForOffline) {
        this.isPreparingForOffline = isPreparingForOffline;
        return this;
    }

    public Boolean isIsPreparingForOffline() {
        return isPreparingForOffline;
    }

    public String getResidentModelId() {
        return residentModelId;
    }

    public ModelServiceRsp setResidentModelId(String residentModelId) {
        this.residentModelId = residentModelId;
        return this;
    }

    public ModelServiceRsp setIsAuthConfigured(Boolean isAuthConfigured) {
        this.isAuthConfigured = isAuthConfigured;
        return this;
    }

    public Boolean isIsAuthConfigured() {
        return isAuthConfigured;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ModelServiceRsp {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    providerId: ").append(toIndentedString(providerId)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    serviceName: ").append(toIndentedString(serviceName)).append("\n");
        sb.append("    serviceKey: ").append(toIndentedString(serviceKey)).append("\n");
        sb.append("    modelName: ").append(toIndentedString(modelName)).append("\n");
        sb.append("    logo: ").append(toIndentedString(logo)).append("\n");
        sb.append("    providerName: ").append(toIndentedString(providerName)).append("\n");
        sb.append("    providerNameEn: ").append(toIndentedString(providerNameEn)).append("\n");
        sb.append("    isReasoning: ").append(toIndentedString(isReasoning)).append("\n");
        sb.append("    isSupportCloseReasoning: ").append(toIndentedString(isSupportCloseReasoning)).append("\n");
        sb.append("    isNetwork: ").append(toIndentedString(isNetwork)).append("\n");
        sb.append("    isPublic: ").append(toIndentedString(isPublic)).append("\n");
        sb.append("    modelType: ").append(toIndentedString(modelType)).append("\n");
        sb.append("    modelTags: ").append(toIndentedString(modelTags)).append("\n");
        sb.append("    modelDescription: ").append(toIndentedString(modelDescription)).append("\n");
        sb.append("    modelDescriptionEn: ").append(toIndentedString(modelDescriptionEn)).append("\n");
        sb.append("    modelDeployType: ").append(toIndentedString(modelDeployType)).append("\n");
        sb.append("    domainId: ").append(toIndentedString(domainId)).append("\n");
        sb.append("    projectId: ").append(toIndentedString(projectId)).append("\n");
        sb.append("    workspaceId: ").append(toIndentedString(workspaceId)).append("\n");
        sb.append("    createdByUser: ").append(toIndentedString(createdByUser)).append("\n");
        sb.append("    createdDate: ").append(toIndentedString(createdDate)).append("\n");
        sb.append("    lastUpdatedDate: ").append(toIndentedString(lastUpdatedDate)).append("\n");
        sb.append("    apiUrl: ").append(toIndentedString(apiUrl)).append("\n");
        sb.append("    isSupportFunction: ").append(toIndentedString(isSupportFunction)).append("\n");
        sb.append("    interfaceProtocol: ").append(toIndentedString(interfaceProtocol)).append("\n");
        sb.append("    authMetadataId: ").append(toIndentedString(authMetadataId)).append("\n");
        sb.append("    authId: ").append(toIndentedString(authId)).append("\n");
        sb.append("    publishStatus: ").append(toIndentedString(publishStatus)).append("\n");
        sb.append("    isSupportStream: ").append(toIndentedString(isSupportStream)).append("\n");
        sb.append("    throttlingPolicy: ").append(toIndentedString(throttlingPolicy)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    disclaimer: ").append(toIndentedString(disclaimer)).append("\n");
        sb.append("    isSubscribed: ").append(toIndentedString(isSubscribed)).append("\n");
        sb.append("    contextLength: ").append(toIndentedString(contextLength)).append("\n");
        sb.append("    modelSeries: ").append(toIndentedString(modelSeries)).append("\n");
        sb.append("    isInFreeTrial: ").append(toIndentedString(isInFreeTrial)).append("\n");
        sb.append("    templateId: ").append(toIndentedString(templateId)).append("\n");
        sb.append("    isSupportDeepThinking: ").append(toIndentedString(isSupportDeepThinking)).append("\n");
        sb.append("    isPreparingForOffline: ").append(toIndentedString(isPreparingForOffline)).append("\n");
        sb.append("    residentModelId: ").append(toIndentedString(residentModelId)).append("\n");
        sb.append("    isAuthConfigured: ").append(toIndentedString(isAuthConfigured)).append("\n");
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
        ModelServiceRsp modelServiceRsp = (ModelServiceRsp) o;
        return Objects.equals(this.id, modelServiceRsp.id) && Objects.equals(this.providerId,
            modelServiceRsp.providerId) && Objects.equals(this.type, modelServiceRsp.type) && Objects.equals(
            this.serviceName, modelServiceRsp.serviceName) && Objects.equals(this.serviceKey,
            modelServiceRsp.serviceKey) && Objects.equals(this.modelName, modelServiceRsp.modelName) && Objects.equals(
            this.logo, modelServiceRsp.logo) && Objects.equals(this.providerName, modelServiceRsp.providerName)
            && Objects.equals(this.providerNameEn, modelServiceRsp.providerNameEn) && Objects.equals(this.isReasoning,
            modelServiceRsp.isReasoning) && Objects.equals(this.isSupportCloseReasoning,
            modelServiceRsp.isSupportCloseReasoning) && Objects.equals(this.isNetwork, modelServiceRsp.isNetwork)
            && Objects.equals(this.isPublic, modelServiceRsp.isPublic) && Objects.equals(this.modelType,
            modelServiceRsp.modelType) && Objects.equals(this.modelTags, modelServiceRsp.modelTags) && Objects.equals(
            this.modelDescription, modelServiceRsp.modelDescription) && Objects.equals(this.modelDescriptionEn,
            modelServiceRsp.modelDescriptionEn) && Objects.equals(this.modelDeployType, modelServiceRsp.modelDeployType)
            && Objects.equals(this.domainId, modelServiceRsp.domainId) && Objects.equals(this.projectId,
            modelServiceRsp.projectId) && Objects.equals(this.workspaceId, modelServiceRsp.workspaceId)
            && Objects.equals(this.createdByUser, modelServiceRsp.createdByUser) && Objects.equals(this.createdDate,
            modelServiceRsp.createdDate) && Objects.equals(this.lastUpdatedDate, modelServiceRsp.lastUpdatedDate)
            && Objects.equals(this.apiUrl, modelServiceRsp.apiUrl) && Objects.equals(this.isSupportFunction,
            modelServiceRsp.isSupportFunction) && Objects.equals(this.interfaceProtocol,
            modelServiceRsp.interfaceProtocol) && Objects.equals(this.authMetadataId, modelServiceRsp.authMetadataId)
            && Objects.equals(this.authId, modelServiceRsp.authId) && Objects.equals(this.publishStatus,
            modelServiceRsp.publishStatus) && Objects.equals(this.isSupportStream, modelServiceRsp.isSupportStream)
            && Objects.equals(this.throttlingPolicy, modelServiceRsp.throttlingPolicy) && Objects.equals(this.status,
            modelServiceRsp.status) && Objects.equals(this.disclaimer, modelServiceRsp.disclaimer) && Objects.equals(
            this.isSubscribed, modelServiceRsp.isSubscribed) && Objects.equals(this.contextLength,
            modelServiceRsp.contextLength) && Objects.equals(this.modelSeries, modelServiceRsp.modelSeries)
            && Objects.equals(this.isInFreeTrial, modelServiceRsp.isInFreeTrial) && Objects.equals(this.templateId,
            modelServiceRsp.templateId) && Objects.equals(this.isSupportDeepThinking,
            modelServiceRsp.isSupportDeepThinking) && Objects.equals(this.isPreparingForOffline,
            modelServiceRsp.isPreparingForOffline) && Objects.equals(this.residentModelId,
            modelServiceRsp.residentModelId) && Objects.equals(this.isAuthConfigured, modelServiceRsp.isAuthConfigured);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, providerId, type, serviceName, serviceKey, modelName, logo, providerName,
            providerNameEn, isReasoning, isSupportCloseReasoning, isNetwork, isPublic, modelType, modelTags,
            modelDescription, modelDescriptionEn, modelDeployType, domainId, projectId, workspaceId, createdByUser,
            createdDate, lastUpdatedDate, apiUrl, isSupportFunction, interfaceProtocol, authMetadataId, authId,
            publishStatus, isSupportStream, throttlingPolicy, status, disclaimer, isSubscribed, contextLength,
            modelSeries, isInFreeTrial, templateId, isSupportDeepThinking, isPreparingForOffline, residentModelId,
            isAuthConfigured);
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
