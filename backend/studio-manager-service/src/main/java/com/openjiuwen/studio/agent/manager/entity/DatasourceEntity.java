/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * t_agent_datasource表实体
 *
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DatasourceEntity {
    /**
     * uuid、主键
     */
    @JsonProperty("id")
    private String id;

    /**
     * project id
     */
    @JsonProperty("project_id")
    private String projectId;

    /**
     * workspace id
     */
    @JsonProperty("workspace_id")
    private String workspaceId;

    /**
     * domain id
     */
    @JsonProperty("domain_id")
    private String domainId;

    /**
     * 数据源资源名称
     */
    @JsonProperty("name")
    private String name;

    /**
     * 数据源类型，暂时只支持MYSQL
     */
    @JsonProperty("type")
    private String type;

    /**
     * 数据源描述信息
     */
    @JsonProperty("desc")
    private String desc;

    /**
     * 接入网络类型
     */
    @JsonProperty("internet_access")
    private String internetAccess;

    /**
     * 接入网络region（rds模式有效）
     */
    @JsonProperty("region")
    private String region;

    /**
     * 实例id（rds模式有效）
     */
    @JsonProperty("instance_id")
    private String instanceId;

    /**
     * 主机名（rds模式有效
     */
    @JsonProperty("instance_name")
    private String instanceName;

    /**
     * 数据库连接信息
     */
    @JsonProperty("connection_info")
    private String connectionInfo;

    /**
     * 数据库状态，支持success和failed
     */
    @JsonProperty("status")
    private String status;

    /**
     * 最后一次错误消息
     */
    @JsonProperty("last_error_message")
    private String lastErrorMessage;

    /**
     * 创建人
     */
    @JsonProperty("created_by")
    private String createdBy;

    /**
     * 创建人id
     */
    @JsonProperty("creator_id")
    private String creatorId;

    /**
     * 创建时间
     */
    @JsonProperty("created_on")
    private Date createdOn;

    /**
     * 修改人
     */
    @JsonProperty("updated_by")
    private String updatedBy;

    /**
     * 修改人id
     */
    @JsonProperty("updaterId")
    private String updaterId;

    /**
     * 更新时间
     */
    @JsonProperty("updated_on")
    private Date updatedOn;
}
