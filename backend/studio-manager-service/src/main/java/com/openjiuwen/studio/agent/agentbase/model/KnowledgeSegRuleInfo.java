/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 知识文件分层规则实体类
 *
 * @since 2025-04-12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeSegRuleInfo {

    private String ruleId;

    private String knowledgeBaseConnectionId;
}
