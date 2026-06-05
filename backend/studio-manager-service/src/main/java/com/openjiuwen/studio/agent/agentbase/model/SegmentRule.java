/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 知识文档分层规则
 *
 * @since 2025-04-12
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SegmentRule {
    private String id;

    private List<String> regexs;
}
