/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.knowledge.lakeserach.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.openjiuwen.studio.agent.common.dto.knowledge.LakeSearchChatReferenceInfo;

import lombok.Data;

import java.util.List;

/**
 * LakeSearch搜索知识库响应实体类
 *
 * @since 2025-08-23
 */
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class SearchTextResponseBody {
    private Long total;

    private List<LakeSearchChatReferenceInfo> docList;
}
