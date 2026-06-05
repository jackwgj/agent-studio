/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.models.knowledge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 知识文档分层规则列表查询响应体
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ListSegmentRuleResp {
    private List<SegmentRule> rules;
}
