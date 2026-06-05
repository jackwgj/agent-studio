/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.dto.knowledge;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * FAQ检索条件
 *
 * @since 2025-04-18
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FaqSearchCriteria {
    private String id;

    @JsonProperty("repo_id")
    private String repoId;

    private String question;

    @JsonProperty("page_num")
    private Integer pageNum;

    @JsonProperty("page_size")
    private Integer pageSize;
}
