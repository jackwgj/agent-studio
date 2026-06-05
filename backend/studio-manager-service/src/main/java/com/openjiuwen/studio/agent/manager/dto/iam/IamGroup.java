/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto.iam;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description iamGroups对象
 * @since 2025/9/11
 **/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IamGroup {
    @JsonProperty("description")
    @JSONField(name = "description")
    private String description;

    @JsonProperty("id")
    @JSONField(name = "id")
    private String id;

    @JsonProperty("domain_id")
    @JSONField(name = "domain_id")
    private String domainId;

    @JsonProperty("name")
    @JSONField(name = "name")
    private String name;

    @JsonProperty("links")
    @JSONField(name = "links")
    private IamLinks links;

    @JsonProperty("create_time")
    @JSONField(name = "create_time")
    private String createTime;
}
