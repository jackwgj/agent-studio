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
 * KooSearch知识文件的分片信息
 *
 * @since 2024-08-07
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileDocInfo {
    private String id;

    private String timestamp;

    private String title;

    private String content;

    @JsonProperty("page_num")
    private Long pageNum;

    @JsonProperty("component_num")
    private Long componentNum;
}
