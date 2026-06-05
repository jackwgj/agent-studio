/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.mapper.ros;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ResourceCleanMapper {
    List<String> selectByDomain(@Param("tableName") String tableName, @Param("idColumn") String idColumn,
        @Param("domainColumn") String domainColumn, @Param("domainId") String domainId);

    void batchDeleteByIds(@Param("tableName") String tableName, @Param("idColumn") String idColumn,
        @Param("ids") List<String> ids);

    int countByDomain(@Param("tableName") String tableName, @Param("domainColumn") String domainColumn,
        @Param("domainId") String domainId);

    int countByMasterIds(@Param("tableName") String tableName, @Param("idColumn") String idColumn,
        @Param("ids") List<String> ids);
}
