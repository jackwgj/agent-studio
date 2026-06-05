/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service.environment.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * 功能描述
 *
 */
@Data
public class OpsEnvironmentInfo {
    /**
     * 环境id
     */
    @JsonProperty("id")
    private String id;

    /**
     * 环境名称
     */
    @JsonProperty("name")
    private String name;

    /**
     * 环境状态
     */
    @JsonProperty("status")
    private String status;

    /**
     * 环境描述
     */
    @JsonProperty("description")
    private String description;

    /**
     * 终端节点
     */
    @JsonProperty("vpcEp")
    private String vpcEp;

    /**
     * 弹性公网ip
     */
    @JsonProperty("eip")
    private String eip;

    /**
     * 环境状态异常原因
     */
    @JsonProperty("status_reason")
    private String statusReason;

    @JsonProperty("updated_at")
    private String updatedAt;

    @JsonProperty("updated_by")
    private String updatedBy;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("created_by")
    private String createdBy;

}
