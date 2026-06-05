/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.mapper.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.openjiuwen.studio.agent.manager.dto.LongTermMemoryStrategy;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.util.List;

@MappedTypes(LongTermMemoryStrategy.class)
@MappedJdbcTypes(JdbcType.VARCHAR)
public class LongTermMemoryStrategyHandler extends AbstractJsonHandler<List<LongTermMemoryStrategy>> {
    private static final TypeReference<List<LongTermMemoryStrategy>> TYPE_REF = new TypeReference<>() {
    };

    @Override
    protected TypeReference<List<LongTermMemoryStrategy>> getTypeReference() {
        return TYPE_REF;
    }
}
