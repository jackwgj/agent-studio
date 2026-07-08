/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.workflow;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.CryptoUtils;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.common.utils.SpringBeanUtils;
import com.openjiuwen.studio.agent.manager.constant.Constants;
import com.openjiuwen.studio.agent.manager.dto.MemoryConfigIR;
import com.openjiuwen.studio.agent.manager.dto.ModelConfig;
import com.openjiuwen.studio.agent.manager.dto.WorkflowVO;
import com.openjiuwen.studio.agent.manager.entity.Agent;
import com.openjiuwen.studio.agent.manager.mapper.MappingMapper;
import com.openjiuwen.studio.agent.manager.mapper.ReleaseVersionMapper;
import com.openjiuwen.studio.agent.manager.mapper.ToolMapper;
import com.openjiuwen.studio.agent.manager.obs.MgObsService;
import com.openjiuwen.studio.agent.manager.service.IrAdapterService;
import com.openjiuwen.studio.agent.manager.service.ToolManagementService;
import com.openjiuwen.studio.agent.manager.service.WorkflowManagementService;
import com.openjiuwen.studio.agent.manager.service.md.ModelServiceManager;
import com.openjiuwen.studio.agent.manager.service.plugin.IPlugin;
import com.openjiuwen.studio.agent.manager.service.plugin.impl.PluginBaseImpl;
import com.openjiuwen.studio.agent.manager.utils.BaseTest;
import com.openjiuwen.studio.agent.manager.utils.CommonUtil;
import com.openjiuwen.studio.agent.manager.utils.JsonUtils;
import com.openjiuwen.studio.agent.manager.utils.TestUtil;
import com.openjiuwen.studio.common.service.service.EncryptionAdapter;

import lombok.SneakyThrows;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 功能描述
 *
 */
