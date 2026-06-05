/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2023. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.sql.Timestamp;

/**
 * Created by Fang Zhen on 2024/3/19.
 */
@Data
public class BaseProperties {
    @JsonProperty("created_date")
    private Timestamp createdDate;

    @JsonProperty("created_by_user_id")
    private String createdByUserId;

    @JsonProperty("last_updated_date")
    private Timestamp lastUpdatedDate;

    @JsonProperty("last_updated_by_user_id")
    private String lastUpdatedByUserId;

    @JsonProperty("tenant_id")
    private String tenantId;

    private Boolean deleted = false;

    @JsonProperty("dept_code")
    private String deptCode;
}
