/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */
package com.openjiuwen.studio.agent.manager.service;

import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.manager.dto.ControllerNodeVO;
import com.openjiuwen.studio.agent.manager.enums.controller.AgentNodeType;
import com.openjiuwen.studio.agent.manager.mapper.AgentMapper;
import com.openjiuwen.studio.agent.manager.mapper.MappingMapper;
import com.openjiuwen.studio.agent.manager.mapper.ReleaseVersionMapper;
import com.openjiuwen.studio.agent.manager.mapper.ShareResourceMapper;
import com.openjiuwen.studio.agent.manager.mapper.WorkflowMapper;
import com.openjiuwen.studio.agent.manager.obs.MgObsService;
import com.openjiuwen.studio.agent.manager.service.md.ModelServiceManager;
import com.openjiuwen.studio.agent.manager.service.memory.AgentMemoryConfigService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ControllerManagementServiceTest {

    private AgentMapper agentMapper;
    private WorkflowManagementService workflowManagementService;
    private AgentCommonService agentCommonService;
    private MappingMapper mappingMapper;
    private ReleaseVersionMapper releaseVersionMapper;
    private WorkflowMapper workflowMapper;
    private MgObsService mgObsService;
    private ModelServiceManager modelServiceManager;
    private ShareResourceMapper shareResourceMapper;
    private RelationManagementService relationManagementService;
    private AgentMemoryConfigService agentMemoryConfigService;
    private IrAdapterService irAdapterService;

    private ControllerManagementService controllerManagementService;

    @BeforeEach
    void setUp() {
        agentMapper = mock(AgentMapper.class);
        workflowManagementService = mock(WorkflowManagementService.class);
        agentCommonService = mock(AgentCommonService.class);
        mappingMapper = mock(MappingMapper.class);
        releaseVersionMapper = mock(ReleaseVersionMapper.class);
        workflowMapper = mock(WorkflowMapper.class);
        mgObsService = mock(MgObsService.class);
        modelServiceManager = mock(ModelServiceManager.class);
        shareResourceMapper = mock(ShareResourceMapper.class);
        relationManagementService = mock(RelationManagementService.class);
        agentMemoryConfigService = mock(AgentMemoryConfigService.class);
        irAdapterService = mock(IrAdapterService.class);

        MockitoAnnotations.openMocks(this);
        controllerManagementService = new ControllerManagementService();
        ReflectionTestUtils.setField(controllerManagementService, "agentMapper", agentMapper);
        ReflectionTestUtils.setField(controllerManagementService, "workflowManagementService", workflowManagementService);
        ReflectionTestUtils.setField(controllerManagementService, "agentCommonService", agentCommonService);
        ReflectionTestUtils.setField(controllerManagementService, "mappingMapper", mappingMapper);
        ReflectionTestUtils.setField(controllerManagementService, "releaseVersionMapper", releaseVersionMapper);
        ReflectionTestUtils.setField(controllerManagementService, "workflowMapper", workflowMapper);
        ReflectionTestUtils.setField(controllerManagementService, "mgObsService", mgObsService);
        ReflectionTestUtils.setField(controllerManagementService, "modelServiceManager", modelServiceManager);
        ReflectionTestUtils.setField(controllerManagementService, "shareResourceMapper", shareResourceMapper);
        ReflectionTestUtils.setField(controllerManagementService, "relationManagementService", relationManagementService);
        ReflectionTestUtils.setField(controllerManagementService, "agentMemoryConfigService", agentMemoryConfigService);
        ReflectionTestUtils.setField(controllerManagementService, "irAdapterService", irAdapterService);
        ReflectionTestUtils.setField(controllerManagementService, "showIntentParamEnable", false);
        ReflectionTestUtils.setField(controllerManagementService, "controllerInitIntentDsl", "{}");
        ReflectionTestUtils.setField(controllerManagementService, "controllerInitIntentDslEn", "{}");
        ReflectionTestUtils.setField(controllerManagementService, "controllerInitDsl", "{}");
        ReflectionTestUtils.setField(controllerManagementService, "controllerInitDslEn", "{}");
        ReflectionTestUtils.setField(controllerManagementService, "controllerInitIr", "{}");
        ReflectionTestUtils.setField(controllerManagementService, "uniqueWorkflowTypes", "type1,type2");
        ReflectionTestUtils.setField(controllerManagementService, "maxIterationConf", 10);
        ReflectionTestUtils.setField(controllerManagementService, "chatHistoryMaxTurnConf", 5);
        ReflectionTestUtils.setField(controllerManagementService, "globalIntendDefaultAction", "default");
        ReflectionTestUtils.setField(controllerManagementService, "controllerWorkflowLimit", 5);
        ReflectionTestUtils.setField(controllerManagementService, "controllerSubAgentLimit", 3);
        ReflectionTestUtils.setField(controllerManagementService, "controllerSubAgentMaxDepth", 2);
        ReflectionTestUtils.setField(controllerManagementService, "workflowInteractiveTypes", "type1,type2");
    }

    @Test
    void testGetControllerNode_Success() {
        ControllerNodeVO nodeVo = new ControllerNodeVO();
        Map<String, ControllerNodeVO> controllerMap = new HashMap<>();
        controllerMap.put("node-1", nodeVo);

        Map<String, Map<String, ControllerNodeVO>> nodesGroupByTypeId = new HashMap<>();
        nodesGroupByTypeId.put(AgentNodeType.CONTROLLER.getType(), controllerMap);

        ControllerNodeVO result = controllerManagementService.getControllerNode(nodesGroupByTypeId);

        assertNotNull(result);
        assertEquals(nodeVo, result);
    }

    @Test
    void testGetControllerNode_EmptyControllerMap() {
        Map<String, Map<String, ControllerNodeVO>> nodesGroupByTypeId = new HashMap<>();
        nodesGroupByTypeId.put(AgentNodeType.CONTROLLER.getType(), Collections.emptyMap());

        assertThrows(AgentStudioException.class, () ->
            controllerManagementService.getControllerNode(nodesGroupByTypeId));
    }

    @Test
    void testGetControllerNode_NullControllerMap() {
        Map<String, Map<String, ControllerNodeVO>> nodesGroupByTypeId = new HashMap<>();

        assertThrows(AgentStudioException.class, () ->
            controllerManagementService.getControllerNode(nodesGroupByTypeId));
    }

    @Test
    void testGetControllerNode_MissingControllerType() {
        Map<String, Map<String, ControllerNodeVO>> nodesGroupByTypeId = new HashMap<>();
        nodesGroupByTypeId.put("other_type", new HashMap<>());

        assertThrows(AgentStudioException.class, () ->
            controllerManagementService.getControllerNode(nodesGroupByTypeId));
    }
}
