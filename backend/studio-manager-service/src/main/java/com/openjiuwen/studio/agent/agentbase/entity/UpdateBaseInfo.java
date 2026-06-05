/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString(exclude = {"updateUserName"})
@AllArgsConstructor
@NoArgsConstructor
public class UpdateBaseInfo {
    @JsonProperty("update_time")
    protected long updateTime;

    @JsonProperty("update_user_id")
    protected String updateUserId;

    @JsonProperty("update_user_name")
    protected String updateUserName;
}
