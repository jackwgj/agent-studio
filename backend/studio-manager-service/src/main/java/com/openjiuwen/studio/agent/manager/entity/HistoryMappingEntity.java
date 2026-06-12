/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 已删除资源的关系映射表
 *
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class HistoryMappingEntity {
    /**
     * 已删除资源关系表唯一标识
     */
    @JsonProperty("history_id")
    private String historyId;

    /**
     * mapping_id
     */
    @JsonProperty("mapping_id")
    private String mappingId;

    /**
     * app_id
     */
    @JsonProperty("app_id")
    private String appId;

    /**
     * app_version
     */
    @JsonProperty("app_version")
    private String appVersion;

    /**
     * app_type
     */
    @JsonProperty("app_type")
    private String appType;

    /**
     * app_name
     */
    @JsonProperty("app_name")
    private String appName;

    /**
     * resource_id
     */
    @JsonProperty("resource_id")
    private String resourceId;

    /**
     * resource_type
     */
    @JsonProperty("resource_type")
    private String resourceType;

    /**
     * resource_name
     */
    @JsonProperty("resource_name")
    private String resourceName;

    /**
     * resource_版本
     */
    @JsonProperty("resource_version")
    private String resourceVersion;

    /**
     * resource描述
     */
    @JsonProperty("resource_desc")
    private String resourceDesc;

    /**
     * valid，关联是否有效
     */
    @JsonProperty("valid")
    private Boolean valid;

    /**
     * 创建时间
     */
    @JsonProperty("created_on")
    private Date createdOn;

    /**
     * 更新时间
     */
    @JsonProperty("updated_on")
    private Date updatedOn;

    /**
     * share表示跨空间共享引用，direct表示本空间直接引用
     */
    @JsonProperty("reference_type")
    private String referenceType;

    /**
     * 被引用元素所属的来源空间
     */
    @JsonProperty("app_workspace_id")
    private String appWorkspaceId;

    /**
     * 被引用元素所属的来源空间
     */
    @JsonProperty("resource_workspace_id")
    private String resourceWorkspaceId;

    /**
     * 被引用资源的输入参数
     */
    @JsonProperty("extends")
    private String extendInfo;

    /**
     * 发布版本子类型
     */
    @JsonProperty("sub_type")
    private String subType;
}
