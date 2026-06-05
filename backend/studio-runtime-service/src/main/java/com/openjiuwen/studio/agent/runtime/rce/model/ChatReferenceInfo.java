/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.rce.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;

import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ChatReferenceInfo {

    private String fileId;

    private String chunkId;

    private List<String> imageIds;

    private List<String> imagePaths;

    private String title;

    private String content;

    private String updateDateTime;

    private String docType;

    private String filePath;

    private String category;

    private List<String> tags;

    private float score;

    private String subtitle;

    private String repoId;

    private String source;

}