@Transactional
@Sql(scripts = {"classpath:sql/t_rollback.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class IRAdapterTest extends BaseTest {
    private static MockedStatic<SpringBeanUtils> mockedStatic;

    private static MockedStatic<CommonUtil> commonMock;

    private static MockedStatic<CryptoUtils> mockedCryptoUtils;

    @Mock
    private PluginBaseImpl pluginBase;

    @Mock
    private IPlugin pluginService;

    @MockitoSpyBean
    private EncryptionAdapter encryptionAdapter;

    @Autowired
    private IrAdapterService irAdapterService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MgObsService mgObsService;

    @Autowired
    private WorkflowManagementService workflowManagementService;

    @Autowired
    private ToolManagementService toolManagementService;

    private AutoCloseable mockitoCloseable;

    @Autowired
    private ReleaseVersionMapper releaseVersionMapper;

    @Autowired
    private ToolMapper toolMapper;

    @Autowired
    private ModelServiceManager modelServiceManager;

    @MockitoSpyBean
    private MappingMapper mappingMapper;

    @AfterAll
    static void end() {
        mockedStatic.close();
        mockedCryptoUtils.close();
    }

    @BeforeAll
    public static void initToken() {
        RequestContextUtils.setRequestAuthTokenAndProjectId(Constants.TEST_TOKEN, Constants.TEST_PROJECT_ID);
        mockedStatic = Mockito.mockStatic(SpringBeanUtils.class);
        mockedCryptoUtils = Mockito.mockStatic(CryptoUtils.class);
        mockedCryptoUtils.when(() -> CryptoUtils.decrypt(anyString())).thenReturn("fake_decrypt_result");
        mockedCryptoUtils.when(() -> CryptoUtils.encrypt(anyString())).thenReturn("fake_encrypt_result");
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
        commonMock.close();
    }

    @BeforeEach
    void setUp() throws IOException {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(pluginBase, "mgObsService", mgObsService);
        ReflectionTestUtils.setField(irAdapterService, "mgObsService", mgObsService);
        ReflectionTestUtils.setField(irAdapterService, "knowledgeSource", "KooSearch");
        ReflectionTestUtils.setField(toolManagementService, "obsService", mgObsService);
        mockedStatic.when(() -> SpringBeanUtils.getBean(IrAdapterService.class)).thenReturn(irAdapterService);
        mockedStatic.when(() -> SpringBeanUtils.getBean(WorkflowManagementService.class))
            .thenReturn(workflowManagementService);
        mockedStatic.when(() -> SpringBeanUtils.getBean(ModelServiceManager.class)).thenReturn(modelServiceManager);
        commonMock = Mockito.mockStatic(CommonUtil.class);
        commonMock.when(CommonUtil::getTenantId).thenReturn("system");
    }

    @SneakyThrows
    @Test
    public void testLLMNodeAdapter_stream_exception() {
        String llmJson = TestUtil.getStringFromFile("classpath:flow_dsl/test_llm_stream_exception_dsl.json");
        Map<String, Object> metadata = new HashMap<>();
        WorkflowVO workflowVO = JSON.parseObject(llmJson, WorkflowVO.class);
        assertThrows(AgentStudioException.class, () -> irAdapterService.adaptWorkflow(workflowVO, metadata));
    }

    @SneakyThrows
    @Test
    public void testLLMNodeAdapter_block_exception() {
        String llmJson = TestUtil.getStringFromFile("classpath:flow_dsl/test_llm_block_exception_dsl.json");
        Map<String, Object> metadata = new HashMap<>();
        WorkflowVO workflowVO = JSON.parseObject(llmJson, WorkflowVO.class);
        assertThrows(AgentStudioException.class, () -> irAdapterService.adaptWorkflow(workflowVO, metadata));
    }

    @SneakyThrows
    @Test
    public void testLLMNodeAdapter() {
        String llmJson = TestUtil.getStringFromFile("classpath:flow_dsl/test_llm_flow_dsl.json");
        Map<String, Object> metadata = new HashMap<>();
        WorkflowVO workflowVO = JSON.parseObject(llmJson, WorkflowVO.class);
        Map<String, Object> irConfig = irAdapterService.adaptWorkflow(workflowVO, metadata);
        Assertions.assertEquals(TestUtil.getStringFromFile("classpath:flow_dsl/test_llm_flow_ir.json"),
            JSON.toJSONString(irConfig, JSONWriter.Feature.WriteMapNullValue));
    }

    @SneakyThrows
    @Test
    @Transactional
    public void testVlLLMNodeAdapter() {
        String llmJson = TestUtil.getStringFromFile("classpath:flow_dsl/test_vl_llm_flow_dsl.json");
        Map<String, Object> metadata = new HashMap<>();
        WorkflowVO workflowVO = JSON.parseObject(llmJson, WorkflowVO.class);
        Map<String, Object> irConfig = irAdapterService.adaptWorkflow(workflowVO, metadata);
        Assertions.assertEquals(TestUtil.getStringFromFile("classpath:flow_dsl/test_vl_llm_flow_ir.json"),
            JSON.toJSONString(irConfig, JSONWriter.Feature.WriteMapNullValue));
    }

    @SneakyThrows
    @Test
    public void testCodeNodeAdapter() {
        String llmJson = TestUtil.getStringFromFile("classpath:flow_dsl/test_code_flow_dsl.json");
        Map<String, Object> metadata = new HashMap<>();
        WorkflowVO workflowVO = JSON.parseObject(llmJson, WorkflowVO.class);
        Map<String, Object> irConfig = irAdapterService.adaptWorkflow(workflowVO, metadata);
        Assertions.assertEquals(TestUtil.getStringFromFile("classpath:flow_dsl/test_code_flow_ir.json"),
            JSON.toJSONString(irConfig, JSONWriter.Feature.WriteMapNullValue));
    }

    @SneakyThrows
    @Test
    public void testCodeNodeAdapter_exception_retry() {
        String llmJson = TestUtil.getStringFromFile("classpath:flow_dsl/test_code_exception_retry_dsl.json");
        Map<String, Object> metadata = new HashMap<>();
        WorkflowVO workflowVO = JSON.parseObject(llmJson, WorkflowVO.class);
        assertThrows(AgentStudioException.class, () -> irAdapterService.adaptWorkflow(workflowVO, metadata));
    }

    @SneakyThrows
    @Test
    public void testCodeNodeAdapter_exception_timeout() {
        String llmJson = TestUtil.getStringFromFile("classpath:flow_dsl/test_code_exception_timeout_dsl.json");
        Map<String, Object> metadata = new HashMap<>();
        WorkflowVO workflowVO = JSON.parseObject(llmJson, WorkflowVO.class);
        assertThrows(AgentStudioException.class, () -> irAdapterService.adaptWorkflow(workflowVO, metadata));
    }

    @SneakyThrows
    @Test
    @Sql(scripts = {"classpath:sql/tool_reserve_meeting_room_setup_db.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void testPluginExceptionAdapter() {
        String llmJson = TestUtil.getStringFromFile("classpath:flow_dsl/test_exception_branch.json");
        Map<String, Object> metadata = new HashMap<>();
        WorkflowVO workflowVO = JSON.parseObject(llmJson, WorkflowVO.class);
        Map<String, Object> irConfig = irAdapterService.adaptWorkflow(workflowVO, metadata);
        List<Object> connections = JsonUtils.objectToClass(irConfig.get("connections"));
        Assertions.assertNotNull(connections);
        Assertions.assertEquals(6, connections.size());
    }

    @SneakyThrows
    @Test
    public void testSqlNodeAdapter() {
        String llmJson = TestUtil.getStringFromFile("classpath:flow_dsl/test_sql_flow_dsl.json");
        Map<String, Object> metadata = new HashMap<>();
        WorkflowVO workflowVO = JSON.parseObject(llmJson, WorkflowVO.class);
        Map<String, Object> irConfig = irAdapterService.adaptWorkflow(workflowVO, metadata);
        Assertions.assertEquals(TestUtil.getStringFromFile("classpath:flow_dsl/test_sql_flow_ir.json"),
            JSON.toJSONString(irConfig, JSONWriter.Feature.WriteMapNullValue));
    }

    @SneakyThrows
    @Test
    public void testDataQueryNodeAdapter() {
        String llmJson = TestUtil.getStringFromFile("classpath:flow_dsl/test_data_query_dsl.json");
        Map<String, Object> metadata = new HashMap<>();
        WorkflowVO workflowVO = JSON.parseObject(llmJson, WorkflowVO.class);
        Map<String, Object> irConfig = irAdapterService.adaptWorkflow(workflowVO, metadata);
        Assertions.assertEquals(TestUtil.getStringFromFile("classpath:flow_dsl/test_data_query_ir.json"),
                JSON.toJSONString(irConfig, JSONWriter.Feature.WriteMapNullValue));
    }

    @SneakyThrows
    @Test
    public void testStructuredMessagesNodeAdapter() {
        String llmJson = TestUtil.getStringFromFile("classpath:flow_dsl/test_structuredmessages_flow_dsl.json");
        Map<String, Object> metadata = new HashMap<>();
        WorkflowVO workflowVO = JSON.parseObject(llmJson, WorkflowVO.class);
        Map<String, Object> irConfig = irAdapterService.adaptWorkflow(workflowVO, metadata);
        String expectedJson = TestUtil.getStringFromFile("classpath:flow_dsl/test_structuredmessages_flow_ir.json");
        Object expected = JSON.parse(expectedJson);
        Object actual = JSON.parse(JSON.toJSONString(irConfig, JSONWriter.Feature.WriteMapNullValue));

        Assertions.assertEquals(expected, actual);
    }

    @SneakyThrows
    @Test
    public void testInputNodeAdapter() {
        String inputJson = TestUtil.getStringFromFile("classpath:flow_dsl/test_input_flow_dsl.json");
        Map<String, Object> metadata = new HashMap<>();
        WorkflowVO workflowVO = JSON.parseObject(inputJson, WorkflowVO.class);
        Map<String, Object> irConfig = irAdapterService.adaptWorkflow(workflowVO, metadata);
        Assertions.assertEquals(TestUtil.getStringFromFile("classpath:flow_dsl/test_input_flow_ir.json"),
            JSON.toJSONString(irConfig, JSONWriter.Feature.WriteMapNullValue));
    }

    @SneakyThrows
    @Test
    public void testBranchNodeAdapter() {
        String llmJson = TestUtil.getStringFromFile("classpath:flow_dsl/test_branch_flow_dsl.json");
        Map<String, Object> metadata = new HashMap<>();
        WorkflowVO workflowVO = JSON.parseObject(llmJson, WorkflowVO.class);
        Map<String, Object> irConfig = irAdapterService.adaptWorkflow(workflowVO, metadata);
        Assertions.assertEquals(TestUtil.getStringFromFile("classpath:flow_dsl/test_branch_flow_ir.json"),
            JSON.toJSONString(irConfig, JSONWriter.Feature.WriteMapNullValue));
    }

    @SneakyThrows
    @Test
    @Sql(scripts = {"classpath:sql/mcp_setup_db.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void testMcpNodeAdapter() {
        // set up
        String dslJsonStr = TestUtil.getStringFromFile("classpath:flow_dsl/test_workflow_mcp_dsl.json");
        WorkflowVO workflowVO = JSON.parseObject(dslJsonStr, WorkflowVO.class);

        // run test
        Assertions.assertThrows(Exception.class, () -> irAdapterService.adaptWorkflow(workflowVO, new HashMap<>()));
    }

    @SneakyThrows
    @Test
    public void testKnowledgeRepoNodeAdapterWithCUSTOM() {
        try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(
            RequestContextUtils.class)) {
            // run test
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.set("cust-token","token");
            httpHeaders.set("cust-userid","userid");
            httpHeaders.set("x-auth-token","token");

            mockedStaticRequestContextUtils.when(RequestContextUtils::getHeader).thenReturn(httpHeaders);
            // set up
            ReflectionTestUtils.setField(irAdapterService, "knowledgeSource", "CUSTOM");
            String dslJsonStr =
                TestUtil.getStringFromFile("classpath:flow_dsl/test_lakeSearch_knowledge_repo_flow_dsl.json");
            WorkflowVO workflowVO = JSON.parseObject(dslJsonStr, WorkflowVO.class);
            Map<String, Object> irConfig = irAdapterService.adaptWorkflow(workflowVO, new HashMap<>());
            Assertions.assertEquals(TestUtil.getStringFromFile("classpath:flow_dsl/test_custom_knowledge_repo_flow_ir.json"),
                JSON.toJSONString(irConfig, JSONWriter.Feature.WriteMapNullValue));
        }
    }

    @SneakyThrows
    @Test
    public void testMessageNodeAdapter() {
        String llmJson = TestUtil.getStringFromFile("classpath:flow_dsl/test_message_flow_dsl.json");
        Map<String, Object> metadata = new HashMap<>();
        WorkflowVO workflowVO = JSON.parseObject(llmJson, WorkflowVO.class);
        Map<String, Object> irConfig = irAdapterService.adaptWorkflow(workflowVO, metadata);
        Assertions.assertEquals(TestUtil.getStringFromFile("classpath:flow_dsl/test_message_ir_dsl.json"),
            JSON.toJSONString(irConfig, JSONWriter.Feature.WriteMapNullValue));
    }

    @SneakyThrows
    @Test
    public void testQuestionerNodeAdapter() {
        String llmJson = TestUtil.getStringFromFile("classpath:flow_dsl/test_question_flow_dsl.json");
        Map<String, Object> metadata = new HashMap<>();
        WorkflowVO workflowVO = JSON.parseObject(llmJson, WorkflowVO.class);
        Map<String, Object> irConfig = irAdapterService.adaptWorkflow(workflowVO, metadata);
        Assertions.assertEquals(TestUtil.getStringFromFile("classpath:flow_dsl/test_question_ir_dsl.json"),
            JSON.toJSONString(irConfig, JSONWriter.Feature.WriteMapNullValue));
    }

    @SneakyThrows
    @Test
    public void testWorkflowNodeAdapter() {
        String dsl = TestUtil.getStringFromFile("classpath:flow_dsl/test_workflow_flow_dsl.json");
        Map<String, Object> metadata = new HashMap<>();
        WorkflowVO workflowVO = JSON.parseObject(dsl, WorkflowVO.class);
        Map<String, Object> irConfig = irAdapterService.adaptWorkflow(workflowVO, metadata);
        Assertions.assertEquals(TestUtil.getStringFromFile("classpath:flow_dsl/test_workflow_flow_ir.json"),
            JSON.toJSONString(irConfig, JSONWriter.Feature.WriteMapNullValue));
    }

    @SneakyThrows
    @Test
    public void testWorkflowNodeAdapter_exception_retry() {
        String dsl = TestUtil.getStringFromFile("classpath:flow_dsl/test_workflow_exception_retry_dsl.json");
        Map<String, Object> metadata = new HashMap<>();
        WorkflowVO workflowVO = JSON.parseObject(dsl, WorkflowVO.class);
        assertThrows(AgentStudioException.class, () -> irAdapterService.adaptWorkflow(workflowVO, metadata));
    }

    @SneakyThrows
    @Test
    public void testAggregationNodeAdapter() {
        String llmJson = TestUtil.getStringFromFile("classpath:flow_dsl/test_aggregation_flow_dsl.json");
        Map<String, Object> metadata = new HashMap<>();
        WorkflowVO workflowVO = JSON.parseObject(llmJson, WorkflowVO.class);
        Map<String, Object> irConfig = irAdapterService.adaptWorkflow(workflowVO, metadata);
        Assertions.assertEquals(TestUtil.getStringFromFile("classpath:flow_dsl/test_aggregation_flow_ir.json"),
            JSON.toJSONString(irConfig, JSONWriter.Feature.WriteMapNullValue));
    }

    @SneakyThrows
    @Test
    public void testSetVariableNodeAdapter() {
        String llmJson = TestUtil.getStringFromFile("classpath:flow_dsl/test_set_variable_flow_dsl.json");
        Map<String, Object> metadata = new HashMap<>();
        WorkflowVO workflowVO = JSON.parseObject(llmJson, WorkflowVO.class);
        Map<String, Object> irConfig = irAdapterService.adaptWorkflow(workflowVO, metadata);
        Assertions.assertEquals(TestUtil.getStringFromFile("classpath:flow_dsl/test_set_variable_flow_ir.json"),
            JSON.toJSONString(irConfig, JSONWriter.Feature.WriteMapNullValue));
    }

    @SneakyThrows
    @Test
    public void testLoopNodeAdapter() {
        String llmJson = TestUtil.getStringFromFile("classpath:flow_dsl/test_loop_flow_dsl.json");
        Map<String, Object> metadata = new HashMap<>();
        WorkflowVO workflowVO = JSON.parseObject(llmJson, WorkflowVO.class);
        Map<String, Object> irConfig = irAdapterService.adaptWorkflow(workflowVO, metadata);
        Assertions.assertEquals(TestUtil.getStringFromFile("classpath:flow_dsl/test_loop_flow_ir.json"),
            JSON.toJSONString(irConfig, JSONWriter.Feature.WriteMapNullValue));

        String llmJson1 = TestUtil.getStringFromFile("classpath:flow_dsl/test_loop_num_dsl.json");
        Map<String, Object> meta1 = new HashMap<>();
        WorkflowVO workflowVO1 = JSON.parseObject(llmJson1, WorkflowVO.class);
        Assertions.assertThrows(AgentStudioException.class, () -> irAdapterService.adaptWorkflow(workflowVO1, meta1));
    }

    @SneakyThrows
    @Test
    public void testLoopFullTestAdapter() {
        String llmJson = TestUtil.getStringFromFile("classpath:flow_dsl/loop_full_test_flow_dsl.json");
        Map<String, Object> metadata = new HashMap<>();
        WorkflowVO workflowVO = JSON.parseObject(llmJson, WorkflowVO.class);
        Map<String, Object> irConfig = irAdapterService.adaptWorkflow(workflowVO, metadata);
        Assertions.assertEquals(TestUtil.getStringFromFile("classpath:flow_dsl/loop_full_test_flow_ir.json"),
            JSON.toJSONString(irConfig, JSONWriter.Feature.WriteMapNullValue));
    }

    @SneakyThrows
    @Test
    public void testSystemVariableTestAdapter() {
        String llmJson = TestUtil.getStringFromFile("classpath:flow_dsl/test_system_flow_dsl.json");
        Map<String, Object> metadata = new HashMap<>();
        WorkflowVO workflowVO = JSON.parseObject(llmJson, WorkflowVO.class);
        Map<String, Object> irConfig = irAdapterService.adaptWorkflow(workflowVO, metadata);
        Assertions.assertEquals(TestUtil.getStringFromFile("classpath:flow_dsl/test_system_flow_ir.json"),
            JSON.toJSONString(irConfig, JSONWriter.Feature.WriteMapNullValue));
    }

    @SneakyThrows
    @Test
    public void testAssignmentVariableTestAdapter() {
        String llmJson = TestUtil.getStringFromFile("classpath:flow_dsl/test_assignment_flow_dsl.json");
        Map<String, Object> metadata = new HashMap<>();
        WorkflowVO workflowVO = JSON.parseObject(llmJson, WorkflowVO.class);
        Map<String, Object> irConfig = irAdapterService.adaptWorkflow(workflowVO, metadata);
        Assertions.assertEquals(TestUtil.getStringFromFile("classpath:flow_dsl/test_assignment_flow_ir.json"),
            JSON.toJSONString(irConfig, JSONWriter.Feature.WriteMapNullValue));
    }

    @SneakyThrows
    @Test
    public void testMemoryVariableTestAdapter() {
        String llmJson = TestUtil.getStringFromFile("classpath:flow_dsl/test_memory_flow_dsl.json");
        Map<String, Object> metadata = new HashMap<>();
        WorkflowVO workflowVO = JSON.parseObject(llmJson, WorkflowVO.class);
        Map<String, Object> irConfig = irAdapterService.adaptWorkflow(workflowVO, metadata);
        Assertions.assertEquals(TestUtil.getStringFromFile("classpath:flow_dsl/test_memory_flow_ir.json"),
            JSON.toJSONString(irConfig, JSONWriter.Feature.WriteMapNullValue));
    }

    @SneakyThrows
    @Test
    public void testHttpTestAdapter() {
        String llmJson = TestUtil.getStringFromFile("classpath:flow_dsl/test_http_flow_dsl.json");
        Map<String, Object> metadata = new HashMap<>();
        WorkflowVO workflowVO = JSON.parseObject(llmJson, WorkflowVO.class);
        Map<String, Object> irConfig = irAdapterService.adaptWorkflow(workflowVO, metadata);
        Assertions.assertEquals(TestUtil.getStringFromFile("classpath:flow_dsl/test_http_flow_ir.json"),
            JSON.toJSONString(irConfig, JSONWriter.Feature.WriteMapNullValue));
    }

    private Agent mockAgent() {
        Agent agent = new Agent();
        agent.setAgentId("test_agent_id");
        agent.setName("测试Agent");
        agent.setDescription("测试Agent描述");
        agent.setProjectId(Constants.TEST_PROJECT_ID);
        agent.setModelConfig(new ModelConfig());
        agent.setModelDeploymentId("deploymentId");
        agent.setWorkspaceId(Constants.TEST_WORKSPACE_ID);
        agent.setSchedulingMode("ToolUse");
        agent.setPlanQaIndependent(true);
        agent.setPlanModelDeploymentId("planModelDeploymentId");
        agent.setPlanModel("planModelMode");
        agent.setPlanModelType("planModelType");
        agent.setPlanModelName("planModelName");
        ModelConfig planModelConfig = new ModelConfig();
        planModelConfig.setMaxTokens(1000);
        agent.setPlanModelConfig(planModelConfig);
        return agent;
    }

    private Map<String, Object> mockMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("projectId", Constants.TEST_PROJECT_ID);
        metadata.put("updatedAt", 1734016143379L);
        return metadata;
    }

    private void mockSccCryptoUtil() {
        mockedStatic.when(() -> CryptoUtils.decrypt(anyString())).thenReturn("fake_decrypt_result");
        mockedStatic.when(() -> CryptoUtils.encrypt(anyString())).thenReturn("fake_encrypt_result");

        // 加密依然返回合法的 E1 格式，防止抛出 AgentStudioException
        Mockito.doReturn("E1-{fake-uuid}-fake_encrypt_result")
                .when(encryptionAdapter).encrypt(anyString(), any());

        // 解密改回 fake_decrypt_result，匹配测试里的 Assertions
        Mockito.doReturn("fake_decrypt_result")
                .when(encryptionAdapter).decrypt(anyString());
        Mockito.doReturn("fake_decrypt_result")
                .when(encryptionAdapter).decrypt(anyString(), any());

        ReflectionTestUtils.setField(pluginBase, "encryptionAdapter", encryptionAdapter);
        ReflectionTestUtils.setField(irAdapterService, "encryptionAdapter", encryptionAdapter);
    }

    @SneakyThrows
    @Test
    public void testMCPTestAdapter() {
        String llmJson = TestUtil.getStringFromFile("classpath:flow_dsl/test_agent_parallel_flow_dsl.json");
        Map<String, Object> metadata = new HashMap<>();
        WorkflowVO workflowVO = JSON.parseObject(llmJson, WorkflowVO.class);
        Map<String, Object> irConfig = irAdapterService.adaptWorkflow(workflowVO, metadata);
        Assertions.assertEquals(TestUtil.getStringFromFile("classpath:flow_dsl/test_agent_parallel_flow_ir.json"),
            JSON.toJSONString(irConfig, JSONWriter.Feature.WriteMapNullValue));
    }

    @SneakyThrows
    @Test
    public void testQaNodeAdapter() {
        // set up
        String dslJsonStr = TestUtil.getStringFromFile("classpath:flow_dsl/test_workflow_qa_dsl.json");
        WorkflowVO workflowVO = JSON.parseObject(dslJsonStr, WorkflowVO.class);

        // run test
        Map<String, Object> irConfig = irAdapterService.adaptWorkflow(workflowVO, new HashMap<>());

        String expected = TestUtil.getStringFromFile("classpath:flow_dsl/test_workflow_qa_ir.json");
        String actual =
            JSON.toJSONString(irConfig, JSONWriter.Feature.WriteMapNullValue, JSONWriter.Feature.SortMapEntriesByKeys);
        // assert result
        Assertions.assertEquals(expected, actual);
    }

    @SneakyThrows
    @Test
    public void testParamExtractionAdapter() {
        String dslJsonStr = TestUtil.getStringFromFile("classpath:flow_dsl/test_param_extraction_flow_dsl.json");
        WorkflowVO workflowVO = JSON.parseObject(dslJsonStr, WorkflowVO.class);
        Map<String, Object> irConfig = irAdapterService.adaptWorkflow(workflowVO, new HashMap<>());

        String expected = TestUtil.getStringFromFile("classpath:flow_dsl/test_param_extraction_flow_ir.json");
        String actual =
            JSON.toJSONString(irConfig, JSONWriter.Feature.WriteMapNullValue, JSONWriter.Feature.SortMapEntriesByKeys);
        workflowManagementService.updateRefResources(workflowVO, null);
        Assertions.assertTrue(actual.length() >= expected.length());
    }

    @SneakyThrows
    @Test
    @Sql(scripts = {"classpath:sql/memory_repo_setup_db.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void testMemoryConfigAdapter() {
        String llmJson = TestUtil.getStringFromFile("classpath:flow_dsl/test_memory_config_flow_dsl.json");
        Map<String, Object> metadata = new HashMap<>();
        WorkflowVO workflowVO = JSON.parseObject(llmJson, WorkflowVO.class);
        Map<String, Object> irConfig = irAdapterService.adaptWorkflow(workflowVO, metadata);

        Object configsObj = irConfig.get("configs");
        assertNotNull(configsObj);
        assertTrue((configsObj instanceof HashMap<?, ?>));

        @SuppressWarnings("rawtypes") Map configMap = (Map) configsObj;
        Object memoryConfigObj = configMap.get("memory");
        assertNotNull(memoryConfigObj);
        assertTrue((memoryConfigObj instanceof MemoryConfigIR));
        MemoryConfigIR memoryConfigIR = (MemoryConfigIR) memoryConfigObj;
        Assertions.assertEquals("test_memory_repo_id", memoryConfigIR.getMemoryRepoId());
    }
}
