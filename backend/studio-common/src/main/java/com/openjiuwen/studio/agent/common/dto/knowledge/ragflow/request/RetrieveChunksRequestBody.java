/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.knowledge.ragflow.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;

import java.util.List;

/**
 * 检索切片的请求体
 *
 * @since 2025-08-23
 */
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RetrieveChunksRequestBody {

    private String question;

    private List<String> datasetIds;

    private List<String> documentIds;

    private Integer page;

    private Integer pageSize;

    private Float similarityThreshold;

    private Float vectorSimilarityWeight;

    private Integer topK;

    private String rerankId;

    private boolean keyword = false;

    private boolean highlight;

    private List<String> crossLanguages;

}
