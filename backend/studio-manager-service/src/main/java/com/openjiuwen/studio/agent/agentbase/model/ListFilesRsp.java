/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * CSS文档列表查询响应实体类
 *
 * @since 2024-04-24
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ListFilesRsp {
    private Integer total;

    @JsonProperty("page_num")
    private Integer pageNum;

    @JsonProperty("page_size")
    private Integer pageSize;

    private List<CssFileInfo> files;
}
