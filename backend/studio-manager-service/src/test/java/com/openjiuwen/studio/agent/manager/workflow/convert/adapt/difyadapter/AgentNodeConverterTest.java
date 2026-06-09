/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.workflow.convert.adapt.difyadapter;

import com.openjiuwen.studio.agent.common.enums.NodeType;
import com.openjiuwen.studio.agent.manager.dto.WorkflowFieldVO;
import com.openjiuwen.studio.agent.manager.dto.WorkflowFieldVOValue;
import com.openjiuwen.studio.agent.manager.dto.WorkflowNodeVO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentNodeConverterTest {

    private AgentNodeConverter converter;
    private Map<String, WorkflowNodeVO> workflowNodeMap;

    @BeforeEach
    void setUp() {
        converter = new AgentNodeConverter();
        workflowNodeMap = new HashMap<>();
    }

    @Test
    @DisplayName("testSupportNodeType_WhenAgentType_ShouldReturnTrue")
    void testSupportNodeType_WhenAgentType_ShouldReturnTrue() {
        // 验证转换器是否声明支持 AGENT 类型
        assertTrue(converter.supportNodeType(NodeType.AGENT));
    }

    @Test
    @DisplayName("testSupportNodeType_WhenOtherType_ShouldReturnFalse")
    void testSupportNodeType_WhenOtherType_ShouldReturnFalse() {
        // 验证非 AGENT 类型（如 LLM）应返回 false
        assertFalse(converter.supportNodeType(NodeType.LLM));
    }

    @Test
    @DisplayName("testAdaptOutputs_Always_ShouldReturnSingleStringOutputNamedOutput")
    void testAdaptOutputs_Always_ShouldReturnSingleStringOutputNamedOutput() {
        Map<String, Object> data = new HashMap<>();

        List<WorkflowFieldVO> outputs = converter.adaptOutputs(data);

        // 1. 验证输出列表大小为 1
        assertEquals(1, outputs.size());

        // 2. 验证关键字段属性
        WorkflowFieldVO outputField = outputs.get(0);
        assertEquals("output", outputField.getName());
        assertEquals("string", outputField.getType());
        assertEquals(WorkflowFieldVO.SourceEnum.USER, outputField.getSource());

        // 3. 验证 Value 类型是否为 GENERATED
        assertNotNull(outputField.getValue());
        assertEquals(WorkflowFieldVOValue.TypeEnum.GENERATED, outputField.getValue().getType());
    }

    @Test
    @DisplayName("testAdaptConfigs_WhenCalled_ShouldReturnSuperConfigs")
    void testAdaptConfigs_WhenCalled_ShouldReturnSuperConfigs() {
        Map<String, Object> data = new HashMap<>();
        data.put("title", "Test Agent Node");

        // 由于 adaptConfigs 内部调用 super.adaptConfigs，
        // 预期会包含 super 中处理的通用逻辑（如 description, title 等）
        Map<String, Object> configs = converter.adaptConfigs(data);

        assertNotNull(configs);
        // 如果 AbstractSDSLNodeConverter 处理了 title，这里应有体现
    }

    @Test
    @DisplayName("testAdapt_FullFlow_ShouldReturnCorrectAgentNodeVO")
    void testAdapt_FullFlow_ShouldReturnCorrectAgentNodeVO() {
        // 准备输入数据
        Map<String, Object> data = new HashMap<>();
        WorkflowNodeVO nodeVO = new WorkflowNodeVO();
        nodeVO.setId("agent_001");

        // 执行适配
        WorkflowNodeVO result = converter.adapt(data, nodeVO, workflowNodeMap);

        // 断言
        assertNotNull(result);
        assertEquals(NodeType.AGENT.getType(), result.getType());

        // 验证各个子方法都被正确调用并组装
        assertFalse(result.getOutputs().isEmpty());
        assertEquals("output", result.getOutputs().get(0).getName());
        assertNotNull(result.getConfigs());
    }
}
