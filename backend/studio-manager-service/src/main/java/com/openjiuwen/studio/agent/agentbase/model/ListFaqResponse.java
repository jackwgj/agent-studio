/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.model;

import com.openjiuwen.studio.agent.common.dto.knowledge.FaqInfo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 查询faq列表响应体
 *
 * @since 2025-04-18
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ListFaqResponse {
    private List<FaqInfo> records;

    private Integer total;

    private Integer size;

    private Integer current;

    private Integer pages;
}
