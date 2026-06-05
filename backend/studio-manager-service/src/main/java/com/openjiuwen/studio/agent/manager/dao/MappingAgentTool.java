/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dao;

import lombok.Data;

import java.util.Date;

/**
 * assistant和tool的多对多关系映射表
 */
@Data
public class MappingAgentTool {
    /**
     * assistant_id
     */
    private String agentId;

    /**
     * tool_id
     */
    private String toolId;

    /**
     * project_id
     */
    private String projectId;

    /**
     * 是否为常驻工具
     */
    private Boolean resident;

    /**
     * 创建时间
     */
    private Date createdOn;

    /**
     * 更新时间
     */
    private Date updatedOn;
}
