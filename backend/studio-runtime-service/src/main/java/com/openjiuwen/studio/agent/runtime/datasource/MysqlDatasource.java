/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.datasource;

import com.openjiuwen.studio.agent.common.dto.agent.node.DatasourceConnectionInfo;
import com.openjiuwen.studio.agent.common.utils.CryptoUtils;
import com.zaxxer.hikari.HikariDataSource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Mysql实现类，兼容mysql5.x和8.x以上版本，mariadb驱动不区分详细版本
 *
 */
@Component("mysql")
public class MysqlDatasource implements DataSource {
    @Override
    public HikariDataSource getConnectionPool(DatasourceConnectionInfo connectionInfo) {
        // 根据版本创建适合的连接池
        HikariDataSource hikariDataSource = new HikariDataSource();

        // Msql配置
        hikariDataSource.setJdbcUrl(getJdbcUrl(connectionInfo));
        hikariDataSource.setUsername(connectionInfo.getUser());
        if (StringUtils.isNotEmpty(connectionInfo.getPassword())) {
            hikariDataSource.setPassword(CryptoUtils.decrypt(connectionInfo.getPassword()));
        }
        hikariDataSource.addDataSourceProperty("useSSL", connectionInfo.isSslEnabled());
        hikariDataSource.addDataSourceProperty("autoReconnect", true);
        hikariDataSource.addDataSourceProperty("characterEncoding", "UTF-8");
        hikariDataSource.addDataSourceProperty("allowMultiQueries", true);
        hikariDataSource.addDataSourceProperty("useAffectedRows", true);
        hikariDataSource.addDataSourceProperty("allowLoadLocalInfile", false);
        hikariDataSource.addDataSourceProperty("allowUrlInLocalInfile", false);

        if (connectionInfo.getMetadata() != null && !connectionInfo.getMetadata().isEmpty()) {
            connectionInfo.getMetadata().forEach(hikariDataSource::addDataSourceProperty);
        }

        // 驱动类名，mariadb支持mysql5.x以及8.x版本，无需根据版本区分驱动
        hikariDataSource.setDriverClassName("org.mariadb.jdbc.Driver");

        return hikariDataSource;
    }

    private String getJdbcUrl(DatasourceConnectionInfo connectionInfo) {
        // 数据源支持不配置端口，只配置host
        return StringUtils.isNotEmpty(connectionInfo.getPort())
            ? String.format("jdbc:mariadb://%s:%s/%s", connectionInfo.getHost(), connectionInfo.getPort(),
            connectionInfo.getDatabaseName())
            : String.format("jdbc:mariadb://%s/%s", connectionInfo.getHost(), connectionInfo.getDatabaseName());
    }
}
