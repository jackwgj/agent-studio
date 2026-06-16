/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.agent.node;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

class DatasourceConnectionInfoTest {

    @Test
    void testAllGettersSetters() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("key1", "val1");
        DatasourceConnectionInfo obj = new DatasourceConnectionInfo()
                .setHost("localhost")
                .setPort("5432")
                .setSslEnabled(true)
                .setDatabaseName("testdb")
                .setUser("admin")
                .setPassword("secret")
                .setMetadata(metadata)
                .setSqlVersion("1.0");
        assertEquals("localhost", obj.getHost());
        assertEquals("5432", obj.getPort());
        assertTrue(obj.isSslEnabled());
        assertEquals("testdb", obj.getDatabaseName());
        assertEquals("admin", obj.getUser());
        assertEquals("secret", obj.getPassword());
        assertEquals(metadata, obj.getMetadata());
        assertEquals("1.0", obj.getSqlVersion());
    }

    @Test
    void testEquals_SameObject() {
        DatasourceConnectionInfo obj = new DatasourceConnectionInfo().setHost("localhost");
        assertEquals(obj, obj);
    }

    @Test
    void testEquals_EqualObject() {
        Map<String, Object> metadata = Map.of("k", "v");
        DatasourceConnectionInfo obj1 = new DatasourceConnectionInfo()
                .setHost("localhost").setPort("5432").setSslEnabled(true)
                .setDatabaseName("testdb").setUser("admin").setPassword("secret")
                .setMetadata(metadata).setSqlVersion("1.0");
        DatasourceConnectionInfo obj2 = new DatasourceConnectionInfo()
                .setHost("localhost").setPort("5432").setSslEnabled(true)
                .setDatabaseName("testdb").setUser("admin").setPassword("secret")
                .setMetadata(metadata).setSqlVersion("1.0");
        assertEquals(obj1, obj2);
    }

    @Test
    void testEquals_Null() {
        DatasourceConnectionInfo obj = new DatasourceConnectionInfo().setHost("localhost");
        assertNotEquals(null, obj);
    }

    @Test
    void testEquals_DifferentClass() {
        DatasourceConnectionInfo obj = new DatasourceConnectionInfo().setHost("localhost");
        assertNotEquals("string", obj);
    }

    @Test
    void testEquals_DifferentValues() {
        DatasourceConnectionInfo obj1 = new DatasourceConnectionInfo().setHost("localhost");
        DatasourceConnectionInfo obj2 = new DatasourceConnectionInfo().setHost("remotehost");
        assertNotEquals(obj1, obj2);
    }

    @Test
    void testHashCode_Consistency() {
        DatasourceConnectionInfo obj1 = new DatasourceConnectionInfo()
                .setHost("localhost").setPort("5432");
        DatasourceConnectionInfo obj2 = new DatasourceConnectionInfo()
                .setHost("localhost").setPort("5432");
        assertEquals(obj1.hashCode(), obj2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        DatasourceConnectionInfo obj = new DatasourceConnectionInfo().setHost("localhost");
        assertNotNull(obj.toString());
    }
}
