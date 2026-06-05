/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2023. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class McpServerRatingEntity {
    /**
     * 服务id
     */
    private String serverId;

    /**
     * 租户id
     */
    private String tenantId;

    /**
     * 评分
     */
    private int score;

    private Timestamp createdDate;

    private Timestamp updatedDate;
}

