/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.rce.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.util.List;

/**
 * 功能描述
 *
 */
@Data
public class ModelServiceInfo {
    @JsonProperty("service_id")
    private String serviceId;

    /**
     * 模型部署服务的服务名称
     */
    @JsonProperty("service_name")
    private String serviceName;

    /**
     * 模型名字
     */
    @JsonProperty("model_name")
    private String modelName;

    /**
     * 模型服务的外部API访问地址
     */
    @JsonProperty("api_url")
    private String apiUrl;

    /**
     * 模型部署服务的模型资产来源
     */
    private String category;

    @JsonProperty("workspace_id")
    private String workspaceId;

    /**
     * 模型资产信息
     */
    private List<ModelAssetInfo> assets;
}
