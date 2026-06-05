/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.knowledge.lakeserach.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;

import java.util.List;

/**
 * 知识库标签列表响应信息
 *
 * @since 2025-08-23
 */
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KnowledgeBaseTagsResponse {

    private Long total;

    private List<LakeSearchTagInfo> labelList;

}
