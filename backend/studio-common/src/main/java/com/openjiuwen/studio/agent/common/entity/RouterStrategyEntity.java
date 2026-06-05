/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.common.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class RouterStrategyEntity {
    /**
     * 模型路由策略ID
     */
    private String id;

    /**
     * 模型路由策略名称
     */
    @JsonProperty("strategy_name")
    @JSONField(name = "strategy_name")
    private String strategyName;

    /**
     * 模型路由策略类型
     */
    @JsonProperty("strategy_type")
    @JSONField(name = "strategy_type")
    private String strategyType;

    /**
     * 模型路由策略调用ID
     */
    @JsonProperty("strategy_key")
    @JSONField(name = "strategy_key")
    private String strategyKey;

    /**
     * 模型路由策略描述
     */
    @JsonProperty("strategy_description")
    @JSONField(name = "strategy_description")
    private String strategyDescription;

    /**
     * 模型服务ID列表
     */
    @JsonProperty("service_id_list")
    @JSONField(name = "service_id_list")
    private String serviceIdList;

    /**
     * 模型供应商认证配置ID
     */
    @JsonProperty("auth_id_list")
    @JSONField(name = "auth_id_list")
    private String authIdList;

    /**
     * 模型路由策略超时时间
     */
    @JsonProperty("strategy_timeout")
    @JSONField(name = "strategy_timeout")
    private Integer strategyTimeout;

    /**
     * 模型服务重试次数
     */
    @JsonProperty("strategy_retry_count")
    @JSONField(name = "strategy_retry_count")
    private Integer strategyRetryCount;

    /**
     * 模型服务数量
     */
    @JsonProperty("service_count")
    @JSONField(name = "service_count")
    private Integer serviceCount;

    /**
     * 预留属性，格式为JSON
     */
    @JsonProperty("prepare_attribute")
    @JSONField(name = "prepare_attribute")
    private String prepareAttribute;

    /**
     * 项目ID
     */
    @JsonProperty("project_id")
    @JSONField(name = "project_id")
    private String projectId;

    /**
     * 空间ID
     */
    @JsonProperty("workspace_id")
    @JSONField(name = "workspace_id")
    private String workspaceId;

    /**
     * DOMAIN_ID
     */
    @JsonProperty("domain_id")
    @JSONField(name = "domain_id")
    private String domainId;

    /**
     * 创建人ID
     */
    @JsonProperty("created_by_user_name")
    @JSONField(name = "created_by_user_name")
    private String createdByUserName;

    /**
     * 最近使用人ID
     */
    @JsonProperty("last_updated_by_user_name")
    @JSONField(name = "last_updated_by_user_name")
    private String lastUpdatedByUserName;

    /**
     * 创建时间
     */
    @JsonProperty("created_date")
    @JSONField(name = "created_date")
    private long createdDate;

    /**
     * 最近更新时间
     */
    @JsonProperty("last_updated_date")
    @JSONField(name = "last_updated_date")
    private long lastUpdatedDate;

    /**
     * traceID
     */
    @JsonProperty("trace_id")
    @JSONField(name = "trace_id")
    private String traceId;
}