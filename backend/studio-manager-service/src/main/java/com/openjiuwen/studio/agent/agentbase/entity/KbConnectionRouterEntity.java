/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * t_tenant_type_mapping_entity表实体
 *
 * @since 2024-04-25
 */
@Data
public class KbConnectionRouterEntity {

    @JsonProperty("tenant_type")
    private String tenantType;

    @JsonProperty("domain_id")
    private String domainId;

    @JsonProperty("knowledge_base_connection_id")
    private String knowledgeBaseConnectionId;

    @JsonProperty("knowledge_base_used")
    private int knowledgeBaseUsed;

    @JsonProperty("knowledge_base_capacity")
    private int knowledgeBaseCapacity;

    private double usageRatio;

}
