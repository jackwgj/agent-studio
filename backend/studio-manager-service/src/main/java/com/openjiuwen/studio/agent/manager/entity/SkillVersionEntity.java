/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SkillVersionEntity {
    @JsonProperty("skill_id")
    private String id;

    @JsonProperty("skill_id")
    private String skillId;

    @JsonProperty("version_name")
    private String versionName;

    @JsonProperty("used")
    private Integer used;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("obs_path")
    private String obsPath;

    @JsonProperty("creator_id")
    private String creatorId;

    @JsonProperty("creator_name")
    private String creatorName;

    @JsonProperty("created_at")
    private Long createdAt;
}
