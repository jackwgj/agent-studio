/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import org.springframework.validation.annotation.Validated;

@ApiModel(description = "ListThirdPartyKnowledgeBaseConnectionsQo: converted from multi query params")

@Validated
@Data
public class ListThirdPartyKnowledgeBaseConnectionsQo {

    @JsonProperty("workspace_id")
    @NotBlank
    private String workspaceId = null;

    @JsonProperty("name")
    @Length(max = 50)
    private String name = null;

    @JsonProperty("status")
    private String status = null;

    @JsonProperty("connector_id")
    @Length(max = 100)
    private String connectorId = null;

    @JsonProperty("offset")
    @Range(min = 0L, max = 10000L)
    private Integer offset = 0;

    @JsonProperty("limit")
    @Range(min = 1L, max = 1000L)
    private Integer limit = 10;

}
