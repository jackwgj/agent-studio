/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.knowledge;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;

import java.util.List;

/**
 * 检索结果中的chunk信息
 *
 * @since 2025-08-23
 */
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class RetrieveChunkInfo {

    private String content;

    private String contentLtks;

    private String datasetId;

    private String docTypeKwd;

    private String documentId;

    private String contentWithWeight;

    private String documentKeyword;

    private String highlight;

    private String id;

    private String imageId;

    private List<String> importantKeywords;

    private String kbId;

    private List<List<Integer>> positions;

    private float similarity;

    private float termSimilarity;

    private float vectorSimilarity;
}
