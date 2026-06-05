/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.mapper.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.openjiuwen.studio.agent.common.dto.auth.AuthInfo;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

/**
 * Tool AuthInfo JsonTypeHandler
 *
 */
@MappedTypes(AuthInfo.class)
@MappedJdbcTypes(JdbcType.VARCHAR)
public class AuthInfoHandler extends AbstractJsonHandler<AuthInfo> {
    private static final TypeReference<AuthInfo> TYPE_REF = new TypeReference<>() {};

    @Override
    protected TypeReference<AuthInfo> getTypeReference() {
        return TYPE_REF;
    }
}
