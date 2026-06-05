/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

import org.hibernate.validator.constraints.Length;

import java.util.List;


@Data
@Accessors(chain = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KnowledgeIdTags {

    @JsonProperty("id")
    @Length(max = 1024)
    private String id = null;

    @JsonProperty("tags")
    @Valid
    @Size()
    private List<@Length() String> tags = null;

}
