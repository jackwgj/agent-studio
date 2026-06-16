/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */
package com.openjiuwen.studio.agent.manager.service.md;

import com.openjiuwen.studio.agent.manager.entity.md.MdInterfaceProtocol;
import com.openjiuwen.studio.agent.manager.mapper.md.MdInterfaceProtocolMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ModelInterfaceProtocolServiceTest {

    @Mock
    private MdInterfaceProtocolMapper mapper;

    @InjectMocks
    private ModelInterfaceProtocolService modelInterfaceProtocolService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(modelInterfaceProtocolService, "pocAgentBuilderEnable", false);
        ReflectionTestUtils.setField(modelInterfaceProtocolService, "hisEnable", false);
        ReflectionTestUtils.setField(modelInterfaceProtocolService, "delayTime", 0);
    }

    @Test
    void testQueryProtocols_NoFilter() {
        List<MdInterfaceProtocol> protocols = new ArrayList<>();
        MdInterfaceProtocol p1 = new MdInterfaceProtocol();
        p1.setModelTypes("LLM");
        p1.setVisible("true");
        protocols.add(p1);

        when(mapper.queryAllProtocols()).thenReturn(protocols);
        ReflectionTestUtils.setField(modelInterfaceProtocolService, "lastRefreshTime", 0L);

        List<MdInterfaceProtocol> result = modelInterfaceProtocolService.queryProtocols(null, null);
        assertEquals(1, result.size());
    }

    @Test
    void testQueryProtocols_FilterByModelType() {
        List<MdInterfaceProtocol> protocols = new ArrayList<>();
        MdInterfaceProtocol p1 = new MdInterfaceProtocol();
        p1.setModelTypes("LLM");
        p1.setVisible("true");
        MdInterfaceProtocol p2 = new MdInterfaceProtocol();
        p2.setModelTypes("EMBEDDING");
        p2.setVisible("true");
        protocols.add(p1);
        protocols.add(p2);

        when(mapper.queryAllProtocols()).thenReturn(protocols);
        ReflectionTestUtils.setField(modelInterfaceProtocolService, "lastRefreshTime", 0L);

        List<MdInterfaceProtocol> result = modelInterfaceProtocolService.queryProtocols("LLM", null);
        assertEquals(1, result.size());
        assertEquals("LLM", result.get(0).getModelTypes());
    }

    @Test
    void testQueryProtocols_FilterByVisible() {
        List<MdInterfaceProtocol> protocols = new ArrayList<>();
        MdInterfaceProtocol p1 = new MdInterfaceProtocol();
        p1.setModelTypes("LLM");
        p1.setVisible("true");
        MdInterfaceProtocol p2 = new MdInterfaceProtocol();
        p2.setModelTypes("LLM");
        p2.setVisible("false");
        protocols.add(p1);
        protocols.add(p2);

        when(mapper.queryAllProtocols()).thenReturn(protocols);
        ReflectionTestUtils.setField(modelInterfaceProtocolService, "lastRefreshTime", 0L);

        List<MdInterfaceProtocol> result = modelInterfaceProtocolService.queryProtocols(null, "true");
        assertEquals(1, result.size());
        assertEquals("true", result.get(0).getVisible());
    }

    @Test
    void testQueryProtocols_CacheNotExpired() {
        List<MdInterfaceProtocol> protocols = new ArrayList<>();
        MdInterfaceProtocol p1 = new MdInterfaceProtocol();
        p1.setModelTypes("LLM");
        p1.setVisible("true");
        protocols.add(p1);

        ReflectionTestUtils.setField(modelInterfaceProtocolService, "cache", protocols);
        ReflectionTestUtils.setField(modelInterfaceProtocolService, "lastRefreshTime", System.currentTimeMillis());

        List<MdInterfaceProtocol> result = modelInterfaceProtocolService.queryProtocols(null, null);
        assertEquals(1, result.size());
        verify(mapper, never()).queryAllProtocols();
    }

    @Test
    void testQueryProtocols_CacheExpired() {
        List<MdInterfaceProtocol> protocols = new ArrayList<>();
        MdInterfaceProtocol p1 = new MdInterfaceProtocol();
        p1.setModelTypes("LLM");
        p1.setVisible("true");
        protocols.add(p1);

        when(mapper.queryAllProtocols()).thenReturn(protocols);
        ReflectionTestUtils.setField(modelInterfaceProtocolService, "lastRefreshTime", 0L);

        List<MdInterfaceProtocol> result = modelInterfaceProtocolService.queryProtocols(null, null);
        assertEquals(1, result.size());
        verify(mapper, atLeastOnce()).queryAllProtocols();
    }

    @Test
    void testQueryProtocols_EmptyResult() {
        when(mapper.queryAllProtocols()).thenReturn(new ArrayList<>());
        ReflectionTestUtils.setField(modelInterfaceProtocolService, "lastRefreshTime", 0L);

        List<MdInterfaceProtocol> result = modelInterfaceProtocolService.queryProtocols("NONEXISTENT", "true");
        assertTrue(result.isEmpty());
    }
}
