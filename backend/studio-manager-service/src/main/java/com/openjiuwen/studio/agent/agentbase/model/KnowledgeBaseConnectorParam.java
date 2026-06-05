/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.model;

import com.openjiuwen.studio.agent.agentbase.enums.KnowledgeBaseConnectorParamType;

import lombok.Data;

@Data
public class KnowledgeBaseConnectorParam {

    private String code;

    private String name;

    private String need;

    private KnowledgeBaseConnectorParamType type;

    private String remark;

    private String pattern;

    private String value;

}
