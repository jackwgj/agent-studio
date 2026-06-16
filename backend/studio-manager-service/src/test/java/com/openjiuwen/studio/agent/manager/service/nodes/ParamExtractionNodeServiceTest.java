/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.manager.service.nodes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openjiuwen.studio.agent.common.enums.NodeType;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.manager.dto.WorkflowNodeVO;
import com.openjiuwen.studio.agent.manager.dto.WorkflowVO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class ParamExtractionNodeServiceTest {

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ParamExtractionNodeService paramExtractionNodeService;

    @Test
    void testGetStartNodeWithOutputs_Success() {
        WorkflowNodeVO startNode = new WorkflowNodeVO()
            .setId("start-1")
            .setName("Start")
            .setType(NodeType.START.getType());

        WorkflowNodeVO llmNode = new WorkflowNodeVO()
            .setId("llm-1")
            .setName("LLM")
            .setType(NodeType.LLM.getType());

        List<WorkflowNodeVO> nodes = new ArrayList<>();
        nodes.add(startNode);
        nodes.add(llmNode);

        WorkflowNodeVO result = paramExtractionNodeService.getStartNodeWithOutputs(nodes, NodeType.START);

        assertNotNull(result);
        assertEquals("start-1", result.getId());
        assertEquals(NodeType.START.getType(), result.getType());
    }

    @Test
    void testGetStartNodeWithOutputs_NodeNotFound() {
        WorkflowNodeVO llmNode = new WorkflowNodeVO()
            .setId("llm-1")
            .setName("LLM")
            .setType(NodeType.LLM.getType());

        List<WorkflowNodeVO> nodes = new ArrayList<>();
        nodes.add(llmNode);

        assertThrows(AgentStudioException.class,
            () -> paramExtractionNodeService.getStartNodeWithOutputs(nodes, NodeType.START));
    }

    @Test
    void testGetStartNodeWithOutputs_EmptyNodeList() {
        List<WorkflowNodeVO> nodes = new ArrayList<>();

        assertThrows(AgentStudioException.class,
            () -> paramExtractionNodeService.getStartNodeWithOutputs(nodes, NodeType.START));
    }

    @Test
    void testGetStartNodeWithOutputs_MultipleMatchingNodes() {
        WorkflowNodeVO startNode1 = new WorkflowNodeVO()
            .setId("start-1")
            .setName("Start 1")
            .setType(NodeType.START.getType());

        WorkflowNodeVO startNode2 = new WorkflowNodeVO()
            .setId("start-2")
            .setName("Start 2")
            .setType(NodeType.START.getType());

        List<WorkflowNodeVO> nodes = new ArrayList<>();
        nodes.add(startNode1);
        nodes.add(startNode2);

        WorkflowNodeVO result = paramExtractionNodeService.getStartNodeWithOutputs(nodes, NodeType.START);

        assertNotNull(result);
        assertEquals("start-1", result.getId());
    }

    @Test
    void testAdaptParamExtractionNode_NoParamExtractionNodes() {
        WorkflowNodeVO startNode = new WorkflowNodeVO()
            .setId("start-1")
            .setName("Start")
            .setType(NodeType.START.getType());

        WorkflowNodeVO llmNode = new WorkflowNodeVO()
            .setId("llm-1")
            .setName("LLM")
            .setType(NodeType.LLM.getType());

        List<WorkflowNodeVO> nodes = new ArrayList<>();
        nodes.add(startNode);
        nodes.add(llmNode);

        WorkflowVO workflow = new WorkflowVO();
        workflow.setId("wf-1");
        workflow.setNodes(nodes);
        workflow.setEdges(new ArrayList<>());

        WorkflowVO result = paramExtractionNodeService.adaptParamExtractionNode(workflow);

        assertNotNull(result);
        assertSame(workflow, result);
    }

    @Test
    void testAdaptParamExtractionNode_EmptyNodes() {
        WorkflowVO workflow = new WorkflowVO();
        workflow.setId("wf-1");
        workflow.setNodes(new ArrayList<>());
        workflow.setEdges(new ArrayList<>());

        WorkflowVO result = paramExtractionNodeService.adaptParamExtractionNode(workflow);

        assertNotNull(result);
        assertSame(workflow, result);
    }

    @Test
    void testGetStartNodeWithOutputs_ParamOutType() {
        WorkflowNodeVO paramOutNode = new WorkflowNodeVO()
            .setId("param-out-1")
            .setName("ParamOutput")
            .setType(NodeType.PARAM_OUT.getType());

        List<WorkflowNodeVO> nodes = new ArrayList<>();
        nodes.add(paramOutNode);

        WorkflowNodeVO result = paramExtractionNodeService.getStartNodeWithOutputs(nodes, NodeType.PARAM_OUT);

        assertNotNull(result);
        assertEquals("param-out-1", result.getId());
    }
}
