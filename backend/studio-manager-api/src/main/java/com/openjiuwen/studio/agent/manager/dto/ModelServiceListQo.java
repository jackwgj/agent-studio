/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * ModelServiceListQo: converted from multi query params
 */
@ApiModel(description = "ModelServiceListQo: converted from multi query params")

@Validated

public class ModelServiceListQo implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("workspace_id")
    @Pattern(regexp = "^[a-zA-Z0-9_()\\-]{1,40}$")
    @NotBlank
    private String workspaceId = null;

    @JsonProperty("page_size")
    @Range(min = 1L, max = 500L)
    private Integer pageSize = 5;

    @JsonProperty("page_num")
    @Range(min = 1L)
    private Integer pageNum = 1;

    @JsonProperty("provider_id")
    @Pattern(regexp = "^[a-zA-Z0-9_-]{1,80}$")
    @Length(max = 80)
    private String providerId = null;

    @JsonProperty("service_name")
    @Length(max = 64)
    private String serviceName = null;

    @JsonProperty("model_name")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5a-zA-Z0-9:. _/|\\\\-]{1,64}$")
    @Length(max = 64)
    private String modelName = null;

    @JsonProperty("model_type")
    @Pattern(
        regexp = "(LLM|Text-Embedding|RERANK|TEXT-TO-IMAGE|IMAGE-TO-TEXT|AUDIO-TO-TEXT|TEXT-TO-AUDIO|TEXT-IMAGE-EMBEDDING)")
    private String modelType = null;

    @JsonProperty("publish_status")
    @Pattern(regexp = "^(?:offline|online)?$")
    private String publishStatus = null;

    @JsonProperty("id")
    @Pattern(regexp = "^[a-zA-Z0-9_-]{1,80}$")
    @Length(max = 80)
    private String id = null;

    @JsonProperty("context_length")
    @Valid
    @Size(max = 10)
    private List<@Length(max = 16) String> contextLength = null;

    @JsonProperty("model_series")
    @Valid
    @Size(max = 10)
    private List<@Length(max = 16) String> modelSeries = null;

    @JsonProperty("model_types")
    @Valid
    @Size(max = 10)
    private List<@Length(max = 64) String> modelTypes = null;

    @JsonProperty("is_support_deep_thinking")
    private Boolean isSupportDeepThinking = null;

    @JsonProperty("is_support_function_call")
    private Boolean isSupportFunctionCall = null;

    @JsonProperty("is_subscribed")
    private Boolean isSubscribed = null;

    @JsonProperty("functioncall")
    private Boolean functioncall = null;

    public String getWorkspaceId() {
        return workspaceId;
    }

    public ModelServiceListQo setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
        return this;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public ModelServiceListQo setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public ModelServiceListQo setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
        return this;
    }

    public String getProviderId() {
        return providerId;
    }

    public ModelServiceListQo setProviderId(String providerId) {
        this.providerId = providerId;
        return this;
    }

    public String getServiceName() {
        return serviceName;
    }

    public ModelServiceListQo setServiceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    public String getModelName() {
        return modelName;
    }

    public ModelServiceListQo setModelName(String modelName) {
        this.modelName = modelName;
        return this;
    }

    public String getModelType() {
        return modelType;
    }

    public ModelServiceListQo setModelType(String modelType) {
        this.modelType = modelType;
        return this;
    }

    public String getPublishStatus() {
        return publishStatus;
    }

    public ModelServiceListQo setPublishStatus(String publishStatus) {
        this.publishStatus = publishStatus;
        return this;
    }

    public String getId() {
        return id;
    }

    public ModelServiceListQo setId(String id) {
        this.id = id;
        return this;
    }

    public List<String> getContextLength() {
        return contextLength;
    }

    public ModelServiceListQo setContextLength(List<String> contextLength) {
        this.contextLength = contextLength;
        return this;
    }

    public List<String> getModelSeries() {
        return modelSeries;
    }

    public ModelServiceListQo setModelSeries(List<String> modelSeries) {
        this.modelSeries = modelSeries;
        return this;
    }

    public List<String> getModelTypes() {
        return modelTypes;
    }

    public ModelServiceListQo setModelTypes(List<String> modelTypes) {
        this.modelTypes = modelTypes;
        return this;
    }

    public ModelServiceListQo setIsSupportDeepThinking(Boolean isSupportDeepThinking) {
        this.isSupportDeepThinking = isSupportDeepThinking;
        return this;
    }

    public Boolean isIsSupportDeepThinking() {
        return isSupportDeepThinking;
    }

    public ModelServiceListQo setIsSupportFunctionCall(Boolean isSupportFunctionCall) {
        this.isSupportFunctionCall = isSupportFunctionCall;
        return this;
    }

    public Boolean isIsSupportFunctionCall() {
        return isSupportFunctionCall;
    }

    public ModelServiceListQo setIsSubscribed(Boolean isSubscribed) {
        this.isSubscribed = isSubscribed;
        return this;
    }

    public Boolean isIsSubscribed() {
        return isSubscribed;
    }

    public ModelServiceListQo setFunctioncall(Boolean functioncall) {
        this.functioncall = functioncall;
        return this;
    }

    public Boolean isFunctioncall() {
        return functioncall;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ModelServiceListQo {\n");

        sb.append("    workspaceId: ").append(toIndentedString(workspaceId)).append("\n");
        sb.append("    pageSize: ").append(toIndentedString(pageSize)).append("\n");
        sb.append("    pageNum: ").append(toIndentedString(pageNum)).append("\n");
        sb.append("    providerId: ").append(toIndentedString(providerId)).append("\n");
        sb.append("    serviceName: ").append(toIndentedString(serviceName)).append("\n");
        sb.append("    modelName: ").append(toIndentedString(modelName)).append("\n");
        sb.append("    modelType: ").append(toIndentedString(modelType)).append("\n");
        sb.append("    publishStatus: ").append(toIndentedString(publishStatus)).append("\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    contextLength: ").append(toIndentedString(contextLength)).append("\n");
        sb.append("    modelSeries: ").append(toIndentedString(modelSeries)).append("\n");
        sb.append("    modelTypes: ").append(toIndentedString(modelTypes)).append("\n");
        sb.append("    isSupportDeepThinking: ").append(toIndentedString(isSupportDeepThinking)).append("\n");
        sb.append("    isSupportFunctionCall: ").append(toIndentedString(isSupportFunctionCall)).append("\n");
        sb.append("    isSubscribed: ").append(toIndentedString(isSubscribed)).append("\n");
        sb.append("    functioncall: ").append(toIndentedString(functioncall)).append("\n");
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
        ModelServiceListQo modelServiceListQo = (ModelServiceListQo) o;
        return Objects.equals(this.workspaceId, modelServiceListQo.workspaceId) && Objects.equals(this.pageSize,
            modelServiceListQo.pageSize) && Objects.equals(this.pageNum, modelServiceListQo.pageNum) && Objects.equals(
            this.providerId, modelServiceListQo.providerId) && Objects.equals(this.serviceName,
            modelServiceListQo.serviceName) && Objects.equals(this.modelName, modelServiceListQo.modelName)
            && Objects.equals(this.modelType, modelServiceListQo.modelType) && Objects.equals(this.publishStatus,
            modelServiceListQo.publishStatus) && Objects.equals(this.id, modelServiceListQo.id) && Objects.equals(
            this.contextLength, modelServiceListQo.contextLength) && Objects.equals(this.modelSeries,
            modelServiceListQo.modelSeries) && Objects.equals(this.modelTypes, modelServiceListQo.modelTypes)
            && Objects.equals(this.isSupportDeepThinking, modelServiceListQo.isSupportDeepThinking) && Objects.equals(
            this.isSupportFunctionCall, modelServiceListQo.isSupportFunctionCall) && Objects.equals(this.isSubscribed,
            modelServiceListQo.isSubscribed) && Objects.equals(this.functioncall, modelServiceListQo.functioncall);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workspaceId, pageSize, pageNum, providerId, serviceName, modelName, modelType,
            publishStatus, id, contextLength, modelSeries, modelTypes, isSupportDeepThinking, isSupportFunctionCall,
            isSubscribed, functioncall);
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
