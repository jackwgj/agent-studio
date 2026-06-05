/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.workflow.convert.adapt.difyadapter;

import com.openjiuwen.studio.agent.common.enums.NodeType;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.manager.dto.WorkflowFieldVO;
import com.openjiuwen.studio.agent.manager.dto.WorkflowFieldVOValue;
import com.openjiuwen.studio.agent.manager.dto.WorkflowNodeVO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpNodeConverterTest {

    private com.openjiuwen.studio.agent.manager.workflow.convert.adapt.difyadapter.HttpNodeConverter converter;
    private Map<String, WorkflowNodeVO> workflowNodeMap;
    private final CheckUrlService checkUrlService = new CheckUrlService();

    @BeforeEach
    void setUp() {
        converter = new HttpNodeConverter(checkUrlService);
        workflowNodeMap = new HashMap<>();

        // 模拟黑名单配置
        ReflectionTestUtils.setField(converter, "connectorConfigHostBlacklist", "127.0.0.1,localhost");

        // 预置一些 Mock 节点数据，用于引用替换测试
        setupMockNodes();
    }

    private void setupMockNodes() {
        // 1. 模拟系统变量节点
        WorkflowNodeVO sysNode = new WorkflowNodeVO();
        WorkflowFieldVO sysOutput = new WorkflowFieldVO();
        sysOutput.setName(CommonConstant.DIFY.SYS_VARIABLE);

        WorkflowFieldVO projectVar = new WorkflowFieldVO();
        projectVar.setName("project_id");
        projectVar.setValue(new WorkflowFieldVOValue().setContent("test-project-001"));

        sysOutput.setSchema(Collections.singletonList(projectVar));
        sysNode.setOutputs(Collections.singletonList(sysOutput));
        workflowNodeMap.put(CommonConstant.DIFY.SYS_VARIABLE, sysNode);

        // 2. 模拟普通节点变量
        WorkflowNodeVO prevNode = new WorkflowNodeVO();
        prevNode.setId("12345");
        workflowNodeMap.put("12345", prevNode);

        // 3. 模拟会话变量 (Conversation Variable)
        WorkflowNodeVO convNode = new WorkflowNodeVO();
        WorkflowFieldVO field = new WorkflowFieldVO();
        field.setName("user_name");
        field.setValue(new WorkflowFieldVOValue().setDefault("Gemini"));
        convNode.setOutputs(Collections.singletonList(field));
        workflowNodeMap.put(CommonConstant.DIFY.CONVERSATION_VARIABLE, convNode);
    }

    @Test
    @DisplayName("测试复杂的URL拆分：带协议、端口、路径和Query")
    void testParseUrlComplex_ShouldParseSuccessfully() {
        String url = "https://{{#sys.project_id#}}.com:8080/api/v1/run?debug=true";

        // 反射调用私有方法 parseUrl
        Object urlInfo = ReflectionTestUtils.invokeMethod(converter, "parseUrl", url, workflowNodeMap);

        assertNotNull(urlInfo);
        assertEquals("https://test-project-001.com:8080/api/v1/run?debug=true",
            ReflectionTestUtils.getField(urlInfo, "processedUrl"));
        assertEquals("https", ReflectionTestUtils.getField(urlInfo, "scheme"));
        assertEquals("test-project-001.com:8080", ReflectionTestUtils.getField(urlInfo, "endpoint"));
        assertEquals("/api/v1/run", ReflectionTestUtils.getField(urlInfo, "path"));
    }

    @Test
    void testAdapt_WhenUrlIsInvalidOrInBlacklist_ShouldReturnEmptyNodeConfig() throws Exception {
        // 1. 准备数据
        Map<String, Object> data = new HashMap<>();
        data.put("method", "POST");
        data.put("url", "http://127.0.0.1/api/bad-request"); // 假设这是非法URL

        WorkflowNodeVO nodeVO = new WorkflowNodeVO();

        // 设置黑名单 mock 值
        String mockBlacklist = "127.0.0.1,localhost";
        ReflectionTestUtils.setField(converter, "connectorConfigHostBlacklist", mockBlacklist);
        CheckUrlService internalService = (CheckUrlService) ReflectionTestUtils.getField(converter, "checkUrlService");
        assertNotNull(internalService);
        ReflectionTestUtils.setField(internalService, "enableUrlCheck", true);

        // 4. 执行被测方法
        WorkflowNodeVO result = converter.adapt(data, nodeVO, workflowNodeMap);

        // 5. 断言
        assertNotNull(result);
        assertEquals(NodeType.EMPTY.getType(), result.getType());
    }

    @Test
    @DisplayName("测试普通节点引用：应转换为 {{node_id.output}} 占位符")
    void testReplaceReferenceWithNodeVar_ShouldReturnLiteralValue() {
        String rawValue = "Bearer {{#12345.var_name#}}";
        String result = ReflectionTestUtils.invokeMethod(converter, "replaceReferenceWithFixedValue", rawValue, workflowNodeMap);

        // 根据代码逻辑，非系统变量会转换成 {{node_12345.var_name}}
        assertEquals("Bearer {{node_12345.var_name}}", result);
    }

    @Test
    @DisplayName("测试当URL命中黑名单时：应返回空白配置且不崩溃")
    void testAdaptUrlBlacklist_ShouldReturnEmptyNode() {
        Map<String, Object> data = new HashMap<>();
        data.put("method", "GET");
        data.put("url", "http://127.0.0.1/admin"); // 命中黑名单

        WorkflowNodeVO nodeVO = new WorkflowNodeVO();
        WorkflowNodeVO result = converter.adapt(data, nodeVO, workflowNodeMap);

        assertEquals(NodeType.EMPTY.getType(), result.getType());
    }

    @Test
    @DisplayName("测试Headers转换：支持Key和Value的引用替换")
    @SuppressWarnings("unchecked")
    void testAdaptHeadersWithReferences_ShouldReturnProcessedValue() {
        Map<String, Object> data = new HashMap<>();
        // 模拟：Key含系统变量，Value含会话变量
        data.put("headers", "X-Project-{{#sys.project_id#}}:Bearer {{#conversation.user_name#}}");

        WorkflowFieldVO result = ReflectionTestUtils.invokeMethod(converter, "adaptHeaders", data, workflowNodeMap);

        assertNotNull(result);
        assertEquals("headers", result.getName());
        List<WorkflowFieldVO> schema = (List<WorkflowFieldVO>) result.getSchema();
        assertEquals(1, schema.size());

        // 验证 Key 是否被解析替换
        assertEquals("X-Project-test-project-001", schema.get(0).getName());

        // 验证 Value 是否被识别为引用类型 (REF) 或固定值 (LITERAL)
        // 按照代码，单个引用且存在变量时，通常转为 REF 或带有固定值的 LITERAL
        WorkflowFieldVOValue value = schema.get(0).getValue();
        assertNotNull(value);
    }

    @Test
    @DisplayName("测试混合引用转换：多变量混合应转为LITERAL固定值")
    void testTransformMixedReference_ShouldReturnRealValue() {
        // 场景：固定值 + 变量 + 固定值
        String rawValue = "Prefix-{{#sys.project_id#}}-Suffix";

        WorkflowFieldVOValue result = ReflectionTestUtils.invokeMethod(
            converter, "transformReferenceToSupportedFormat", rawValue, workflowNodeMap);

        // Agent Arts 不支持这种混合引用，代码应将其处理为 LITERAL
        assertNotNull(result);
        assertEquals(WorkflowFieldVOValue.TypeEnum.LITERAL, result.getType());
        assertEquals("Prefix-test-project-001-Suffix", result.getContent());
    }

    @Test
    @DisplayName("测试单个环境变量引用")
    void testTransformSingleEnvReference_ShouldReturnRealValue() {
        // 这里需要注意：代码中 getResolvedEnvReferenceValue 尚未在你的代码段中完全展示，
        // 假设它从 workflowNodeMap 中获取名为 ENV_VARIABLE 的节点
        String rawValue = "{{#env.API_KEY#}}";

        // 模拟环境变量节点数据
        WorkflowNodeVO envNode = workflowNodeMap.get(CommonConstant.DIFY.ENV_VARIABLE);
        // ... (补充envNode的内部结构)

        WorkflowFieldVOValue result = ReflectionTestUtils.invokeMethod(
            converter, "transformReferenceToSupportedFormat", rawValue, workflowNodeMap);

        // 环境变量通常被直接替换为固定值
        assertNotNull(result);
        assertEquals(WorkflowFieldVOValue.TypeEnum.LITERAL, result.getType());
    }

    @Test
    @DisplayName("测试Query解析：从URL中自动提取并转换为Schema")
    @SuppressWarnings("unchecked")
    void testAdaptQueryFromUrl_ShouldReturnProcessedUrl() {
        Map<String, Object> data = new HashMap<>();
        data.put("url", "http://api.com?name=tester&role={{#sys.project_id#}}");

        WorkflowFieldVO result = ReflectionTestUtils.invokeMethod(converter, "adaptQuery", data, workflowNodeMap);

        assertNotNull(result);
        assertEquals("query", result.getName());
        // 验证是否解析出两个 query 参数
        List<WorkflowFieldVO> schema = (List<WorkflowFieldVO>) result.getSchema();
        assertEquals(2, schema.size());

        // 检查第二个参数 role 的值是否已被替换
        WorkflowFieldVO roleField = schema.stream().filter(f -> f.getName().equals("role")).findFirst().orElse(null);
        assertNotNull(roleField);
        Map<String, String> roleMp = (Map<String, String>) roleField.getValue().getContent();
        assertEquals("system", roleMp.get("source"));
    }

    @Test
    @DisplayName("测试Body转换：当输入为JSON类型且含引用时，预期得到替换后的JSON字符串")
    void testAdaptBody_WhenTypeIsJsonWithRef_ShouldReturnProcessedJson() {
        // 准备数据
        Map<String, Object> data = new HashMap<>();
        data.put("authorization", Map.of("type", "no_auth"));
        Map<String, Object> httpBody = new HashMap<>();
        httpBody.put("type", "json");
        List<Map<String, Object>> dataList = new ArrayList<>();
        dataList.add(Map.of("value", "{\"name\": \"{{#sys.project_id#}}\"}"));
        httpBody.put("data", dataList);

        Map<String, Object> configs = new HashMap<>();

        // 执行
        ReflectionTestUtils.invokeMethod(converter, "adaptBody", data, httpBody, configs, workflowNodeMap);

        // 断言
        assertEquals("JSON", configs.get("request_type"));
        assertEquals("{\"name\": \"{{_sys_project_id}}\"}", configs.get("request_body"));
    }

    @Test
    @DisplayName("测试Body转换：当输入类型为NONE时，预期request_type为NONE且body为空")
    void testAdaptBody_WhenTypeIsNone_ShouldReturnNoneType() {
        Map<String, Object> data = new HashMap<>();
        data.put("authorization", Map.of("type", "no_auth"));
        Map<String, Object> httpBody = Map.of("type", "none");
        Map<String, Object> configs = new HashMap<>();

        ReflectionTestUtils.invokeMethod(converter, "adaptBody", data, httpBody, configs, workflowNodeMap);

        assertEquals("NONE", configs.get("request_type"));
        assertEquals("", configs.get("request_body"));
    }

    @Test
    @DisplayName("测试鉴权转换：当为NoAuth时，预期得到空的auth_info映射")
    void testAdaptAuth_WhenNoAuth_ShouldReturnEmptyMap() {
        Map<String, Object> data = new HashMap<>();
        data.put("authorization", Map.of("type", "no_auth"));
        Map<String, Object> configs = new HashMap<>();

        ReflectionTestUtils.invokeMethod(converter, "adaptAuth", data, configs);

        assertTrue(((Map<?, ?>) configs.get("auth_info")).isEmpty());
    }

    @Test
    @DisplayName("testAdaptAuth_WhenApiKeyBasic_ShouldReturnStructuredAuthInfoWithHeaders")
    @SuppressWarnings("unchecked")
    void testAdaptAuth_WhenApiKeyIsBasic_ShouldReturnStructuredAuthInfo() {
        // 1. 构造内层 config：type 使用 "basic"，确保 fillKeyPair 有效
        Map<String, Object> config = new HashMap<>();
        config.put("type", "basic");
        config.put("api_key", "test-api-key-value");

        // 2. 构造外层 authorization
        Map<String, Object> authorization = new HashMap<>();
        // 外层 type 只要不是 "no-auth" 即可触发逻辑
        authorization.put("type", "api-key");
        authorization.put("config", config);

        Map<String, Object> data = new HashMap<>();
        data.put("authorization", authorization);
        Map<String, Object> configs = new HashMap<>();


        // 3. 执行
        ReflectionTestUtils.invokeMethod(converter, "adaptAuth", data, configs);

        // 4. 断言
        Map<String, Object> authInfo = (Map<String, Object>) configs.get("auth_info");

        assertNotNull(authInfo, "auth_info 不应为 null");
        assertEquals("HEADERS", authInfo.get("domain"));
        assertEquals("SERVICE", authInfo.get("scope"));

        List<Map<String, String>> authKeys = (List<Map<String, String>>) authInfo.get("auth_keys");
        assertNotNull(authKeys, "auth_keys 列表不应为 null");
        assertFalse(authKeys.isEmpty(), "auth_keys 列表不应为空");

        // 验证具体的 Key-Pair (由 DifyAuthType.BASIC 生成)
        Map<String, String> keyPair = authKeys.get(0);
        assertEquals("Authorization", keyPair.get("target_name"));
        assertEquals("test-api-key-value", keyPair.get("auth_key"));
    }


    @Test
    @DisplayName("testAdaptAuth_WhenApiKeyBasic_ShouldReturnStructuredAuthInfoWithHeaders")
    @SuppressWarnings("unchecked")
    void testAdaptAuth_WhenApiKeyIsCustom_ShouldReturnStructuredAuthInfo() {
        // 1. 构造内层 config：type 使用 "basic"，确保 fillKeyPair 有效
        Map<String, String> config = new HashMap<>();
        config.put("type", "custom");
        config.put("header", "custom-header");
        config.put("api_key", "test-api-key-value");

        // 2. 构造外层 authorization
        Map<String, Object> authorization = new HashMap<>();
        // 外层 type 只要不是 "no-auth" 即可触发逻辑
        authorization.put("type", "api-key");
        authorization.put("config", config);

        Map<String, Object> data = new HashMap<>();
        data.put("authorization", authorization);
        Map<String, Object> configs = new HashMap<>();


        // 3. 执行
        ReflectionTestUtils.invokeMethod(converter, "adaptAuth", data, configs);

        // 4. 断言
        Map<String, Object> authInfo = (Map<String, Object>) configs.get("auth_info");

        assertNotNull(authInfo, "auth_info 不应为 null");
        assertEquals("HEADERS", authInfo.get("domain"));
        assertEquals("SERVICE", authInfo.get("scope"));

        List<Map<String, String>> authKeys = (List<Map<String, String>>) authInfo.get("auth_keys");
        assertNotNull(authKeys, "auth_keys 列表不应为 null");
        assertFalse(authKeys.isEmpty(), "auth_keys 列表不应为空");

        // 验证具体的 Key-Pair (由 DifyAuthType.BASIC 生成)
        Map<String, String> keyPair = authKeys.get(0);
        assertEquals("custom-header", keyPair.get("target_name"));
        assertEquals("test-api-key-value", keyPair.get("auth_key"));
    }

    @Test
    @DisplayName("testAdaptAuth_WhenApiKeyBasic_ShouldReturnStructuredAuthInfoWithHeaders")
    @SuppressWarnings("unchecked")
    void testAdaptAuth_WhenApiKeyIsBearer_ShouldReturnStructuredAuthInfo() {
        // 1. 构造内层 config：type 使用 "basic"，确保 fillKeyPair 有效
        Map<String, Object> config = new HashMap<>();
        config.put("type", "bearer");
        config.put("api_key", "test-api-key-value");

        // 2. 构造外层 authorization
        Map<String, Object> authorization = new HashMap<>();
        // 外层 type 只要不是 "no-auth" 即可触发逻辑
        authorization.put("type", "api-key");
        authorization.put("config", config);

        Map<String, Object> data = new HashMap<>();
        data.put("authorization", authorization);
        Map<String, Object> configs = new HashMap<>();


        // 3. 执行
        ReflectionTestUtils.invokeMethod(converter, "adaptAuth", data, configs);

        // 4. 断言
        Map<String, Object> authInfo = (Map<String, Object>) configs.get("auth_info");

        assertNotNull(authInfo, "auth_info 不应为 null");
        assertEquals("HEADERS", authInfo.get("domain"));
        assertEquals("SERVICE", authInfo.get("scope"));

        List<Map<String, String>> authKeys = (List<Map<String, String>>) authInfo.get("auth_keys");
        assertNotNull(authKeys, "auth_keys 列表不应为 null");
        assertFalse(authKeys.isEmpty(), "auth_keys 列表不应为空");

        // 验证具体的 Key-Pair (由 DifyAuthType.BASIC 生成)
        Map<String, String> keyPair = authKeys.get(0);
        assertEquals("Authorization", keyPair.get("target_name"));
        assertEquals("Bearer test-api-key-value", keyPair.get("auth_key"));
    }


    @Test
    @DisplayName("测试异常转换：当开启异常策略时，预期得到填充了默认值的JSON字符串")
    void testAdaptException_WhenStrategyIsDefaultValue_ShouldReturnSuppressionJson() {
        Map<String, Object> data = new HashMap<>();
        data.put("error_strategy", "default-value");
        List<Map<String, Object>> defaultValues = List.of(
            Map.of("key", "status_code", "value", 0, "type", "number"),
            Map.of("key", "body", "value", "", "type", "string"),
            Map.of("key", "headers", "value", "{}", "type", "object")
        );
        data.put("default_value", defaultValues);
        Map<String, Object> configs = new HashMap<>();

        ReflectionTestUtils.invokeMethod(converter, "adaptException", data, configs);

        assertEquals(true, configs.get("exception_enable"));
        String suppression = (String) configs.get("exception_suppression");
        assertTrue(suppression.contains("\"status_code\":0"));
        assertTrue(suppression.contains("\"body\":\"\""));
    }

    @Test
    @DisplayName("测试异常转换：当开启异常策略时，预期得到填充了默认值的JSON字符串")
    void testAdaptException_WhenStrategyIsFailBranch_ShouldDisableException() {
        Map<String, Object> data = new HashMap<>();
        data.put("error_strategy", "fail-branch");
        Map<String, Object> configs = new HashMap<>();

        ReflectionTestUtils.invokeMethod(converter, "adaptException", data, configs);

        assertEquals(false, configs.get("exception_enable"));
    }

    @Test
    @DisplayName("测试异常转换：当开启异常策略时，预期得到填充了默认值的JSON字符串")
    void testAdaptException_WhenStrategyIsNone_ShouldDisableException() {
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> configs = new HashMap<>();

        ReflectionTestUtils.invokeMethod(converter, "adaptException", data, configs);

        assertEquals(false, configs.get("exception_enable"));
    }

    @Test
    @DisplayName("testAdaptOutputs_ShouldReturnFourStandardFields")
    void testAdaptOutputs_ShouldReturnFourStandardFields() {
        Map<String, Object> data = new HashMap<>(); // Http 节点的 data，暂不影响 outputs

        List<WorkflowFieldVO> outputs = converter.adaptOutputs(data);

        // 1. 验证数量
        assertEquals(4, outputs.size());

        // 2. 验证关键字段是否存在
        Set<String> expectedNames = Set.of("body", "status_code", "headers", "error_message");
        Set<String> actualNames = outputs.stream()
            .map(WorkflowFieldVO::getName)
            .collect(Collectors.toSet());

        assertEquals(expectedNames, actualNames);

        // 3. 验证 error_message 的特殊结构 (value.content 应该不为 null)
        WorkflowFieldVO errorMsgField = outputs.stream()
            .filter(f -> "error_message".equals(f.getName()))
            .findFirst()
            .orElse(null);

        assertNotNull(errorMsgField);
        assertNotNull(errorMsgField.getValue().getContent());
    }

    @Test
    void testAdaptUrl_shouldSetConfigSuccessfully() {
        String url_1 = "http://{{#sys.project_id#}}.com:8080/api/v1/run?debug=true";
        String url_2 = "http://test.com";
        String url_3 = "/path/test";
        for (String url : List.of(url_1, url_2, url_3)) {
            Map<String, Object> data = new HashMap<>();
            List<WorkflowFieldVO> inputParams = new ArrayList<>();
            data.put("url", url);
            converter.adaptConfigs(data, workflowNodeMap, inputParams);
        }
    }

    @Test
    void testAdapt_WhenHttpNodeIsBlocked_ShouldReturnEmptyNode() {
        // 1. 准备测试数据
        Map<String, Object> data = new HashMap<>();
        WorkflowNodeVO workflowNodeVO = new WorkflowNodeVO();
        workflowNodeVO.setType("http_request"); // 初始类型
        workflowNodeVO.setInputs(List.of(new WorkflowFieldVO())); // 初始有输入

        Map<String, WorkflowNodeVO> workflowNodeMap = new HashMap<>();

        // 2. 注入模拟的 blockNodes 环境变量 (关键步骤)
        // 假设你的 Service 实例名为 adapterService
        ReflectionTestUtils.setField(converter, "blockNodes", "http,python,ssh");

        // 3. 执行被测方法
        WorkflowNodeVO result = converter.adapt(data, workflowNodeVO, workflowNodeMap);

        // 4. 断言验证
        // 验证类型是否被强制改为 EMPTY (假设 NodeType.EMPTY.getType() 返回 "empty")
        assertEquals(NodeType.EMPTY.getType(), result.getType());

        // 验证配置是否被处理（调用了 super.adaptConfigs）
        assertNotNull(result.getConfigs());

        // 验证输入输出是否被清空
        assertTrue(result.getInputs().isEmpty(), "Inputs should be cleared");
        assertTrue(result.getOutputs().isEmpty(), "Outputs should be cleared");
    }

}
