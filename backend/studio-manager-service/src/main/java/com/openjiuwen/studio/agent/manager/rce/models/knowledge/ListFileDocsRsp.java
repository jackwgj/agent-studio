/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.models.knowledge;

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
