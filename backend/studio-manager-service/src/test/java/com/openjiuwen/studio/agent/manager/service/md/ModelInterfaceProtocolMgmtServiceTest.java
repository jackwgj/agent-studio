/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */
package com.openjiuwen.studio.agent.manager.service.md;

import com.openjiuwen.studio.agent.manager.dto.ModelInterfaceProtocolListRsp;
import com.openjiuwen.studio.agent.manager.dto.QueryMdInterfaceProtocolQo;
import com.openjiuwen.studio.agent.manager.entity.md.MdInterfaceProtocol;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ModelInterfaceProtocolMgmtServiceTest {

    @Mock
    private ModelInterfaceProtocolService service;

    @InjectMocks
    private ModelInterfaceProtocolMgmtService modelInterfaceProtocolMgmtService;

    @Test
    void testQueryMdInterfaceProtocol_WithData() {
        QueryMdInterfaceProtocolQo qo = new QueryMdInterfaceProtocolQo();
        qo.setModelType("LLM");
        qo.setVisible("true");

        List<MdInterfaceProtocol> protocolList = new ArrayList<>();
        MdInterfaceProtocol p = new MdInterfaceProtocol();
        p.setId("p1");
        p.setModelTypes("LLM");
        p.setVisible("true");
        protocolList.add(p);

        when(service.queryProtocols("LLM", "true")).thenReturn(protocolList);

        ModelInterfaceProtocolListRsp result = modelInterfaceProtocolMgmtService.queryMdInterfaceProtocol("proj1", qo);

        assertNotNull(result);
        assertNotNull(result.getData());
        assertEquals(1, result.getData().size());
    }

    @Test
    void testQueryMdInterfaceProtocol_EmptyResult() {
        QueryMdInterfaceProtocolQo qo = new QueryMdInterfaceProtocolQo();
        qo.setModelType("NONEXISTENT");
        qo.setVisible("true");

        when(service.queryProtocols("NONEXISTENT", "true")).thenReturn(new ArrayList<>());

        ModelInterfaceProtocolListRsp result = modelInterfaceProtocolMgmtService.queryMdInterfaceProtocol("proj1", qo);

        assertNotNull(result);
        assertTrue(result.getData().isEmpty());
    }

    @Test
    void testQueryMdInterfaceProtocol_NullFilters() {
        QueryMdInterfaceProtocolQo qo = new QueryMdInterfaceProtocolQo();

        List<MdInterfaceProtocol> protocolList = new ArrayList<>();
        MdInterfaceProtocol p = new MdInterfaceProtocol();
        p.setId("p1");
        protocolList.add(p);

        when(service.queryProtocols(null, null)).thenReturn(protocolList);

        ModelInterfaceProtocolListRsp result = modelInterfaceProtocolMgmtService.queryMdInterfaceProtocol("proj1", qo);

        assertNotNull(result);
        assertEquals(1, result.getData().size());
    }
}
