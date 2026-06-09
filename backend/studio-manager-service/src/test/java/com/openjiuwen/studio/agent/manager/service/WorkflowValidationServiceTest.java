/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.utils.I18nUtil;
import com.openjiuwen.studio.agent.manager.dto.WorkflowFieldVO;
import com.openjiuwen.studio.agent.manager.dto.WorkflowFieldVOValue;
import com.openjiuwen.studio.agent.manager.dto.WorkflowValidationVOErrors;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.manager.dto.WorkflowNodeVO;
import com.openjiuwen.studio.agent.manager.entity.ShareResourceEntity;
import com.openjiuwen.studio.agent.manager.entity.ShareScopeEntity;
import com.openjiuwen.studio.agent.manager.entity.ToolEntity;
import com.openjiuwen.studio.agent.manager.entity.plugin.PluginEntity;
import com.openjiuwen.studio.agent.manager.mapper.ShareResourceMapper;
import com.openjiuwen.studio.agent.manager.mapper.ShareScopeMapper;
import com.openjiuwen.studio.agent.manager.mapper.ToolMapper;
import com.openjiuwen.studio.agent.manager.mapper.plugin.PluginMapper;
import com.openjiuwen.studio.agent.manager.service.plugin.IPluginBase;
import com.openjiuwen.studio.agent.manager.utils.JsonUtils;
import com.openjiuwen.studio.agent.manager.service.WorkflowValidationService.Node;
import com.openjiuwen.studio.agent.manager.workflow.jiuwen.models.IRConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;

