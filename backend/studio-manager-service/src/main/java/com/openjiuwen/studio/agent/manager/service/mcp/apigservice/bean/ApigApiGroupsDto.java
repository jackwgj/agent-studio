/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service.mcp.apigservice.bean;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * @ClassName : ApicApisDto
 * @Description :
 * @Date : Created in 2024/4/8 19:12
 * @Version : V1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApigApiGroupsDto {
    private String id;

    private String name;

    private String description;

    @JsonProperty("tenant_id")
    private String tenantId;

    /**
     * 创建者对应的用户名
     */
    @JsonProperty("created_by_user_id")
    private String createdByUserId;

    @JsonProperty("last_updated_by_user_id")
    private String lastUpdatedByUserId;

    @JsonProperty("created_date")
    private Timestamp createdDate;

    @JsonProperty("last_updated_date")
    private Timestamp lastUpdatedDate;

    private String status;
}
