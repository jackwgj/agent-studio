/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.enums;

import lombok.Getter;

@Getter
public enum KnowledgeBaseConnectorParamType {

    STRING("STRING"),
    SECRET("SECRET"),
    BOOLEAN("BOOLEAN"),
    FILE("FILE");

    private final String value;

    KnowledgeBaseConnectorParamType(String value) {
        this.value = value;
    }

}
