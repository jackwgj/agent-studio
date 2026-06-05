/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 知识库的连接信息
 *
 * @since 2025-08-25
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeBaseConnection {

    private String id;

    private String connectorId;

    private String name;

    private String description;

    private String icon;

    private String params;

    private String status;

    private String createUserId;

    private Long createTime;

    private String updateUserId;

    private Long updateTime;

    private String projectId;

    private String workspaceId;

    private String domainId;

}
