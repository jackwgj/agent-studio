/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.openjiuwen.studio.agent.common.dto.knowledge.FileDocInfo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * KooSearch知识文件的分片列表
 *
 * @since 2024-08-07
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ListFileDocsRsp {
    private Long total;

    private List<FileDocInfo> docs;

    @JsonProperty("page_num")
    private Long pageNum;

    @JsonProperty("page_size")
    private Long pageSize;
}
