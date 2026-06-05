/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.datasource;

import com.openjiuwen.studio.agent.common.dto.agent.node.DatasourceConnectionInfo;
import com.zaxxer.hikari.HikariDataSource;

/**
 * DataSource接口类
 *
 */
public interface DataSource {
    /**
     * 根据配置实例化连接池
     *
     * @param connectionInfo 数据源连接配置
     * @return 数据源连接池
     */
    HikariDataSource getConnectionPool(DatasourceConnectionInfo connectionInfo);
}
