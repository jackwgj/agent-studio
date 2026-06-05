/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.knowledge.ragflow.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.openjiuwen.studio.agent.common.dto.knowledge.RetrieveChunkInfo;

import lombok.Data;

import java.util.List;

/**
 * 检索切片的响应消息体
 *
 * @since 2025-08-23
 */
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class RetrieveChunksResponseBody {

    private List<RetrieveChunkInfo> chunks;

    private List<RetrieveDocumentInfo> docAggs;

    private int total;
}
