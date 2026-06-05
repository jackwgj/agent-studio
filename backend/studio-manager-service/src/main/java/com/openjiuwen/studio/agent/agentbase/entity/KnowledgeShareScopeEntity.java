/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.agentbase.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 知识库共享范围
 * t_knowledge_share_scope
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeShareScopeEntity {

    private String id;

    private String knowledgeBaseId;

    private String workspaceId;

    private String projectId;

    private String createdUserId;

    private String createdUserName;

    private Long createTime;

}