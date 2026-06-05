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
 * 功能描述 发布版本数据库实体
 *
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name = "t_history_release_version")
public class HistoryReleaseVersionEntity {
    /**
     * UUID
     */
    @Id
    @Column(name = "history_id")
    private String historyId;

    /**
     * UUID
     */
    @Column(name = "id")
    private String id;

    /**
     * 发布版本唯一标识
     */
    @Column(name = "version_id")
    private String versionId;

    /**
     * 发布版本名称
     */
    @Column(name = "version_name")
    private String versionName;

    /**
     * 发布版本说明
     */
    @Column(name = "version_note")
    private String versionNote;

    /**
     * 应用ID
     */
    @Column(name = "app_id")
    private String appId;

    /**
     * 应用类型
     */
    @Column(name = "app_type")
    private String appType;

    /**
     * 发布版本子类型（如：deepresearch）
     */
    @Column(name = "sub_type")
    private String subType;

    /**
     * 发布版本状态
     */
    private String status;

    /**
     * DSL文件相对路径
     */
    @Column(name = "dsl_path")
    private String dslPath;

    /**
     * IR文件相对路径
     */
    @Column(name = "ir_path")
    private String irPath;

    /**
     * 版本创建者
     */
    private String creator;

    /**
     * 版本创建者user id
     */
    @Column(name = "creator_id")
    private String creatorId;

    /**
     * 版本发布时间
     */
    @Column(name = "released_on")
    private Date releasedOn;
}
