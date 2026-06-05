/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.mapper.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.openjiuwen.studio.agent.manager.dto.McpServerTools;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

/**
 * 功能描述
 *
 */
@MappedTypes(McpServerTools.class)
@MappedJdbcTypes(JdbcType.VARCHAR)
public class McpServerToolsHandler extends AbstractJsonHandler<McpServerTools> {
    private static final TypeReference<McpServerTools> TYPE_REF = new TypeReference<>() {};

    @Override
    protected TypeReference<McpServerTools> getTypeReference() {
        return TYPE_REF;
    }
}
