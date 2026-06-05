/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.common.dto.knowledge.general.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;

import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class RetrieveQueryRequestBody {

    private List<String> knowledgeBaseIds;

    private String query;

    private String method;

    private Integer offset;

    private Integer limit;

    private Integer topK;

    private Float searchThreshold;


}
