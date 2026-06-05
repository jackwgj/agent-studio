/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.knowledge;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Css搜索知识库请求实体类
 *
 * @since 2024-04-29
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchTextReq {
    @JsonProperty("repo_id")
    private String repoId;

    private String content;

    @JsonProperty("page_num")
    private Integer pageNum;

    @JsonProperty("page_size")
    private Integer pageSize;

    @JsonProperty("filter_string")
    private String filterString;

    private String scope;

    // 多知识库搜索
    @JsonProperty("extra_repo_ids")
    private List<String> extraRepoIds;
}
