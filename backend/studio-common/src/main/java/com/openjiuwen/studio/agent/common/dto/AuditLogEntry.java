/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto;

import com.alibaba.fastjson2.annotation.JSONField;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 审计日志条目，对应日志文件中的一行 JSON
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogEntry {

    private String timestamp;

    @JSONField(name = "traceId")
    private String traceId;

    private String userId;

    private String userName;

    @JSONField(name = "operationType")
    private String operationType;

    @JSONField(name = "resourceType")
    private String resourceType;

    @JSONField(name = "resourceId")
    private String resourceId;

    @JSONField(name = "resourceName")
    private String resourceName;

    private String description;

    private String method;

    private Boolean success;

    @JSONField(name = "errorMessage")
    private String errorMessage;

    private Map<String, Object> params;
}