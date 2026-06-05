/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Entity
@Table(name = "t_history_mapping")
public class HistoryMappingEntity {
    /**
     * 已删除资源关系表唯一标识
     */
    @Id
    @Column(name = "history_id")
    private String historyId;

    /**
     * mapping_id
     */
    @Column(name = "mapping_id")
    private String mappingId;

    /**
     * app_id
     */
    @Column(name = "app_id")
    private String appId;

    /**
     * app_version
     */
    @Column(name = "app_version")
    private String appVersion;

    /**
     * app_type
     */
    @Column(name = "app_type")
    private String appType;

    /**
     * app_name
     */
    @Column(name = "app_name")
    private String appName;

    /**
     * resource_id
     */
    @Column(name = "resource_id")
    private String resourceId;

    /**
     * resource_type
     */
    @Column(name = "resource_type")
    private String resourceType;

    /**
     * resource_name
     */
    @Column(name = "resource_name")
    private String resourceName;

    /**
     * resource_版本
     */
    @Column(name = "resource_version")
    private String resourceVersion;

    /**
     * resource描述
     */
    @Column(name = "resource_desc")
    private String resourceDesc;

    /**
     * valid，关联是否有效
     */
    @Column(name = "valid")
    private boolean valid;

    /**
     * 创建时间
     */
    @Column(name = "created_on")
    private Date createdOn;

    /**
     * 更新时间
     */
    @Column(name = "updated_on")
    private Date updatedOn;

    /**
     * share表示跨空间共享引用，direct表示本空间直接引用
     */
    @Column(name = "reference_type")
    private String referenceType;

    /**
     * 被引用元素所属的来源空间
     */
    @Column(name = "app_workspace_id")
    private String appWorkspaceId;

    /**
     * 被引用元素所属的来源空间
     */
    @Column(name = "resource_workspace_id")
    private String resourceWorkspaceId;

    /**
     * 被引用资源的输入参数
     */
    @Column(name = "extends")
    private String extendInfo;

    /**
     * 发布版本子类型
     */
    @Column(name = "sub_type")
    private String subType;
}
