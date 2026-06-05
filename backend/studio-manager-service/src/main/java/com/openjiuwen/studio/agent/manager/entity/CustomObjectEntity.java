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
 * 对象管理实体类
 *
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name = "t_custom_object")
public class CustomObjectEntity {
    /**
     * 自定义对象id
     */
    @Id
    @Column(name = "id")
    private String id;

    /**
     * 项目id
     */
    @Column(name = "project_id")
    private String projectId;

    /**
     * 工作空间id
     */
    @Column(name = "workspace_id")
    private String workspaceId;

    /**
     * 自定义对象名字
     */
    @Column(name = "name")
    private String name;

    /**
     * 自定义对象描述
     */
    @Column(name = "description")
    private String description;

    /**
     * 自定义对象格式
     */
    @Column(name = "object_schema")
    private String objectSchema;

    /**
     * 创建人id
     */
    @Column(name = "creator_id")
    private String creatorId;

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
}
