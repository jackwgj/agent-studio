/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.knowledge.koosearch.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KooSearchChatReferenceInfo {

    private String fileId;

    private String chunkId;

    private String title;

    private String content;

    private String docType;

    private String filePath;

    private String category;

    private Float score;

    private String subtitle;

    String repoId;
}
