/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dao;

import lombok.Data;

import java.util.Date;

/**
 * agent和knowledgeRepo的多对多关系映射表
 *
 */
@Data
public class MappingAgentKnowledge {
    /**
     * agent_id
     */
    private String agentId;

    /**
     * knowledge_repo_id
     */
    private String knowledgeRepoId;

    /**
     * project_id
     */
    private String projectId;

    /**
     * 创建时间
     */
    private Date createdOn;

    /**
     * 更新时间
     */
    private Date updatedOn;
}
