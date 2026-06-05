/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service.mcp.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * UpdateBaseInfo
 *
 * @Date: 2024/5/14 14:11
 * @Description: 更新数据库的基本结构体
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateBaseInfo {
    @JsonProperty("last_updated_date")
    @JSONField(name = "last_updated_date")
    protected Date lastUpdatedDate;

    @JsonProperty("last_updated_by_user_id")
    @JSONField(name = "last_updated_by_user_id")
    protected String lastUpdatedByUserId;
}
