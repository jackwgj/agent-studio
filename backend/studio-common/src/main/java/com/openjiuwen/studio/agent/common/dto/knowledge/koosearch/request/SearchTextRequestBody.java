/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.knowledge.koosearch.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;

import java.util.List;

/**
 * KooSearch搜索知识库请求实体类
 *
 * @since 2025-12-1
 */
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class SearchTextRequestBody {

    private String repoId;

    private String content;

    private Integer pageNum;

    private Integer pageSize;

    private String filterString;

    private String scope;

    // 多知识库搜索
    private List<String> extraRepoIds;
}
