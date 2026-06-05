/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service.mcp.apigservice.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.huaweicloud.sdk.apig.v2.model.ApiFuncCreate;

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
public class ApigRequsetDto {
    private String id;

    private String serviceId;

    private String name;

    private String description;

    @JsonProperty("tenant_id")
    private String tenantId;

    private String projectId;

    // 函数名称
    private String funcName;

    // 函数的调用uri
    private String funcUrn;

    private ApiFuncCreate.NetworkTypeEnum networkTypeEnum;

    private String apiServiceName;

    private String fgInstanceName;

    private String fgInstanceVersion;

    private String fgInstanceUri;

    private String apigPath;

    private String groupId;

    private String codeUrl;

    private String groupName;

    private String apiType;

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
