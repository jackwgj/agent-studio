/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.runtime.datasource.DatasourceFactory;
import com.openjiuwen.studio.agent.runtime.dto.DatasourceExecuteReq;
import com.openjiuwen.studio.agent.runtime.dto.DatasourceExecuteResp;
import com.zaxxer.hikari.HikariDataSource;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 数据源运行态服务
 *
 */
@Slf4j
@Service
public class DatasourceRuntimeService implements IDatasourceRuntimeService {
    @Autowired
    private DatasourceFactory datasourceFactory;

    @Value("${datasource.execute.max-response}")
    private int maxResponse;

    @Value("${datasource.execute.timeout}")
    private int executeTimeout;

    @Override
    public DatasourceExecuteResp executeDatasource(String datasourceId, DatasourceExecuteReq body) {
        // 获取数据库id连接池
        HikariDataSource dataSource = datasourceFactory.getConnectionPool(datasourceId, body.isShouldByCache());
        return executeSql(body.getQuery(), dataSource, body.getConditions());
    }

    /**
     * 执行sql （建表，插入记录，更新记录等）
     *
     * @param query sql语句
     * @param dataSource 参数
     * @return 执行结果
     */
    private DatasourceExecuteResp executeSql(String query, HikariDataSource dataSource, List<String> conditions) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                DatasourceExecuteResp datasourceExecuteResp = new DatasourceExecuteResp();
                if (conditions != null && !conditions.isEmpty()) {
                    AtomicInteger index = new AtomicInteger(1);
                    conditions.forEach(condition -> {
                        try {
                            statement.setString(index.getAndIncrement(), condition);
                        } catch (SQLException e) {
                            log.error("SQL prepared statement failed! Query: {}", query, e);
                            throw new AgentStudioException(StudioError.SQL_EXECUTE_FAILED);
                        }
                    });
                }
                // 超时配置
                statement.setQueryTimeout(executeTimeout);

                try (ResultSet resultSet = statement.executeQuery()) {
                    convertResultSetToExecuteResp(resultSet, datasourceExecuteResp);
                }
                while (statement.getMoreResults() || statement.getUpdateCount() != -1) {
                    try (ResultSet resultSet = statement.getResultSet()) {
                        if (resultSet != null) {
                            // 避免更新计数（非结果集）
                            convertResultSetToExecuteResp(resultSet, datasourceExecuteResp);
                        }
                    }
                }
                datasourceExecuteResp.setRowNum(datasourceExecuteResp.getOutputList().size());
                return datasourceExecuteResp;
            } catch (SQLTimeoutException e) {
                log.error("SQL execution timed out! Query: {}", query, e);
                throw new AgentStudioException(StudioError.SQL_EXECUTE_FAILED);
            } catch (SQLException e) {
                log.error("execute failed! please check your sql");
                throw new AgentStudioException(StudioError.SQL_EXECUTE_FAILED);
            }
        } catch (SQLException e) {
            log.error("connection to sql failed! please check your datasource config. SQL connection error! bad sql {}", query);
            throw new AgentStudioException(StudioError.SQL_EXECUTE_FAILED);
        }
    }

    private void convertResultSetToExecuteResp(ResultSet resultSet, DatasourceExecuteResp datasourceExecuteResp)
        throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();

        // 单次执行最大返回值限制
        int columnCount = Math.min(metaData.getColumnCount(), maxResponse);
        while (resultSet.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnLabel(i);
                Object value = resultSet.getObject(i);
                row.put(columnName, value);
            }
            datasourceExecuteResp.getOutputList().add(row);
        }
    }
}
