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
 * @description iamRole对象
 * @since 2025/9/11
 **/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IamRole {
    @JsonProperty("flag")
    @JSONField(name = "flag")
    private String flag;

    @JsonProperty("description_cn")
    @JSONField(name = "description_cn")
    private String descriptionCn;

    @JsonProperty("catalog")
    @JSONField(name = "catalog")
    private String catalog;

    @JsonProperty("name")
    @JSONField(name = "name")
    private String name;

    @JsonProperty("description")
    @JSONField(name = "description")
    private String description;

    @JsonProperty("links")
    @JSONField(name = "links")
    private Object links;

    @JsonProperty("id")
    @JSONField(name = "id")
    private String id;

    @JsonProperty("display_name")
    @JSONField(name = "display_name")
    private String displayName;

    @JsonProperty("type")
    @JSONField(name = "type")
    private String type;

    @JsonProperty("policy")
    @JSONField(name = "policy")
    private Object policy;

    @JsonProperty("updated_time")
    @JSONField(name = "updated_time")
    private String updatedTime;

    @JsonProperty("created_time")
    @JSONField(name = "created_time")
    private String createdTime;
}
