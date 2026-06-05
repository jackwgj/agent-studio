/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;

/**
 * 功能描述 模型信息
 *
 */
@SuppressWarnings("LombokGetterMayBeUsed")
public class ModelServiceInfo {
    @JsonProperty("deployment_id")
    private String deploymentId = null;

    /**
     * set deploymentId
     *
     * @param deploymentId deploymentId
     * @return ModelServiceInfo
     */
    public ModelServiceInfo setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
        return this;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    @JsonProperty("name")
    private String name = null;

    /**
     * set name
     *
     * @param name name
     * @return ModelServiceInfo
     */
    public ModelServiceInfo setName(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    @JsonProperty("project_id")
    private String projectId = null;

    /**
     * set projectId
     *
     * @param projectId projectId
     * @return ModelServiceInfo
     */
    public ModelServiceInfo setProjectId(String projectId) {
        this.projectId = projectId;
        return this;
    }

    public String getProjectId() {
        return projectId;
    }

    @JsonProperty("model_id")
    private String modelId = null;

    /**
     * set modelId
     *
     * @param modelId modelId
     * @return ModelServiceInfo
     */
    public ModelServiceInfo setModelId(String modelId) {
        this.modelId = modelId;
        return this;
    }

    public String getModelId() {
        return modelId;
    }

    @JsonProperty("model_type")
    private String modelType = null;

    /**
     * set modelType
     *
     * @param modelType modelType
     * @return ModelServiceInfo
     */
    public ModelServiceInfo setModelType(String modelType) {
        this.modelType = modelType;
        return this;
    }

    public String getModelType() {
        return modelType;
    }

    @JsonProperty("model_name")
    private String modelName = null;

    /**
     * set modelName
     *
     * @param modelName modelName
     * @return ModelServiceInfo
     */
    public ModelServiceInfo setModelName(String modelName) {
        this.modelName = modelName;
        return this;
    }

    public String getModelName() {
        return modelName;
    }

    private String model = null;

    /**
     * set model
     *
     * @param model model
     * @return ModelServiceInfo
     */
    public ModelServiceInfo setModel(String model) {
        this.model = model;
        return this;
    }

    public String getModel() {
        return model;
    }

    @JsonProperty("endpoint")
    private String endpoint = null;

    /**
     * set endpoint
     *
     * @param endpoint endpoint
     * @return ModelServiceInfo
     */
    public ModelServiceInfo setEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public String getEndpoint() {
        return endpoint;
    }

    @JsonProperty("enable_status")
    private String enableStatus = null;

    /**
     * set enableStatus
     *
     * @param enableStatus enableStatus
     * @return ModelServiceInfo
     */
    public ModelServiceInfo setEnableStatus(String enableStatus) {
        this.enableStatus = enableStatus;
        return this;
    }

    public String getEnableStatus() {
        return enableStatus;
    }

    @JsonProperty("enable_model")
    private String enableModel = null;

    /**
     * set enableModel
     *
     * @param enableModel enableModel
     * @return ModelServiceInfo
     */
    public ModelServiceInfo setEnableModel(String enableModel) {
        this.enableModel = enableModel;
        return this;
    }

    public String getEnableModel() {
        return enableModel;
    }

    @JsonProperty("metadata")
    @Valid
    private ModelMetadata metadata = null;

    /**
     * set metadata
     *
     * @param metadata metadata
     * @return ModelServiceInfo
     */
    public ModelServiceInfo setMetadata(ModelMetadata metadata) {
        this.metadata = metadata;
        return this;
    }

    public ModelMetadata getMetadata() {
        return metadata;
    }

    @JsonProperty("is_preset")
    private boolean isPreset;

    /**
     * set isPreset
     *
     * @param isPreset isPreset
     * @return ModelServiceInfo
     */
    public ModelServiceInfo setIsPreset(boolean isPreset) {
        this.isPreset = isPreset;
        return this;
    }

    public boolean getIsPreset() {
        return isPreset;
    }

    @JsonProperty("api_env")
    private Object apiEnv;

    /**
     * set apiEnv
     *
     * @param apiEnv apiEnv
     * @return ModelServiceInfo
     */
    public ModelServiceInfo setApiEnv(Object apiEnv) {
        this.apiEnv = apiEnv;
        return this;
    }

    public Object getApiEnv() {
        return apiEnv;
    }
}
