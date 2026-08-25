/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.conversation.infrastructure.migration;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 对话资源清理字段的显式、幂等迁移器。
 *
 * <p>默认关闭，部署迁移窗口通过
 * {@code conversation.cleanup.schema-migration-enabled=true} 显式启用一次。
 * 每列均先通过 JDBC metadata 检查，因而服务重复启动不会重复执行 DDL。</p>
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "conversation.cleanup.schema-migration-enabled", havingValue = "true")
public class ConversationCleanupSchemaMigration implements ApplicationRunner {
    private static final String TABLE = "t_conversation";

    private static final Map<String, String> COLUMNS = new LinkedHashMap<>();

    static {
        COLUMNS.put("cleanup_status",
            "ALTER TABLE t_conversation ADD COLUMN cleanup_status VARCHAR(16) NOT NULL DEFAULT 'NONE' "
                + "COMMENT '资源清理状态：NONE/PENDING/PROCESSING/DONE/FAILED'");
        COLUMNS.put("cleanup_attempts",
            "ALTER TABLE t_conversation ADD COLUMN cleanup_attempts INT NOT NULL DEFAULT 0 "
                + "COMMENT '资源清理尝试次数'");
        COLUMNS.put("cleanup_updated_at",
            "ALTER TABLE t_conversation ADD COLUMN cleanup_updated_at TIMESTAMP NULL "
                + "COMMENT '资源清理状态更新时间'");
        COLUMNS.put("cleanup_error",
            "ALTER TABLE t_conversation ADD COLUMN cleanup_error VARCHAR(1024) NULL "
                + "COMMENT '最近一次资源清理错误'");
    }

    private final JdbcTemplate jdbcTemplate;

    public ConversationCleanupSchemaMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) throws SQLException {
        try (var connection = jdbcTemplate.getDataSource().getConnection()) {
            DatabaseMetaData metadata = connection.getMetaData();
            for (Map.Entry<String, String> column : COLUMNS.entrySet()) {
                if (!columnExists(metadata, connection.getCatalog(), column.getKey())) {
                    jdbcTemplate.execute(column.getValue());
                    log.info("Added conversation cleanup column: {}", column.getKey());
                }
            }
        }
    }

    private boolean columnExists(DatabaseMetaData metadata, String catalog, String column) throws SQLException {
        try (ResultSet columns = metadata.getColumns(catalog, null, TABLE, column)) {
            if (columns.next()) {
                return true;
            }
        }
        try (ResultSet columns = metadata.getColumns(catalog, null, TABLE.toUpperCase(Locale.ROOT),
            column.toUpperCase(Locale.ROOT))) {
            return columns.next();
        }
    }
}
