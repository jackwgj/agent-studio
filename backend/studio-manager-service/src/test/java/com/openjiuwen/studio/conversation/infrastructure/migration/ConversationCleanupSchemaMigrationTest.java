/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.conversation.infrastructure.migration;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ConversationCleanupSchemaMigrationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withBean(JdbcTemplate.class, () -> mock(JdbcTemplate.class))
        .withUserConfiguration(MigrationConfiguration.class);

    @Test
    void shouldEnableMigrationByDefaultAndAllowExplicitDisable() {
        contextRunner.run(context -> assertThat(context)
            .hasSingleBean(ConversationCleanupSchemaMigration.class));

        contextRunner
            .withPropertyValues("conversation.cleanup.schema-migration-enabled=false")
            .run(context -> assertThat(context)
                .doesNotHaveBean(ConversationCleanupSchemaMigration.class));
    }

    @Test
    void shouldBackfillHistoricalRowsAndRemainRepeatable() throws Exception {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:conversation_cleanup;MODE=MySQL;DB_CLOSE_DELAY=-1");
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        jdbc.execute("DROP TABLE IF EXISTS t_conversation");
        jdbc.execute("CREATE TABLE t_conversation (conversation_id VARCHAR(64) PRIMARY KEY)");
        jdbc.update("INSERT INTO t_conversation(conversation_id) VALUES (?)", "historical");

        ConversationCleanupSchemaMigration migration = new ConversationCleanupSchemaMigration(jdbc);
        migration.run(null);
        migration.run(null);

        assertThat(jdbc.queryForObject(
            "SELECT cleanup_status FROM t_conversation WHERE conversation_id = 'historical'", String.class))
            .isEqualTo("NONE");
        assertThat(jdbc.queryForObject(
            "SELECT cleanup_attempts FROM t_conversation WHERE conversation_id = 'historical'", Integer.class))
            .isZero();
    }

    @Test
    void shouldCreateCanonicalConversationTablesOnOfficialBaselineAndRemainRepeatable() throws Exception {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:conversation_baseline;MODE=MySQL;DB_CLOSE_DELAY=-1");
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        ConversationCleanupSchemaMigration migration = new ConversationCleanupSchemaMigration(jdbc);

        migration.run(null);
        migration.run(null);

        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM t_conversation", Integer.class)).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM t_conversation_run", Integer.class)).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM t_conversation_sub_run", Integer.class)).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM t_conversation_workflow", Integer.class)).isZero();
    }

    @Test
    void shouldBackfillLegacyExecutionColumnsIntoCanonicalRunIdentity() throws Exception {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:conversation_legacy;MODE=MySQL;DB_CLOSE_DELAY=-1");
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        jdbc.execute("CREATE TABLE t_conversation_run (id BIGINT AUTO_INCREMENT PRIMARY KEY, "
            + "execution_id VARCHAR(64), conversation_id VARCHAR(64), role VARCHAR(16), project_id VARCHAR(64))");
        jdbc.execute("CREATE TABLE t_conversation_sub_run (id BIGINT AUTO_INCREMENT PRIMARY KEY, "
            + "sub_execution_id VARCHAR(64), execution_id VARCHAR(64), conversation_id VARCHAR(64), "
            + "role VARCHAR(16), project_id VARCHAR(64))");
        jdbc.update("INSERT INTO t_conversation_run(execution_id, conversation_id, role, project_id) VALUES (?,?,?,?)",
            "root-1", "c1", "assistant", "p1");
        jdbc.update("INSERT INTO t_conversation_sub_run(sub_execution_id, execution_id, conversation_id, role, project_id) "
            + "VALUES (?,?,?,?,?)", "child-1", "root-1", "c1", "assistant", "p1");

        ConversationCleanupSchemaMigration migration = new ConversationCleanupSchemaMigration(jdbc);
        migration.run(null);

        assertThat(jdbc.queryForObject("SELECT run_id FROM t_conversation_run WHERE id=1", String.class))
            .isEqualTo("root-1");
        assertThat(jdbc.queryForObject("SELECT run_id FROM t_conversation_sub_run WHERE id=1", String.class))
            .isEqualTo("child-1");
        assertThat(jdbc.queryForObject("SELECT parent_run_id FROM t_conversation_sub_run WHERE id=1", String.class))
            .isEqualTo("root-1");
    }

    @Configuration(proxyBeanMethods = false)
    @Import(ConversationCleanupSchemaMigration.class)
    static class MigrationConfiguration {
    }
}
