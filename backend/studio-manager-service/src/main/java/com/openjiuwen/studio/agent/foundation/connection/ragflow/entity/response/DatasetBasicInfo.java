/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.foundation.connection.ragflow.entity.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;

/**
 * RagFlow里的数据集基本信息
 *
 * @since 2025-08-23
 */
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DatasetBasicInfo {

    private String id;

    private String name;

    private String description;

    private String avatar;

    private String status;

    private int chunkCount;

    private long createTime;

    private int documentCount;

    private String chunkMethod;

    private String permission;

    private double similarityThreshold;

    private long updateTime;

    private double vectorSimilarityWeight;

}
