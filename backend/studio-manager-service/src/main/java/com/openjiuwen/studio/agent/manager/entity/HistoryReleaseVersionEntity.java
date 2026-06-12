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
 * 功能描述 发布版本数据库实体
 *
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class HistoryReleaseVersionEntity {

    /**
     * UUID
     */
    @JsonProperty("history_id")
    private String historyId;

    /**
     * UUID
     */
    @JsonProperty("id")
    private String id;

    /**
     * 发布版本唯一标识
     */
    @JsonProperty("version_id")
    private String versionId;

    /**
     * 发布版本名称
     */
    @JsonProperty("version_name")
    private String versionName;

    /**
     * 发布版本说明
     */
    @JsonProperty("version_note")
    private String versionNote;

    /**
     * 应用ID
     */
    @JsonProperty("app_id")
    private String appId;

    /**
     * 应用类型
     */
    @JsonProperty("app_type")
    private String appType;

    /**
     * 发布版本子类型（如：deepresearch）
     */
    @JsonProperty("sub_type")
    private String subType;

    /**
     * 发布版本状态
     */
    @JsonProperty("status")
    private String status;

    /**
     * DSL文件相对路径
     */
    @JsonProperty("dsl_path")
    private String dslPath;

    /**
     * IR文件相对路径
     */
    @JsonProperty("ir_path")
    private String irPath;

    /**
     * 版本创建者
     */
    @JsonProperty("creator")
    private String creator;

    /**
     * 版本创建者user id
     */
    @JsonProperty("creator_id")
    private String creatorId;

    /**
     * 版本发布时间
     */
    @JsonProperty("released_on")
    private Date releasedOn;
}

