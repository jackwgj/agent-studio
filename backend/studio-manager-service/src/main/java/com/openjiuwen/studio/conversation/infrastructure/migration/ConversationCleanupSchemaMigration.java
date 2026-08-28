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
 * <p>默认随 Manager 启动自动执行，可通过
 * {@code conversation.cleanup.schema-migration-enabled=false} 显式禁用。
 * 每列均先通过 JDBC metadata 检查，因而服务重复启动不会重复执行 DDL。</p>
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "conversation.cleanup.schema-migration-enabled", havingValue = "true",
    matchIfMissing = true)
public class ConversationCleanupSchemaMigration implements ApplicationRunner {
    private static final Map<String, String> CONVERSATION_COLUMNS = new LinkedHashMap<>();
    private static final Map<String, String> RUN_COLUMNS = new LinkedHashMap<>();
    private static final Map<String, String> SUB_RUN_COLUMNS = new LinkedHashMap<>();

    static {
        CONVERSATION_COLUMNS.put("cleanup_status",
            "ALTER TABLE t_conversation ADD COLUMN cleanup_status VARCHAR(16) NOT NULL DEFAULT 'NONE' "
                + "COMMENT '资源清理状态：NONE/PENDING/PROCESSING/DONE/FAILED'");
        CONVERSATION_COLUMNS.put("cleanup_attempts",
            "ALTER TABLE t_conversation ADD COLUMN cleanup_attempts INT NOT NULL DEFAULT 0 "
                + "COMMENT '资源清理尝试次数'");
        CONVERSATION_COLUMNS.put("cleanup_updated_at",
            "ALTER TABLE t_conversation ADD COLUMN cleanup_updated_at TIMESTAMP NULL "
                + "COMMENT '资源清理状态更新时间'");
        CONVERSATION_COLUMNS.put("cleanup_error",
            "ALTER TABLE t_conversation ADD COLUMN cleanup_error VARCHAR(1024) NULL "
                + "COMMENT '最近一次资源清理错误'");

        RUN_COLUMNS.put("run_id", "ALTER TABLE t_conversation_run ADD COLUMN run_id VARCHAR(64) NULL");
        RUN_COLUMNS.put("parent_run_id", "ALTER TABLE t_conversation_run ADD COLUMN parent_run_id VARCHAR(64) NULL");
        RUN_COLUMNS.put("tool_name", "ALTER TABLE t_conversation_run ADD COLUMN tool_name VARCHAR(255) NULL");
        RUN_COLUMNS.put("execution_type", "ALTER TABLE t_conversation_run ADD COLUMN execution_type VARCHAR(32) NULL");
        RUN_COLUMNS.put("workflow_id", "ALTER TABLE t_conversation_run ADD COLUMN workflow_id VARCHAR(64) NULL");
        RUN_COLUMNS.put("node_id", "ALTER TABLE t_conversation_run ADD COLUMN node_id VARCHAR(128) NULL");
        RUN_COLUMNS.put("event_index", "ALTER TABLE t_conversation_run ADD COLUMN event_index BIGINT NULL");

        SUB_RUN_COLUMNS.put("run_id", "ALTER TABLE t_conversation_sub_run ADD COLUMN run_id VARCHAR(64) NULL");
        SUB_RUN_COLUMNS.put("parent_run_id", "ALTER TABLE t_conversation_sub_run ADD COLUMN parent_run_id VARCHAR(64) NULL");
        SUB_RUN_COLUMNS.put("tool_name", "ALTER TABLE t_conversation_sub_run ADD COLUMN tool_name VARCHAR(255) NULL");
        SUB_RUN_COLUMNS.put("execution_type", "ALTER TABLE t_conversation_sub_run ADD COLUMN execution_type VARCHAR(32) NULL");
        SUB_RUN_COLUMNS.put("workflow_id", "ALTER TABLE t_conversation_sub_run ADD COLUMN workflow_id VARCHAR(64) NULL");
        SUB_RUN_COLUMNS.put("node_id", "ALTER TABLE t_conversation_sub_run ADD COLUMN node_id VARCHAR(128) NULL");
        SUB_RUN_COLUMNS.put("event_index", "ALTER TABLE t_conversation_sub_run ADD COLUMN event_index BIGINT NULL");
    }

    private final JdbcTemplate jdbcTemplate;

    public ConversationCleanupSchemaMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) throws SQLException {
        try (var connection = jdbcTemplate.getDataSource().getConnection()) {
            DatabaseMetaData metadata = connection.getMetaData();
            createConversationTables();
            ensureColumns(metadata, connection.getCatalog(), "t_conversation", CONVERSATION_COLUMNS);
            ensureColumns(metadata, connection.getCatalog(), "t_conversation_run", RUN_COLUMNS);
            ensureColumns(metadata, connection.getCatalog(), "t_conversation_sub_run", SUB_RUN_COLUMNS);
            backfillLegacyRunIds(metadata, connection.getCatalog());
        }
    }

    private void createConversationTables() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS t_conversation (conversation_id VARCHAR(64) PRIMARY KEY, "
            + "title VARCHAR(256), project_id VARCHAR(64) NOT NULL, workspace_id VARCHAR(64), domain_id VARCHAR(64), "
            + "owner_domain_id VARCHAR(64), owner_user_id VARCHAR(64), source VARCHAR(32), status VARCHAR(32), "
            + "creator VARCHAR(64), creator_id VARCHAR(64), updater VARCHAR(64), updater_id VARCHAR(64), "
            + "created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP, updated_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
            + "deleted TINYINT NOT NULL DEFAULT 0)");
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS t_conversation_run (id BIGINT AUTO_INCREMENT PRIMARY KEY, "
            + "run_id VARCHAR(64) NOT NULL, parent_run_id VARCHAR(64), conversation_id VARCHAR(64) NOT NULL, "
            + "role VARCHAR(16) NOT NULL, content TEXT, tool_id VARCHAR(84), tool_name VARCHAR(255), tool_args TEXT, "
            + "file_ids TEXT, event VARCHAR(32), execution_type VARCHAR(32), workflow_id VARCHAR(64), node_id VARCHAR(128), "
            + "event_index BIGINT, agent_id VARCHAR(64), model_deployment_id VARCHAR(80), total_tokens VARCHAR(64), "
            + "prompt_tokens VARCHAR(64), completion_tokens VARCHAR(64), project_id VARCHAR(64) NOT NULL, "
            + "workspace_id VARCHAR(64), domain_id VARCHAR(64), creator VARCHAR(64), creator_id VARCHAR(64), "
            + "updater VARCHAR(64), updater_id VARCHAR(64), created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
            + "updated_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP, deleted TINYINT NOT NULL DEFAULT 0)");
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS t_conversation_sub_run (id BIGINT AUTO_INCREMENT PRIMARY KEY, "
            + "run_id VARCHAR(64) NOT NULL, parent_run_id VARCHAR(64), conversation_id VARCHAR(64) NOT NULL, "
            + "agent_id VARCHAR(64), role VARCHAR(16) NOT NULL, content TEXT, tool_id VARCHAR(84), tool_name VARCHAR(255), "
            + "tool_args TEXT, file_ids TEXT, event VARCHAR(32), execution_type VARCHAR(32), workflow_id VARCHAR(64), "
            + "node_id VARCHAR(128), event_index BIGINT, total_tokens VARCHAR(64), prompt_tokens VARCHAR(64), "
            + "completion_tokens VARCHAR(64), project_id VARCHAR(64) NOT NULL, workspace_id VARCHAR(64), domain_id VARCHAR(64), "
            + "creator VARCHAR(64), creator_id VARCHAR(64), updater VARCHAR(64), updater_id VARCHAR(64), "
            + "created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP, updated_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
            + "deleted TINYINT NOT NULL DEFAULT 0)");
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS t_conversation_workflow (id BIGINT AUTO_INCREMENT PRIMARY KEY, "
            + "conversation_id VARCHAR(64) NOT NULL, tool_id VARCHAR(84), parent_run_id VARCHAR(64), workflow_id VARCHAR(64), "
            + "node_id VARCHAR(128), node_name VARCHAR(255), node_type VARCHAR(64), node_index INT, status VARCHAR(32), "
            + "input_content TEXT, output_content TEXT, error_code VARCHAR(64), error_message TEXT, started_on TIMESTAMP NULL, "
            + "finished_on TIMESTAMP NULL, project_id VARCHAR(64), workspace_id VARCHAR(64), domain_id VARCHAR(64), "
            + "creator_id VARCHAR(64), created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
            + "updated_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP, deleted TINYINT NOT NULL DEFAULT 0)");
    }

    private void ensureColumns(DatabaseMetaData metadata, String catalog, String table,
                               Map<String, String> columns) throws SQLException {
        for (Map.Entry<String, String> column : columns.entrySet()) {
            if (!columnExists(metadata, catalog, table, column.getKey())) {
                jdbcTemplate.execute(column.getValue());
                log.info("Added conversation schema column: {}.{}", table, column.getKey());
            }
        }
    }

    private void backfillLegacyRunIds(DatabaseMetaData metadata, String catalog) throws SQLException {
        if (columnExists(metadata, catalog, "t_conversation_run", "execution_id")) {
            jdbcTemplate.update("UPDATE t_conversation_run SET run_id = execution_id WHERE run_id IS NULL");
        }
        if (columnExists(metadata, catalog, "t_conversation_sub_run", "sub_execution_id")) {
            jdbcTemplate.update("UPDATE t_conversation_sub_run SET run_id = sub_execution_id WHERE run_id IS NULL");
        }
        if (columnExists(metadata, catalog, "t_conversation_sub_run", "execution_id")) {
            jdbcTemplate.update("UPDATE t_conversation_sub_run SET parent_run_id = execution_id "
                + "WHERE parent_run_id IS NULL");
        }
    }

    private boolean columnExists(DatabaseMetaData metadata, String catalog, String table, String column)
        throws SQLException {
        try (ResultSet columns = metadata.getColumns(catalog, null, table, column)) {
            if (columns.next()) {
                return true;
            }
        }
        try (ResultSet columns = metadata.getColumns(catalog, null, table.toUpperCase(Locale.ROOT),
            column.toUpperCase(Locale.ROOT))) {
            return columns.next();
        }
    }
}