@MockitoSettings(strictness = Strictness.LENIENT)
class WorkflowValidationServiceTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ToolMapper toolMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private PluginMapper pluginMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private IPluginBase pluginDomain;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShareResourceMapper shareResourceMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShareScopeMapper shareScopeMapper;

    @Mock
    private I18nUtil i18nUtil;

    @InjectMocks
    private WorkflowValidationService workflowValidationService;

    @InjectMocks
    private WorkflowValidationService validator;

    private Node node;

    private AutoCloseable mockitoCloseable;

    private List<WorkflowValidationVOErrors> errors;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(workflowValidationService, "workflowSchema", "not_empty");
        ReflectionTestUtils.setField(workflowValidationService, "iconMaxSize", "not_empty");
        ReflectionTestUtils.setField(workflowValidationService, "opSvcProjectId", "not_empty");
        ReflectionTestUtils.setField(workflowValidationService, "systemFields", "[\"request_id\", \"session_id\"]");
        ReflectionTestUtils.setField(workflowValidationService, "interactiveNodeType", "not_empty");
        ReflectionTestUtils.setField(workflowValidationService, "maxNodeNum", 0);
        ReflectionTestUtils.setField(workflowValidationService, "workflowExceptionSupportNodes", "not_empty");
        ReflectionTestUtils.setField(workflowValidationService, "branchNodeType", "not_empty");
        
        // 初始化正则表达式模式
        ReflectionTestUtils.invokeMethod(workflowValidationService, "init");
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_validateTools_with_null_nodes() throws Exception {
        assertDoesNotThrow(() -> {
            workflowValidationService.validateTools(null, "projectId", "workspaceId");
        });
    }

    @Test
    void test_validateTools_with_valid_config_and_plugins() throws Exception {
        List<Map<String, Object>> nodes = new ArrayList<>();
        Map<String, Object> nodeMap = new HashMap<>();
        nodeMap.put("type", "Plugin");
        Map<String, Object> configMap = new HashMap<>();

        HashMap<String, Object> exception_process = new HashMap<>();
        exception_process.put("retry_times", 2);

        configMap.put("plugins", new ArrayList<>());
        configMap.put("exception_process", exception_process);

        nodeMap.put("configs", configMap);
        nodes.add(nodeMap);

        try (MockedStatic<JsonUtils> jsonUtilsMock = mockStatic(JsonUtils.class)) {
            jsonUtilsMock.when(() -> JsonUtils.objectToClass(nodeMap.get("configs"))).thenReturn(configMap);
            jsonUtilsMock.when(() -> JsonUtils.objectToClass(configMap.get("exception_process")))
                    .thenReturn(exception_process);

            ToolEntity toolEntity = mock(ToolEntity.class);
            when(toolMapper.selectById(anyString())).thenReturn(toolEntity);
            when(toolEntity.getWorkspaceId()).thenReturn("workspaceId");
            assertThrows(AgentStudioException.class, () -> {
                workflowValidationService.validateTools(nodes, "projectId", "workspaceId");
            });
        }
    }

    @Test
    void test_validateTools_with_no_agent_nodes() throws Exception {
        assertDoesNotThrow(() -> {
            List<Map<String, Object>> nodes = new ArrayList<>();
            Map<String, Object> nodeMap = new HashMap<>();
            nodeMap.put("type", "NotAgent");
            nodes.add(nodeMap);

            workflowValidationService.validateTools(nodes, "projectId", "workspaceId");
        });
    }

    @Test
    void test_validateTools_with_agent_nodes_without_plugins() throws Exception {
        assertDoesNotThrow(() -> {
            List<Map<String, Object>> nodes = new ArrayList<>();
            Map<String, Object> nodeMap = new HashMap<>();
            nodeMap.put("type", "Agent");
            nodeMap.put("configs", new HashMap<>());
            nodes.add(nodeMap);

            workflowValidationService.validateTools(nodes, "projectId", "workspaceId");
            // No exception should be thrown
        });
    }

    @Test
    void test_validateTools_with_agent_nodes_and_matching_workspace_ids() throws Exception {
        assertDoesNotThrow(() -> {
            List<Map<String, Object>> nodes = new ArrayList<>();
            Map<String, Object> nodeMap = new HashMap<>();
            nodeMap.put("type", "Agent");
            Map<String, Object> configs = new HashMap<>();
            List<Map<String, Object>> plugins = new ArrayList<>();
            Map<String, Object> plugin = new HashMap<>();
            plugin.put("id", "pluginId");
            ToolEntity toolEntity = new ToolEntity();
            toolEntity.setWorkspaceId("workspaceId");
            toolEntity.setType("inner");
            plugins.add(plugin);
            configs.put("plugins", plugins);
            nodeMap.put("configs", configs);
            nodes.add(nodeMap);

            when(shareScopeMapper.selectShareScopesByResourceIdAndWorkspaceId(anyString(), anyString())).thenReturn(new ShareScopeEntity());
            when(shareResourceMapper.selectShareResourceEntityByResourceId(anyString())).thenReturn(new ShareResourceEntity());

            when(pluginDomain.buildToolByPlugin(anyString(), anyString(), anyString(), anyString())).thenReturn(toolEntity);
            workflowValidationService.validateTools(nodes, "projectId", "workspaceId");
            // No exception should be thrown
        });
    }

    @Test
    void test_validateTools_with_agent_nodes_and_non_matching_workspace_ids() throws Exception {
        List<Map<String, Object>> nodes = new ArrayList<>();
        Map<String, Object> nodeMap = new HashMap<>();
        nodeMap.put("type", "Agent");
        Map<String, Object> configs = new HashMap<>();
        List<Map<String, Object>> plugins = new ArrayList<>();
        Map<String, Object> plugin = new HashMap<>();
        plugin.put("id", "pluginId");
        PluginEntity toolEntity = new PluginEntity();
        toolEntity.setWorkspaceId("differentWorkspaceId");
        plugins.add(plugin);
        configs.put("plugins", plugins);
        nodeMap.put("configs", configs);
        nodes.add(nodeMap);

        when(pluginMapper.selectByPrimaryKey("pluginId", null)).thenReturn(toolEntity);
        when(shareScopeMapper.selectShareScopesByResourceIdAndWorkspaceId(anyString(), anyString())).thenReturn(new ShareScopeEntity());
        when(shareResourceMapper.selectShareResourceEntityByResourceId(anyString())).thenReturn(new ShareResourceEntity());

        workflowValidationService.validateTools(nodes, "projectId", "workspaceId");
    }

    @Test
    void test_validateTools_with_null_nodes_should_return_early() throws Exception {
        assertDoesNotThrow(() -> {
            workflowValidationService.validateTools(null, "projectId", "workspaceId");
        });
    }

    @Test
    void test_validateQuestionerNode_with_both_fields_exceeding_max_length() throws Exception {
        try (MockedStatic<JsonUtils> jsonUtilsMock = mockStatic(JsonUtils.class)) {

            //Given
            List<Map<String, Object>> nodes = new ArrayList<>();
            Map<String, Object> nodeMap = new HashMap<>();
            nodeMap.put("type", "Questioner");

            Map<String, Object> configMap = new HashMap<>();
            String extraPrompt = "a".repeat(3001); // 超过最大限制3000
            String questionContent = "b".repeat(3001); // 超过最大限制3000
            configMap.put("extra_prompt_for_fields_extraction", extraPrompt);
            configMap.put("question_content", questionContent);

            nodeMap.put("configs", configMap);
            nodes.add(nodeMap);

            jsonUtilsMock.when(() -> JsonUtils.objectToClass(nodeMap.get("configs"))).thenReturn(configMap);
            jsonUtilsMock.when(() -> JsonUtils.objectToClass(configMap.get("extra_prompt_for_fields_extraction")))
                .thenReturn(extraPrompt);
            jsonUtilsMock.when(() -> JsonUtils.objectToClass(configMap.get("question_content")))
                .thenReturn(questionContent);

            assertThrows(AgentStudioException.class, () -> {
                workflowValidationService.validateQuestionerNode(nodes, "workflowId");
            });
        }
    }

    @Test
    void testValidate_NestedInvalidName() {
        lenient().when(i18nUtil.getMessage(anyString())).thenReturn("Error message for {0}");
        errors = new ArrayList<>();

        Node mockNode = Mockito.mock(Node.class);
        when(mockNode.getId()).thenReturn("node_id_001");
        when(mockNode.getType()).thenReturn("http_request");
        WorkflowNodeVO mockNodeInfo = Mockito.mock(WorkflowNodeVO.class);
        when(mockNode.getNodeInfo()).thenReturn(mockNodeInfo);
        when(mockNodeInfo.getType()).thenReturn("http_request");

        WorkflowFieldVO parentField = new WorkflowFieldVO();
        parentField.setName("parent");
        WorkflowFieldVOValue parentValue = new WorkflowFieldVOValue();
        parentValue.setType(WorkflowFieldVOValue.TypeEnum.NESTED);
        parentField.setValue(parentValue);

        com.alibaba.fastjson2.JSONObject nestedObj = new com.alibaba.fastjson2.JSONObject();
        nestedObj.put("name", "123name"); // 非法名称：数字开头

        WorkflowFieldVOValue nestedVal = new WorkflowFieldVOValue();
        nestedVal.setType(WorkflowFieldVOValue.TypeEnum.LITERAL);
        nestedVal.setContent("some value");
        nestedObj.put("value", nestedVal);

        com.alibaba.fastjson2.JSONArray schema = new com.alibaba.fastjson2.JSONArray();
        schema.add(nestedObj);
        parentField.setSchema(schema);

        ReflectionTestUtils.invokeMethod(validator, "validateInputFields",
            List.of(parentField), mockNode, new HashMap<>(), new HashMap<>(), errors);

        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getReason().contains("parent"));
    }

    @Test
    void testValidate_LiteralRequiredEmpty() {
        lenient().when(i18nUtil.getMessage(anyString())).thenReturn("Error message for {0}");
        errors = new ArrayList<>();

        Node mockNode = Mockito.mock(Node.class);
        when(mockNode.getId()).thenReturn("node_id_001");
        when(mockNode.getType()).thenReturn("http_request");
        WorkflowNodeVO mockNodeInfo = Mockito.mock(WorkflowNodeVO.class);
        when(mockNode.getNodeInfo()).thenReturn(mockNodeInfo);

        WorkflowFieldVO field = new WorkflowFieldVO();
        field.setName("testField");
        field.setRequired(true);

        WorkflowFieldVOValue value = new WorkflowFieldVOValue();
        value.setType(WorkflowFieldVOValue.TypeEnum.LITERAL);
        value.setContent("");
        field.setValue(value);

        ReflectionTestUtils.invokeMethod(validator, "validateInputFields",
            List.of(field), mockNode, new HashMap<>(), new HashMap<>(), errors);

        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getReason().contains("testField"));
    }

    /**
     * 测试 isStringTypeValid 方法 - 非String类型元素
     */
    @Test
    void testIsStringTypeValid_withNonStringElement() {
        // 准备测试数据
        WorkflowNodeVO nodeInfo = new WorkflowNodeVO();
        Map<String, Object> configs = new HashMap<>();
        configs.put("enable_history", true);
        nodeInfo.setConfigs(configs);
        
        Node node = new Node(nodeInfo);
        
        IRConfig irConfig = new IRConfig();
        irConfig.setBlank(true);
        irConfig.setTemplate(false);
        
        List<String> inputFields = new ArrayList<>();
        
        // 调用被测方法
        boolean result = (Boolean) ReflectionTestUtils.invokeMethod(workflowValidationService, "isStringTypeValid", 123, irConfig, inputFields, node);
        
        // 验证结果
        assertFalse(result);
    }

    /**
     * 测试 isStringTypeValid 方法 - String类型元素，非空校验通过
     */
    @Test
    void testIsStringTypeValid_withValidStringElement() {
        // 准备测试数据
        WorkflowNodeVO nodeInfo = new WorkflowNodeVO();
        Map<String, Object> configs = new HashMap<>();
        configs.put("enable_history", true);
        nodeInfo.setConfigs(configs);
        
        Node node = new Node(nodeInfo);
        
        IRConfig irConfig = new IRConfig();
        irConfig.setBlank(true);  // 允许为空
        irConfig.setTemplate(false);  // 非模板
        
        List<String> inputFields = new ArrayList<>();
        
        // 调用被测方法
        boolean result = (Boolean) ReflectionTestUtils.invokeMethod(workflowValidationService, "isStringTypeValid", "test", irConfig, inputFields, node);
        
        // 验证结果
        assertTrue(result);
    }

    /**
     * 测试 isStringTypeValid 方法 - String类型元素，非空校验失败（非MESSAGE节点）
     */
    @Test
    void testIsStringTypeValid_withEmptyStringAndNonBlankConfigForNonMessageNode() {
        // 准备测试数据
        WorkflowNodeVO nodeInfo = new WorkflowNodeVO();
        Map<String, Object> configs = new HashMap<>();
        configs.put("enable_history", true);
        nodeInfo.setConfigs(configs);
        nodeInfo.setType("HTTP");  // 非MESSAGE节点
        
        Node node = new Node(nodeInfo);
        
        IRConfig irConfig = new IRConfig();
        irConfig.setBlank(false);  // 不允许为空
        irConfig.setTemplate(false);  // 非模板
        
        List<String> inputFields = new ArrayList<>();
        
        // 调用被测方法
        boolean result = (Boolean) ReflectionTestUtils.invokeMethod(workflowValidationService, "isStringTypeValid", "", irConfig, inputFields, node);
        
        // 验证结果
        assertFalse(result);
    }

    /**
     * 测试 isStringTypeValid 方法 - String类型元素，非空校验失败（MESSAGE节点，enableHistory=true）
     */
    @Test
    void testIsStringTypeValid_withEmptyStringAndNonBlankConfigForMessageNodeWithHistoryEnabled() {
        // 准备测试数据
        WorkflowNodeVO nodeInfo = new WorkflowNodeVO();
        Map<String, Object> configs = new HashMap<>();
        configs.put("enable_history", true);
        nodeInfo.setConfigs(configs);
        nodeInfo.setType("Message");  // MESSAGE节点
        
        Node node = new Node(nodeInfo);
        
        IRConfig irConfig = new IRConfig();
        irConfig.setBlank(false);  // 不允许为空
        irConfig.setTemplate(false);  // 非模板
        
        List<String> inputFields = new ArrayList<>();
        
        // 调用被测方法
        boolean result = (Boolean) ReflectionTestUtils.invokeMethod(workflowValidationService, "isStringTypeValid", "", irConfig, inputFields, node);
        
        // 验证结果
        assertFalse(result);
    }

    /**
     * 测试 isStringTypeValid 方法 - String类型元素，非空校验通过但模板引用失败
     */
    @Test
    void testIsStringTypeValid_withTemplateReferencesNotFoundInInputFields() {
        // 准备测试数据
        WorkflowNodeVO nodeInfo = new WorkflowNodeVO();
        Map<String, Object> configs = new HashMap<>();
        configs.put("enable_history", true);
        nodeInfo.setConfigs(configs);
        nodeInfo.setType("HTTP");
        
        Node node = new Node(nodeInfo);
        
        IRConfig irConfig = new IRConfig();
        irConfig.setBlank(true);  // 允许为空
        irConfig.setTemplate(true);  // 模板类型
        
        List<String> inputFields = new ArrayList<>();
        inputFields.add("field1");  // 只有field1在输入字段中
        
        // 调用被测方法，包含模板引用{{field1}}和{{field2}}，其中field2不在输入字段中
        boolean result = (Boolean) ReflectionTestUtils.invokeMethod(workflowValidationService, "isStringTypeValid", "Hello {{field1}} and {{field2}}", irConfig, inputFields, node);
        
        // 验证结果
        assertFalse(result);
    }

    /**
     * 测试 isStringTypeValid 方法 - String类型元素，非空校验且模板引用都通过
     */
    @Test
    void testIsStringTypeValid_withAllTemplateReferencesFoundInInputFields() {
        // 准备测试数据
        WorkflowNodeVO nodeInfo = new WorkflowNodeVO();
        Map<String, Object> configs = new HashMap<>();
        configs.put("enable_history", true);
        nodeInfo.setConfigs(configs);
        nodeInfo.setType("HTTP");
        
        Node node = new Node(nodeInfo);
        
        IRConfig irConfig = new IRConfig();
        irConfig.setBlank(true);  // 允许为空
        irConfig.setTemplate(true);  // 模板类型
        
        List<String> inputFields = new ArrayList<>();
        inputFields.add("field1");
        inputFields.add("field2");  // field1和field2都在输入字段中
        
        // 调用被测方法，包含模板引用{{field1}}和{{field2}}，所有引用都在输入字段中
        boolean result = (Boolean) ReflectionTestUtils.invokeMethod(workflowValidationService, "isStringTypeValid", "Hello {{field1}} and {{field2}}", irConfig, inputFields, node);
        
        // 验证结果
        assertTrue(result);
    }

    /**
     * 测试 isStringTypeValid 方法 - MESSAGE节点，enableHistory=false，空字符串应该通过
     */
    @Test
    void testIsStringTypeValid_withMessageNodeAndHistoryDisabledAllowsEmptyString() {
        // 准备测试数据
        WorkflowNodeVO nodeInfo = new WorkflowNodeVO();
        Map<String, Object> configs = new HashMap<>();
        configs.put("enable_history", false);  // 历史关闭
        nodeInfo.setConfigs(configs);
        nodeInfo.setType("Message");  // 注意：这里应该是"Message"，不是"MESSAGE"
        
        Node node = new Node(nodeInfo);
        
        IRConfig irConfig = new IRConfig();
        irConfig.setBlank(false);  // 表面上不允许为空
        irConfig.setTemplate(false);  // 非模板
        
        List<String> inputFields = new ArrayList<>();
        
        // 调用被测方法
        boolean result = (Boolean) ReflectionTestUtils.invokeMethod(workflowValidationService, "isStringTypeValid", "", irConfig, inputFields, node);
        
        // 验证结果 - 对于MESSAGE节点且历史关闭，空字符串应该通过
        assertTrue(result);
    }

    /**
     * 测试 isStringTypeValid 方法 - 包含系统字段的模板引用
     */
    @Test
    void testIsStringTypeValid_withSystemFieldReferences() {
        // 准备测试数据
        WorkflowNodeVO nodeInfo = new WorkflowNodeVO();
        Map<String, Object> configs = new HashMap<>();
        configs.put("enable_history", true);
        nodeInfo.setConfigs(configs);
        nodeInfo.setType("HTTP");
        
        Node node = new Node(nodeInfo);
        
        IRConfig irConfig = new IRConfig();
        irConfig.setBlank(true);
        irConfig.setTemplate(true);
        
        List<String> inputFields = new ArrayList<>();
        inputFields.add("user_input");
        
        // 调用被测方法，包含用户字段和系统字段，系统字段不应该被提取
        boolean result = (Boolean) ReflectionTestUtils.invokeMethod(workflowValidationService, "isStringTypeValid", "Hello {{user_input}} request_id={{request_id}}", irConfig, inputFields, node);
        
        // 验证结果 - 只有user_input需要检查，request_id是系统字段
        assertTrue(result);
    }

    @Test
    void test_validateQuestionerNodeConfig_success() {
        Map<String, Object> configs = new HashMap<>();
        configs.put("extract_fields_from_response", false);
        configs.put("question_content", "");
        Node mockNode = Mockito.mock(Node.class);
        when(mockNode.getId()).thenReturn("node_id_001");
        when(mockNode.getType()).thenReturn("Questioner");
        WorkflowNodeVO mockNodeInfo = Mockito.mock(WorkflowNodeVO.class);
        when(mockNode.getNodeInfo()).thenReturn(mockNodeInfo);
        when(mockNodeInfo.getConfigs()).thenReturn(configs);
        List<WorkflowValidationVOErrors> errors = new ArrayList<>();

        String errorMsg = "Question content cannot be empty";
        when(i18nUtil.getMessage("openjiuwen.02201093")).thenReturn(errorMsg);

        // 2. 使用 ReflectionTestUtils 调用私有方法
        // 参数依次为：(目标对象, 方法名, 参数1, 参数2)
        ReflectionTestUtils.invokeMethod(workflowValidationService, "validateQuestionerNodeConfig", mockNode, errors);

        // 3. 断言验证
        assertEquals(1, errors.size(), "应该产生一个错误");
        assertEquals("node_id_001", errors.get(0).getId());
        assertEquals("Questioner", errors.get(0).getType());
        assertEquals(errorMsg, errors.get(0).getReason());
    }

    @Test
    void test_validateStreamTransformNodeConfig_success() {
        Map<String, Object> configs = new HashMap<>();
        List<String> concat = new ArrayList<>();
        concat.add("raw_output.content");

        Map<String, Object> transformer = new HashMap<>();
        transformer.put("frame_template", "{\"type\":\"message\",\"data\":{\"content\":\"{{raw_output.content}}\",\"riskDescription\":null},\"finish\":\"{{raw_output.finish}}\"}");

        configs.put("transformer", transformer);
        configs.put("concat", concat);
        Node mockNode = Mockito.mock(Node.class);
        when(mockNode.getId()).thenReturn("node_id_001");
        when(mockNode.getType()).thenReturn("StreamTransform");
        WorkflowNodeVO mockNodeInfo = Mockito.mock(WorkflowNodeVO.class);
        when(mockNode.getNodeInfo()).thenReturn(mockNodeInfo);
        when(mockNodeInfo.getConfigs()).thenReturn(configs);
        List<WorkflowValidationVOErrors> errors = new ArrayList<>();


        // 2. 使用 ReflectionTestUtils 调用私有方法
        // 参数依次为：(目标对象, 方法名, 参数1, 参数2)
        ReflectionTestUtils.invokeMethod(workflowValidationService, "validateStreamTransformNodeConfig", mockNode, errors);

        // 3. 断言验证
        assertEquals(0, errors.size(), "应该产生0个错误");
    }

    @Test
    void test_validateStreamTransformNodeConfig_success_1() {
        Map<String, Object> configs = new HashMap<>();
        List<String> concat = new ArrayList<>();
        concat.add("raw_output.test");

        Map<String, Object> transformer = new HashMap<>();
        transformer.put("frame_template", "{\"type\":\"message\",\"data\":{\"content\":\"{{raw_output.content}}\",\"riskDescription\":null},\"finish\":\"{{raw_output.finish}}\"}");

        configs.put("transformer", transformer);
        configs.put("concat", concat);
        Node mockNode = Mockito.mock(Node.class);
        when(mockNode.getId()).thenReturn("node_id_001");
        when(mockNode.getType()).thenReturn("StreamTransform");
        WorkflowNodeVO mockNodeInfo = Mockito.mock(WorkflowNodeVO.class);
        when(mockNode.getNodeInfo()).thenReturn(mockNodeInfo);
        when(mockNodeInfo.getConfigs()).thenReturn(configs);
        List<WorkflowValidationVOErrors> errors = new ArrayList<>();


        // 2. 使用 ReflectionTestUtils 调用私有方法
        // 参数依次为：(目标对象, 方法名, 参数1, 参数2)
        ReflectionTestUtils.invokeMethod(workflowValidationService, "validateStreamTransformNodeConfig", mockNode, errors);

        // 3. 断言验证
        assertEquals(1, errors.size(), "应该产生1个错误");
        assertEquals("node_id_001", errors.get(0).getId());
        assertEquals("StreamTransform", errors.get(0).getType());
    }

    @Test
    void test_validateStreamTransformNodeConfig_success_2() {
        Map<String, Object> configs = new HashMap<>();
        List<String> concat = new ArrayList<>();
        concat.add("raw_output.content");

        Map<String, Object> transformer = new HashMap<>();
        transformer.put("frame_template", "");

        configs.put("transformer", transformer);
        configs.put("concat", concat);
        Node mockNode = Mockito.mock(Node.class);
        when(mockNode.getId()).thenReturn("node_id_001");
        when(mockNode.getType()).thenReturn("StreamTransform");
        WorkflowNodeVO mockNodeInfo = Mockito.mock(WorkflowNodeVO.class);
        when(mockNode.getNodeInfo()).thenReturn(mockNodeInfo);
        when(mockNodeInfo.getConfigs()).thenReturn(configs);
        List<WorkflowValidationVOErrors> errors = new ArrayList<>();


        // 2. 使用 ReflectionTestUtils 调用私有方法
        // 参数依次为：(目标对象, 方法名, 参数1, 参数2)
        ReflectionTestUtils.invokeMethod(workflowValidationService, "validateStreamTransformNodeConfig", mockNode, errors);

        // 3. 断言验证
        assertEquals(2, errors.size(), "应该产生2个错误");
        assertEquals("node_id_001", errors.get(0).getId());
        assertEquals("StreamTransform", errors.get(0).getType());
    }

    @Test
    void test_validateStreamTransformNodeConfig_success_3() {
        Map<String, Object> configs = new HashMap<>();
        List<String> concat = new ArrayList<>();
        concat.add("raw_output.content");

        Map<String, Object> transformer = new HashMap<>();
        transformer.put("frame_template", "raw_output.content");

        configs.put("transformer", transformer);
        configs.put("concat", concat);
        Node mockNode = Mockito.mock(Node.class);
        when(mockNode.getId()).thenReturn("node_id_001");
        when(mockNode.getType()).thenReturn("StreamTransform");
        WorkflowNodeVO mockNodeInfo = Mockito.mock(WorkflowNodeVO.class);
        when(mockNode.getNodeInfo()).thenReturn(mockNodeInfo);
        when(mockNodeInfo.getConfigs()).thenReturn(configs);
        List<WorkflowValidationVOErrors> errors = new ArrayList<>();


        // 2. 使用 ReflectionTestUtils 调用私有方法
        // 参数依次为：(目标对象, 方法名, 参数1, 参数2)
        ReflectionTestUtils.invokeMethod(workflowValidationService, "validateStreamTransformNodeConfig", mockNode, errors);

        // 3. 断言验证
        assertEquals(1, errors.size(), "应该产生1个错误");
        assertEquals("node_id_001", errors.get(0).getId());
        assertEquals("StreamTransform", errors.get(0).getType());
    }
}
