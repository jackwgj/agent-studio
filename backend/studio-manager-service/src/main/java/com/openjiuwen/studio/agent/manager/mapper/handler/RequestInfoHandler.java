/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.mapper.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.openjiuwen.studio.agent.manager.dto.RequestInfo;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

/**
 * Tool RequestInfo JsonTypeHandler
 *
 */
@MappedTypes(RequestInfo.class)
@MappedJdbcTypes(JdbcType.VARCHAR)
public class RequestInfoHandler extends AbstractJsonHandler<RequestInfo> {
    private static final TypeReference<RequestInfo> TYPE_REF = new TypeReference<>() {};

    @Override
    protected TypeReference<RequestInfo> getTypeReference() {
        return TYPE_REF;
    }
}
