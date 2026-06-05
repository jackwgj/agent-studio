/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Accessors(chain = true)
@TableName(value = "t_quick_command", autoResultMap = true)
public class QuickCommandEntity {

    @JsonProperty("id")
    private String id;

    @JsonProperty("agent_id")
    private String agentId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("active")
    private Integer active;

    @JsonProperty("description")
    private String description;

    @JsonProperty("content")
    private String content;

    @JsonProperty("display_no")
    private Integer displayNo;

    @JsonProperty("recommend")
    private Integer recommend;

    @JsonProperty("domain_id")
    private String domainId;

    @JsonProperty("created_date")
    private Timestamp createdDate;

    @JsonProperty("created_by_user_id")
    private String createdByUserId;

    @JsonProperty("last_updated_date")
    private Timestamp lastUpdatedDate;

    @JsonProperty("last_updated_by_user_id")
    private String lastUpdatedByUserId;
}
