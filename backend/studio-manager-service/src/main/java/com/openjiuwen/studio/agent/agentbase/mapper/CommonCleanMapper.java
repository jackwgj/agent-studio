/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 通用清理 Mapper 接口
 * 支持动态表名的删除操作
 *
 * @since 2025-12-14
 */
@Mapper
public interface CommonCleanMapper {

    /**
     * 动态删除指定表的数据
     *
     * @param tableName 表名 (注意：SQL 中要使用 ${tableName} 接收)
     * @param domainId 租户ID (注意：SQL 中要使用 #{domainId} 接收)
     * @return 影响的行数
     */
    int deleteDataByDomainId(@Param("tableName") String tableName, @Param("domainId") String domainId);
}