/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.workflow.jiuwen;

import com.openjiuwen.studio.agent.manager.dto.WorkflowFieldVO;
import com.openjiuwen.studio.agent.manager.dto.WorkflowFieldVOValue;
import com.openjiuwen.studio.agent.manager.dto.WorkflowNodeVO;
import com.openjiuwen.studio.agent.manager.workflow.jiuwen.adapt.StreamTransformNodeAdapter;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MockitoSettings(strictness = Strictness.LENIENT)
public class StreamTransformNodeAdapterTest {

    @InjectMocks
    private StreamTransformNodeAdapter streamTransformNodeAdapter;

    @Test
    @SuppressWarnings("unchecked")
    void test_adaptConfigs_success() {
        WorkflowNodeVO workflowNodeVO = new WorkflowNodeVO();
        Map<String, Object> configs = new HashMap<>();

        // 设置configs
        List<String> concat = List.of("raw_output.content");
        configs.put("concat", concat);
        String frame_template = "{\"type\":\"message\",\"data\":{\"content\":\"{{raw_output.content}}\","
            + "\"riskDescription\":null},\"finish\":\"{{raw_output.finish}}\"}";
        Map<String, Object> transformer = new HashMap<>();
        transformer.put("frame_template", frame_template);
        configs.put("transformer", transformer);
        workflowNodeVO.setConfigs(configs);

        // 设置inputs
        Map<String, String> content = new HashMap<>();
        content.put("ref_node_id", "node_plugin");
        content.put("ref_var_name", "userFields.stream_output");
        content.put("source", "system");
        WorkflowFieldVOValue workflowFieldVOValue = new WorkflowFieldVOValue();
        workflowFieldVOValue.setType(WorkflowFieldVOValue.TypeEnum.REF)
            .setContent(content);

        WorkflowFieldVO workflowFieldVO = new WorkflowFieldVO();
        workflowFieldVO.setName("raw_output")
            .setType("object")
            .setSource(WorkflowFieldVO.SourceEnum.USER)
            .setValue(workflowFieldVOValue);
        workflowNodeVO.setInputs(List.of(workflowFieldVO));

        workflowNodeVO.setOutputs(new ArrayList<>());


        Map<String, Object> result = streamTransformNodeAdapter.adaptConfig(workflowNodeVO);

        // 3. 验证结果
        assertNotNull(result);
        assertTrue(result.containsKey("transformer"));

        Map<String, Object> irTransformer = (Map<String, Object>) result.get("transformer");

        // 验证变量生成
        assertTrue(irTransformer.containsKey("variables"));
        List<Map<String, Object>> variables = (List<Map<String, Object>>) irTransformer.get("variables");
        assertFalse(variables.isEmpty(), "Variables should not be empty");

        // 验证变量中是否正确标记了 concat
        // 注意：这取决于你 processStreamNodeFrameTemplateAndVariables 的实现逻辑
        boolean hasConcat = variables.stream().anyMatch(v -> Boolean.TRUE.equals(v.get("concat")));
        assertTrue(hasConcat, "At least one variable should have concat=true");

        // 验证 frame_template 是否被解析为 JsonNode (因为用了 readTree)
        assertNotNull(irTransformer.get("frame_template"));
        assertNotEquals(String.class, irTransformer.get("frame_template").getClass(), "Frame template should be a JsonNode, not String");

        // 验证固定配置项
        assertEquals(true, irTransformer.get("emit_final_concat_frame"));
        assertEquals(false, irTransformer.get("prune_none_fields"));
    }



}
