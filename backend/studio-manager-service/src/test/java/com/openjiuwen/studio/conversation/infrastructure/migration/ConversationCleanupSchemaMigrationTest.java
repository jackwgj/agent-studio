/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.conversation.infrastructure.migration;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class ConversationCleanupSchemaMigrationTest {
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
}
