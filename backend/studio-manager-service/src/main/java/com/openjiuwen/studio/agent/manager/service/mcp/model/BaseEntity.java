/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service.mcp.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * BaseEntity
 *
 * @Date: 2024/5/14 14:10
 * @Description: 基本Entity
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BaseEntity extends UpdateBaseInfo {
    @NotNull
    @JsonProperty("tenant_id")
    @JSONField(name = "tenant_id")
    protected String tenantId;

    @JsonProperty("dept_code")
    @JSONField(name = "dept_code")
    protected String deptCode;

    @JsonProperty("created_date")
    @JSONField(name = "created_date")
    protected Date createdDate;

    @JsonProperty("created_by_user_id")
    @JSONField(name = "created_by_user_id")
    protected String createdByUserId;

    @JsonProperty("deleted")
    @JSONField(name = "deleted")
    protected boolean deleted;
}
