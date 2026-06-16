/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.knowledge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class RepoIndexConfigTest {

    @Test
    void testBuilder() {
        RepoIndexConfig config = RepoIndexConfig.builder()
                .shareRepoLimit(10)
                .build();
        assertEquals(10, config.getShareRepoLimit());
    }

    @Test
    void testNoArgsConstructor() {
        RepoIndexConfig config = new RepoIndexConfig();
        assertNotNull(config);
        assertNull(config.getShareRepoLimit());
    }

    @Test
    void testAllArgsConstructor() {
        RepoIndexConfig config = new RepoIndexConfig(5);
        assertEquals(5, config.getShareRepoLimit());
    }

    @Test
    void testSettersAndGetters() {
        RepoIndexConfig config = new RepoIndexConfig();
        config.setShareRepoLimit(20);
        assertEquals(20, config.getShareRepoLimit());
    }

    @Test
    void testEqualsAndHashCode() {
        RepoIndexConfig c1 = RepoIndexConfig.builder().shareRepoLimit(10).build();
        RepoIndexConfig c2 = RepoIndexConfig.builder().shareRepoLimit(10).build();
        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    void testEquals_Different() {
        RepoIndexConfig c1 = RepoIndexConfig.builder().shareRepoLimit(10).build();
        RepoIndexConfig c2 = RepoIndexConfig.builder().shareRepoLimit(20).build();
        assertNotEquals(c1, c2);
    }

    @Test
    void testToString() {
        RepoIndexConfig config = RepoIndexConfig.builder().shareRepoLimit(10).build();
        assertNotNull(config.toString());
        assertEquals(true, config.toString().contains("10"));
    }
}
