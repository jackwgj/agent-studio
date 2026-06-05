/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 知识检索实体类
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeTestEntity {
    /**
     * 命中测试记录id，UUID
     */
    private String recordId;

    /**
     * 知识库id
     */
    private String knowledgeRepoId;

    /**
     * 租户项目id
     */
    private String projectId;

    /**
     * 命中测试的问题
     */
    private String query;

    /**
     * 创建时间
     */
    private Date createdOn;
}
