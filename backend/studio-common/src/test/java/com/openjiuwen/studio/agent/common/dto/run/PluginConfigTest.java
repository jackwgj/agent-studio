/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.common.dto.run;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PluginConfigTest {

    @Test
    void testGettersAndSetters_AllFields() {
        Map<String, Object> config = new HashMap<>();
        config.put("key", "value");
        PluginConfig pluginConfig = new PluginConfig()
                .setPluginId("plugin-1")
                .setConfig(config);

        assertEquals("plugin-1", pluginConfig.getPluginId());
        assertSame(config, pluginConfig.getConfig());
    }

    @Test
    void testSetters_ReturnThis() {
        PluginConfig pluginConfig = new PluginConfig();
        assertSame(pluginConfig, pluginConfig.setPluginId("id"));
        assertSame(pluginConfig, pluginConfig.setConfig(new HashMap<>()));
    }

    @Test
    void testEquals_SameObject() {
        PluginConfig pluginConfig = new PluginConfig().setPluginId("id");
        assertEquals(pluginConfig, pluginConfig);
    }

    @Test
    void testEquals_EqualObject() {
        PluginConfig c1 = new PluginConfig().setPluginId("id").setConfig(null);
        PluginConfig c2 = new PluginConfig().setPluginId("id").setConfig(null);
        assertEquals(c1, c2);
    }

    @Test
    void testEquals_Null() {
        PluginConfig pluginConfig = new PluginConfig();
        assertNotEquals(null, pluginConfig);
    }

    @Test
    void testEquals_DifferentClass() {
        PluginConfig pluginConfig = new PluginConfig();
        assertNotEquals("string", pluginConfig);
    }

    @Test
    void testHashCode_Consistency() {
        PluginConfig c1 = new PluginConfig().setPluginId("id");
        PluginConfig c2 = new PluginConfig().setPluginId("id");
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    void testToString_NotNull() {
        PluginConfig pluginConfig = new PluginConfig().setPluginId("id");
        assertNotNull(pluginConfig.toString());
    }
}
