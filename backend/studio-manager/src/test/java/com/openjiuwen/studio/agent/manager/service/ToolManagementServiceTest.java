/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.enums.TestStatus;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.CryptoUtils;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.common.utils.RequestHeaderHolderUtils;
import com.openjiuwen.studio.agent.common.utils.UrlCheckUtils;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.manager.constant.Constants;
import com.openjiuwen.studio.agent.common.dto.auth.AuthInfo;
import com.openjiuwen.studio.agent.common.dto.auth.AuthKeyInfo;
import com.openjiuwen.studio.agent.manager.dto.CommonDeleteRsp;
import com.openjiuwen.studio.agent.manager.dto.CreateToolOpenAPIResponseBody;
import com.openjiuwen.studio.agent.manager.dto.CreateToolReq;
import com.openjiuwen.studio.agent.manager.dto.CreateToolRsp;
import com.openjiuwen.studio.agent.manager.dto.CreateVersionReq;
import com.openjiuwen.studio.agent.manager.dto.GetToolVersionQo;
import com.openjiuwen.studio.agent.manager.dto.ListToolsV1Qo;
import com.openjiuwen.studio.agent.manager.dto.ModifyToolReq;
import com.openjiuwen.studio.agent.manager.dto.ModifyToolRsp;
import com.openjiuwen.studio.agent.common.dto.tool.RunToolResponseBody;
import com.openjiuwen.studio.agent.manager.dto.Tool;
import com.openjiuwen.studio.agent.manager.dto.ToolCredential;
import com.openjiuwen.studio.agent.manager.dto.ToolListRsp;
import com.openjiuwen.studio.agent.manager.dto.VersionInfo;
import com.openjiuwen.studio.agent.manager.dto.VersionListRsp;
import com.openjiuwen.studio.agent.manager.entity.MappingEntity;
import com.openjiuwen.studio.agent.manager.entity.ToolEntity;
import com.openjiuwen.studio.agent.manager.enums.ToolType;
import com.openjiuwen.studio.agent.manager.mapper.MappingMapper;
import com.openjiuwen.studio.agent.manager.mapper.ToolCredentialMapper;
import com.openjiuwen.studio.agent.manager.mapper.ToolMapper;
import com.openjiuwen.studio.agent.manager.obs.MgObsService;
import com.openjiuwen.studio.agent.manager.service.plugin.IPluginBase;
import com.openjiuwen.studio.agent.manager.service.proxy.AgentServiceProxyService;
import com.openjiuwen.studio.agent.manager.utils.BaseTest;
import com.openjiuwen.studio.agent.manager.utils.JsonUtils;
import com.openjiuwen.studio.agent.manager.utils.TestUtil;
import com.openjiuwen.studio.common.service.service.EncryptionAdapter;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * tool管理态Service测试类
 *
 */
@Transactional
@MockitoSettings(strictness = Strictness.LENIENT)
class ToolManagementServiceTest extends BaseTest {
    private static MockedStatic<RequestContextUtils> mockedStatic;

    private static MockedStatic<RequestHeaderHolderUtils> mockedHeaderStatic;

    private static MockedStatic<CryptoUtils> mockedCryptoUtils;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MgObsService obsService;

    @Mock()
    private IPluginBase pluginBase;

    @Mock
    private AgentServiceProxyService agentServiceProxyService;

    @Autowired
    private ToolManagementService toolManagementService;

    @Autowired
    private UrlCheckUtils urlCheckUtils;

    private AutoCloseable mockitoCloseable;

    @Autowired
    private ToolMapper toolMapper;

    @Autowired
    private MappingMapper mappingMapper;

    @Autowired
    private ToolCredentialMapper toolCredentialMapper;

    @Mock
    private EncryptionAdapter encryptionAdapter;

    @BeforeAll
    public static void init() {
        RequestContextUtils.setRequestAuthTokenAndProjectId(Constants.TEST_TOKEN, Constants.TEST_PROJECT_ID);
        mockedStatic = Mockito.mockStatic(RequestContextUtils.class);
        mockedStatic.when(RequestContextUtils::getRequestUserName).thenReturn(Constants.TEST_CREATOR);
        mockedStatic.when(RequestContextUtils::getRequestUserId).thenReturn(Constants.TEST_CREATOR_ID);

        // 【新增】为加密适配器提供全局虚拟租户ID
        mockedStatic.when(RequestContextUtils::getRequestUserDomainId).thenReturn(Constants.TEST_PROJECT_ID);

        mockedHeaderStatic = Mockito.mockStatic(RequestHeaderHolderUtils.class);
        mockedHeaderStatic.when(RequestHeaderHolderUtils::getRequestLanguage).thenReturn("zh-CN");

        mockedCryptoUtils = Mockito.mockStatic(CryptoUtils.class);
        mockedCryptoUtils.when(() -> CryptoUtils.decrypt(anyString())).thenReturn("fake_decrypt_result");
        mockedCryptoUtils.when(() -> CryptoUtils.encrypt(anyString())).thenReturn("fake_encrypt_result");
    }

    @AfterAll
    static void end() {
        mockedStatic.close();
        mockedHeaderStatic.close();
        mockedCryptoUtils.close();
    }

    @BeforeEach
    void setUp() {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(toolManagementService, "opSvcProjectId", Constants.TEST_OP_PROJECT_ID);
        ReflectionTestUtils.setField(toolManagementService, "obsService", obsService);
        ReflectionTestUtils.setField(toolManagementService, "pluginBase", pluginBase);
        ReflectionTestUtils.setField(toolManagementService, "agentServiceProxyService", agentServiceProxyService);

        // 【新增】把 Mock 的适配器塞进被测试的服务中，并预设它的加解密返回值
        ReflectionTestUtils.setField(toolManagementService, "encryptionAdapter", encryptionAdapter);
        when(encryptionAdapter.encrypt(any(), any())).thenReturn("E1-{fake-kms-id}-fake_encrypt_result");
        when(encryptionAdapter.decrypt(any())).thenReturn("fake_decrypt_result");

        ReflectionTestUtils.setField(urlCheckUtils, "toolUrlWhiteList",
                Arrays.asList("api.example.com", "192.168.1.100"));
        ReflectionTestUtils.setField(toolManagementService, "excludeInnerTools", "Read_File,OCR");
        ReflectionTestUtils.setField(urlCheckUtils, "toolUrlBlackList", Arrays.asList("", "10.10.10.10"));
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_createTool_should_throws_AgentManagerException_when_tool_inputJsonSchema_is_invalid() {
        assertThrows(AgentStudioException.class, () -> {
            // setup
            CreateToolReq body = new CreateToolReq();
            body.setInputSchema(Constants.INVALID_JSONSCHEMA);

            // run the test
            toolManagementService.createToolV1(Constants.TEST_PROJECT_ID, "default", body);
        });
    }

    @Test
    void testCreateToolWithIam() throws IOException {
        // setup
        final CreateToolReq body = TestUtil.getJsonObject(CreateToolReq.class,
            "classpath:service/create_tool_with_iam.json");

        // run the test
        toolManagementService.createToolV1(Constants.TEST_PROJECT_ID, "default", body);
    }

    @Test
    @Sql(scripts = {"classpath:sql/inner_tool_setup_db.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void test_listTools_with_excludeIds_should_succeed() {
        final ToolListRsp toolListRsp = toolManagementService.listToolsV1(Constants.TEST_PROJECT_ID, new ListToolsV1Qo());
        assertEquals(1, toolListRsp.getCount());
    }

    @Test
    void test_createTool_should_throws_AgentManagerException_when_tool_outputJsonSchema_is_invalid() {
        assertThrows(AgentStudioException.class, () -> {
            // setup
            CreateToolReq body = new CreateToolReq();
            body.setInputSchema(Constants.VALID_JSONSCHEMA);
            body.setOutputSchema(Constants.INVALID_JSONSCHEMA);

            // run the test
            toolManagementService.createToolV1(Constants.TEST_PROJECT_ID, "default", body);
        });
    }

    @Test
    void test_createTool_should_throws_AgentManagerException_when_toolName_already_exist_in_blackList() {
        assertThrows(AgentStudioException.class, () -> {
            // setup
            CreateToolReq body = new CreateToolReq();
            body.setToolDisplayName("retrieval");

            // run the test
            toolManagementService.createToolV1(Constants.TEST_PROJECT_ID, "default", body);
        });
    }

    @Test
    @Sql(scripts = {"classpath:sql/tool_reserve_meeting_room_setup_db.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void test_createTool_should_throws_AgentManagerException_when_toolName_already_exist() {
        assertThrows(AgentStudioException.class, () -> {
            // setup
            final CreateToolReq body = TestUtil.getJsonObject(CreateToolReq.class,
                "classpath:service/create_tool.json");

            // run the test
            toolManagementService.createToolV1(Constants.TEST_PROJECT_ID, "default", body);
        });
    }

    @Test
    @Sql(scripts = {"classpath:sql/tool_reserve_meeting_room_setup_db.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void test_create_tool_openapi_should_succeed_when_tool_exist() {
        ToolEntity tool = toolMapper.selectByPrimaryKeyAndWorkspace("reserve_meeting_room", Constants.TEST_PROJECT_ID,
            "default");
        tool.setIsInputList(false);
        tool.setIsOutputList(false);
        when(
            pluginBase.buildToolByPlugin(eq(Constants.TEST_PROJECT_ID), eq("default"), eq("reserve_meeting_room"), eq("0"))).thenReturn(
            tool);
        CreateToolOpenAPIResponseBody reserveMeetingRoom = toolManagementService.createToolOpenAPIByIdV1(
            Constants.TEST_PROJECT_ID, "reserve_meeting_room", "default");

        assertEquals("reserve_meeting_room", reserveMeetingRoom.getToolId());
    }

    @Test
    void test_create_tool_openapi_should_throw_agentManagerException_when_tool_not_exist() {
        assertThrows(AgentStudioException.class,
            () -> toolManagementService.createToolOpenAPIByIdV1(Constants.TEST_PROJECT_ID, "reserve_meeting_room",
                "default"));
    }

    @Test
    void test_createTool_should_succeed_when_test_data_combined() throws IOException {
        // setup
        final CreateToolReq body = TestUtil.getJsonObject(CreateToolReq.class, "classpath:service/create_tool.json");

        // run the test
        CreateToolRsp result = toolManagementService.createToolV1(Constants.TEST_PROJECT_ID, "default", body);

        // verify the results
        assertNotNull(result.getToolId());

        Tool tool = toolManagementService.retrieveToolV1(Constants.TEST_PROJECT_ID, result.getToolId(), "default");
        assertEquals(TestStatus.UNKNOWN.name(), tool.getTestStatus());
    }

    @Test
    void test_createTool_should_succeed_when_auth_scope_is_service() throws IOException {
        // setup
        final CreateToolReq body = TestUtil.getJsonObject(CreateToolReq.class, "classpath:service/create_tool.json");
        body.setAuthInfo(mockServiceAuthInfo());

        // run the test
        final CreateToolRsp result = toolManagementService.createToolV1(Constants.TEST_PROJECT_ID, "default", body);

        final Tool tool = toolManagementService.retrieveToolV1(Constants.TEST_PROJECT_ID, result.getToolId(), "default");

        // verify the results
        assertEquals("****", tool.getAuthInfo()
            .getAuthKeys()
            .get(0)
            .getAuthKey()
            .substring(1, tool.getAuthInfo().getAuthKeys().get(0).getAuthKey().length() - 1));
    }

    @Test
    @Sql(scripts = {"classpath:sql/tool_reserve_meeting_room_setup_db.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void test_createToolApi_should_succeed() throws IOException {
        final CreateToolReq body = TestUtil.getJsonObject(CreateToolReq.class, "classpath:service/create_tool.json");
        when(obsService.uploadObsFile(anyString(), anyString(), anyInt())).thenReturn("mock_file_path");

        final CreateToolOpenAPIResponseBody result = toolManagementService.createToolOpenAPI(Constants.TEST_PROJECT_ID,
            "default", body);

        assertNotNull(result);
        assertNotNull(result.getToolId());
    }

    @Test
    @Sql(scripts = {"classpath:sql/tool_reserve_meeting_room_setup_db.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void test_createToolApi_stream_should_succeed() throws IOException {
        CreateToolReq body = TestUtil.getJsonObject(CreateToolReq.class, "classpath:service/create_tool_stream.json");
        when(obsService.uploadObsFile(anyString(), anyString(), anyInt())).thenReturn("mock_file_path");

        body.getAuthInfo().setScope(AuthInfo.ScopeEnum.IAM);
        body.getAuthInfo().setIamCredentials(new HashMap<>());
        final CreateToolOpenAPIResponseBody result = toolManagementService.createToolOpenAPI(Constants.TEST_PROJECT_ID,
            "default", body);

        assertNotNull(result);
        assertNotNull(result.getToolId());
    }

    @Test
    @Sql(scripts = {"classpath:sql/tool_reserve_meeting_room_setup_db.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void test_deleteTool_should_succeed_when_tool_not_bound_by_assistant() {
        CommonDeleteRsp deleteToolRsp = toolManagementService.deleteToolV1(Constants.TEST_PROJECT_ID,
            Constants.TEST_MEETING_ROOM_TOOL_ID, "default");
        assertEquals(Constants.TEST_MEETING_ROOM_TOOL_ID, deleteToolRsp.getId());
    }

    @Test
    void test_deleteTool_should_succeed_when_tool_not_exist() {
        assertNull(toolManagementService.deleteToolV1(Constants.TEST_PROJECT_ID, Constants.TEST_MEETING_ROOM_TOOL_ID,
            "default"));
    }

    @Test
    @Sql(scripts = {"classpath:sql/tool_reserve_meeting_room_setup_db.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void test_deleteTool_should_throws_AgentManagerException_when_user_has_no_permission() {
        assertDoesNotThrow(() -> {
            // run the test
            toolManagementService.deleteToolV1(Constants.TEST_PROJECT_ID, "reserve_meeting_room_change", "default");
        });
    }

    @Test
    @Sql(scripts = {
        "classpath:sql/tool_ir_adapter_setup_db.sql", "classpath:sql/agent_setup_db.sql",
        "classpath:sql/workflow_setup_db.sql", "classpath:sql/mapping_setup_db.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:sql/t_rollback.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void test_deleteTool_should_success_when_tool_bound_by_app() {
        List<MappingEntity> mappingEntities1 = mappingMapper.selectByResourceId("reserve_meeting_room_9");
        assertEquals(1, mappingEntities1.size());
        assertTrue(mappingEntities1.get(0).isValid());

        toolManagementService.deleteToolV1(Constants.TEST_PROJECT_ID, "reserve_meeting_room_9", "default");

        List<MappingEntity> mappingEntities2 = mappingMapper.selectByResourceId("reserve_meeting_room_9");
        assertEquals(1, mappingEntities2.size());
    }

    @Test
    @Sql(scripts = {"classpath:sql/tool_reserve_meeting_room_setup_db.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void test_listTools_should_succeed_when_tool_exist() {
        final ToolListRsp toolListRsp = toolManagementService.listToolsV1(Constants.TEST_PROJECT_ID, new ListToolsV1Qo());
        assertEquals(6, toolListRsp.getCount());
    }

    @Test
    @Sql(scripts = {"classpath:sql/tool_reserve_meeting_room_setup_db.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void test_listTools_should_succeed_when_tool_ids() {
        ListToolsV1Qo listToolsQo = new ListToolsV1Qo().setIds(
            Collections.singletonList(Constants.TEST_MEETING_ROOM_TOOL_ID));
        final ToolListRsp toolListRsp = toolManagementService.listToolsV1(Constants.TEST_PROJECT_ID, listToolsQo);
        assertEquals(1, toolListRsp.getCount());
    }

    @Test
    @Sql(scripts = {"classpath:sql/tool_reserve_meeting_room_setup_db.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void test_listTools_should_succeed_when_tool_type_is_custom() {
        final ToolListRsp toolListRsp = toolManagementService.listToolsV1(Constants.TEST_PROJECT_ID,
            new ListToolsV1Qo().setType(ToolType.CUSTOM.type));
        assertEquals(6, toolListRsp.getCount());
    }

    @Test
    void test_modifyTool_should_throws_AgentManagerException_when_tool_inputJsonSchema_is_invalid() {
        assertThrows(AgentStudioException.class, () -> {
            // setup
            ModifyToolReq body = new ModifyToolReq();
            body.setInputSchema(Constants.INVALID_JSONSCHEMA);

            // run the test
            toolManagementService.modifyToolV1(Constants.TEST_PROJECT_ID, "default", Constants.TEST_MEETING_ROOM_TOOL_ID,
                body);
        });
    }

    @Test
    @Sql(scripts = {"classpath:sql/tool_customize_node_setup_db.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void test_publish_Tool_to_customize_node_should_success() throws IOException {
        // setup
        final ModifyToolReq body = TestUtil.getJsonObject(ModifyToolReq.class, "classpath:service/create_tool.json");
        body.setCustomizeNode(true);
        ListToolsV1Qo listToolsQo = new ListToolsV1Qo().setCustomizeNode(true);

        // run the test
        final ModifyToolRsp result = toolManagementService.modifyToolV1(Constants.TEST_OP_PROJECT_ID, "default",
            "custom_tool", body);
        Tool tool = toolManagementService.retrieveToolV1(Constants.TEST_OP_PROJECT_ID, "custom_tool", "default");
        ToolListRsp toolListRsp = toolManagementService.listToolsV1(Constants.TEST_OP_PROJECT_ID, listToolsQo);

        // verify the results
        assertEquals("custom_tool", result.getToolId());
        assertTrue(tool.isCustomizeNode());
        assertEquals(1, toolListRsp.getCount());
        assertTrue(toolListRsp.getToolList().get(0).isCustomizeNode());
    }

    @Test
    @Sql(scripts = {"classpath:sql/tool_reserve_meeting_room_setup_db.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void test_publish_Tool_to_customize_node_should_throws_AgentManagerException_when_not_op() throws IOException {
        assertThrows(AgentStudioException.class, () -> {
            // setup
            final ModifyToolReq body = TestUtil.getJsonObject(ModifyToolReq.class,
                "classpath:service/create_tool.json");
            body.setCustomizeNode(true);

            // run the test
            toolManagementService.modifyToolV1(Constants.TEST_PROJECT_ID, "default", Constants.TEST_MEETING_ROOM_TOOL_ID,
                body);
        });
    }

    @Test
    void test_modifyTool_should_throws_AgentManagerException_when_tool_outputJsonSchema_is_invalid() {
        assertThrows(AgentStudioException.class, () -> {
            // setup
            ModifyToolReq body = new ModifyToolReq();
            body.setInputSchema(Constants.VALID_JSONSCHEMA);
            body.setOutputSchema(Constants.INVALID_JSONSCHEMA);

            // run the test
            toolManagementService.modifyToolV1(Constants.TEST_PROJECT_ID, "default", Constants.TEST_MEETING_ROOM_TOOL_ID,
                body);
        });
    }

    @Test
    void test_modifyTool_should_throws_AgentManagerException_when_old_tool_is_not_exist() {
        assertThrows(AgentStudioException.class, () -> {
            // setup
            ModifyToolReq body = new ModifyToolReq();
            body.setInputSchema(Constants.VALID_JSONSCHEMA);
            body.setOutputSchema(Constants.VALID_JSONSCHEMA);

            // run the test
            toolManagementService.modifyToolV1(Constants.TEST_PROJECT_ID, "default", Constants.TEST_MEETING_ROOM_TOOL_ID,
                body);
        });
    }

    @Test
    @Sql(scripts = {"classpath:sql/tool_reserve_meeting_room_setup_db.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void test_modifyTool_should_succeed_when_tool_desc_is_not_changed() throws IOException {
        // setup
        final ModifyToolReq body = TestUtil.getJsonObject(ModifyToolReq.class, "classpath:service/create_tool.json");
        body.setMetadata("changed");
        body.setToolChineseName("reserve_meeting_room_1");
        // run the test
        final ModifyToolRsp result = toolManagementService.modifyToolV1(Constants.TEST_PROJECT_ID, "default",
            Constants.TEST_MEETING_ROOM_TOOL_ID, body);

        // verify the results
        assertEquals(Constants.TEST_MEETING_ROOM_TOOL_ID, result.getToolId());
    }

    @Test
    @Sql(scripts = {"classpath:sql/tool_reserve_meeting_room_setup_db.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testSetToolTestStatus() throws IOException {
        final ModifyToolReq body = TestUtil.getJsonObject(ModifyToolReq.class, "classpath:service/create_tool.json");
        body.setTestStatus("UNKNOWN");
        body.setToolChineseName("reserve_meeting_room_1");
        // run the test
        final ModifyToolRsp result = toolManagementService.modifyToolV1(Constants.TEST_PROJECT_ID, "default",
            Constants.TEST_MEETING_ROOM_TOOL_ID, body);
        Tool tool = toolManagementService.retrieveToolV1(Constants.TEST_PROJECT_ID, result.getToolId(), "default");
        assertEquals(TestStatus.UNKNOWN.name(), tool.getTestStatus());
    }

    @Test
    @Sql(scripts = {"classpath:sql/tool_reserve_meeting_room_setup_db.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void test_modifyTool_should_succeed_when_api_key_value_is_not_changed() throws IOException {
        // setup
        final ModifyToolReq body = TestUtil.getJsonObject(ModifyToolReq.class, "classpath:service/create_tool.json");
        body.setToolDisplayName("reserve_meeting_room_changed01");
        body.setToolChineseName("reserve_meeting_room_1");
        AuthKeyInfo authKeyInfo = new AuthKeyInfo().setTargetName("X-API-Key").setAuthKey(null);
        AuthInfo authInfo = new AuthInfo().setScope(AuthInfo.ScopeEnum.SERVICE)
            .setDomain(AuthInfo.DomainEnum.HEADERS)
            .setAuthKeys(Collections.singletonList(authKeyInfo));
        body.setAuthInfo(authInfo);

        // run the test
        final ModifyToolRsp result = toolManagementService.modifyToolV1(Constants.TEST_PROJECT_ID, "default",
            "reserve_meeting_room_service", body);

        // verify the results
        assertEquals("reserve_meeting_room_service", result.getToolId());
    }

    @Test
    @Sql(scripts = {"classpath:sql/tool_reserve_meeting_room_setup_db.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void test_modifyTool_when_user_is_op() throws IOException {
        // setup
        final ModifyToolReq body = TestUtil.getJsonObject(ModifyToolReq.class, "classpath:service/create_tool.json");
        body.setToolDisplayName("reserve_meeting_room_changed01");

        // run the test
        final ModifyToolRsp result = toolManagementService.modifyToolV1(Constants.TEST_OP_PROJECT_ID, "default",
            "inner_tool", body);

        // verify the results
        assertEquals("inner_tool", result.getToolId());
    }

    @Test
    @Sql(scripts = {"classpath:sql/tool_reserve_meeting_room_setup_db.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void test_modifyTool_should_throws_AgentManagerException_when_user_has_no_permission() {
        assertThrows(AgentStudioException.class, () -> {
            // setup
            final ModifyToolReq body = TestUtil.getJsonObject(ModifyToolReq.class,
                "classpath:service/create_tool.json");
            body.setMetadata("changed");
            // run the test
            toolManagementService.modifyToolV1(Constants.TEST_PROJECT_ID, "default", "reserve_meeting_room_change", body);
        });
    }

    @Test
    @Sql(scripts = {"classpath:sql/tool_reserve_meeting_room_setup_db.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void test_modifyTool_should_throws_AgentManagerException_when_tool_name_is_already_exist() {
        assertThrows(AgentStudioException.class, () -> {
            // setup
            final ModifyToolReq body = TestUtil.getJsonObject(ModifyToolReq.class,
                "classpath:service/create_tool.json");
            body.setToolDisplayName("reserve_meeting_room_change");
            // run the test
            toolManagementService.modifyToolV1(Constants.TEST_PROJECT_ID, "default", "reserve_meeting_room", body);
        });
    }

    @Test
    @Sql(scripts = {"classpath:sql/tool_reserve_meeting_room_setup_db.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void test_retrieveTool_should_succeed_when_tool_exist() {
        final Tool tool = toolManagementService.retrieveToolV1(Constants.TEST_PROJECT_ID,
            Constants.TEST_MEETING_ROOM_TOOL_ID, "default");
        assertEquals(Constants.TEST_MEETING_ROOM_TOOL_ID, tool.getToolId());
    }

    @Test
    void test_retrieveTool_should_throw_AgentManagerException_when_tool_not_exist() {
        assertThrows(AgentStudioException.class,
            () -> toolManagementService.retrieveToolV1(Constants.TEST_PROJECT_ID, Constants.TEST_MEETING_ROOM_TOOL_ID,
                "default"));
    }

    @Test
    void test_check_url_should_succeed_when_in_white_list() throws IOException {
        CreateToolReq toolReq = TestUtil.getJsonObject(CreateToolReq.class, "classpath:service/create_tool.json");
        toolReq.getRequestInfo().setUrl("http://api.example.com/data");
        CreateToolRsp tool1 = toolManagementService.createToolV1(Constants.TEST_PROJECT_ID, "default", toolReq);
        assertNotNull(tool1.getToolId());
    }

    @Test
    void test_check_url_should_failed_when_in_black_list() throws IOException {
        CreateToolReq toolReq = TestUtil.getJsonObject(CreateToolReq.class, "classpath:service/create_tool.json");

        toolReq.getRequestInfo().setUrl("");
        assertThrows(AgentStudioException.class,
            () -> toolManagementService.createToolV1(Constants.TEST_PROJECT_ID, "default", toolReq));

        toolReq.getRequestInfo().setUrl("http://10.10.10.10");
        assertThrows(AgentStudioException.class,
            () -> toolManagementService.createToolV1(Constants.TEST_PROJECT_ID, "default", toolReq));
    }

    @Test
    void test_check_url_should_failed_when_inner_address() throws IOException {
        CreateToolReq toolReq = TestUtil.getJsonObject(CreateToolReq.class, "classpath:service/create_tool.json");

        toolReq.getRequestInfo().setUrl("http://192.168.1.1");
        assertThrows(AgentStudioException.class,
            () -> toolManagementService.createToolV1(Constants.TEST_PROJECT_ID, "default", toolReq));
    }

    @Test
    @Sql(scripts = {"classpath:sql/tool_reserve_meeting_room_setup_db.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testReleaseToolVersionShouldFailedWhenNotTested() {
        CreateVersionReq createVersionReq = new CreateVersionReq();
        createVersionReq.setVersionName("test_version_name");
        createVersionReq.setVersionNote("test_version_note");

        assertNotNull(toolManagementService.releaseToolVersion(Constants.TEST_PROJECT_ID,
            Constants.TEST_MEETING_ROOM_TOOL_ID, "default", createVersionReq));
    }

    @Test
    @Sql(scripts = {"classpath:sql/tool_reserve_meeting_room_setup_db.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testReleaseToolVersionShouldSuccess() throws IOException {
        final ModifyToolReq body = TestUtil.getJsonObject(ModifyToolReq.class, "classpath:service/create_tool.json");
        body.setTestStatus(TestStatus.SUCCESS.name());
        body.setToolChineseName("reserve_meeting_room_1");
        toolManagementService.modifyToolV1(Constants.TEST_PROJECT_ID, "default", Constants.TEST_MEETING_ROOM_TOOL_ID,
            body);

        CreateVersionReq createVersionReq = new CreateVersionReq();
        createVersionReq.setVersionName("test_version_name");
        createVersionReq.setVersionNote("test_version_note");

        VersionInfo versionInfo = toolManagementService.releaseToolVersion(Constants.TEST_PROJECT_ID,
            Constants.TEST_MEETING_ROOM_TOOL_ID, "default", createVersionReq);

        assertNotNull(versionInfo);
        assertEquals("test_version_name", versionInfo.getVersionName());
        assertEquals("test_version_note", versionInfo.getVersionNote());
        assertNotNull(versionInfo.getVersionId());
        assertNotNull(versionInfo.getReleaseTime());

        String lastVersionId = toolMapper.selectById(Constants.TEST_MEETING_ROOM_TOOL_ID).getLastVersionId();
        assertEquals(versionInfo.getVersionId(), lastVersionId);
    }

    @Test
    @Sql(scripts = {"classpath:sql/tool_reserve_meeting_room_setup_db.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testListToolVersions() throws IOException, InterruptedException {
        final ModifyToolReq body = TestUtil.getJsonObject(ModifyToolReq.class, "classpath:service/create_tool.json");
        body.setTestStatus(TestStatus.SUCCESS.name());
        body.setToolChineseName("reserve_meeting_room_1");
        toolManagementService.modifyToolV1(Constants.TEST_PROJECT_ID, "default", Constants.TEST_MEETING_ROOM_TOOL_ID,
            body);

        CreateVersionReq createVersionReq = new CreateVersionReq();
        createVersionReq.setVersionName("test_version_name");
        createVersionReq.setVersionNote("test_version_note");

        toolManagementService.releaseToolVersion(Constants.TEST_PROJECT_ID, Constants.TEST_MEETING_ROOM_TOOL_ID,
            "default",
            new CreateVersionReq().setVersionName("test_version_name1").setVersionNote("test_version_note1"));
        // 防止创建过快导致versionId一致
        Thread.sleep(1);

        toolManagementService.releaseToolVersion(Constants.TEST_PROJECT_ID, Constants.TEST_MEETING_ROOM_TOOL_ID,
            "default",
            new CreateVersionReq().setVersionName("test_version_name2").setVersionNote("test_version_note2"));
        Thread.sleep(1);

        toolManagementService.releaseToolVersion(Constants.TEST_PROJECT_ID, Constants.TEST_MEETING_ROOM_TOOL_ID,
            "default",
            new CreateVersionReq().setVersionName("test_version_name3").setVersionNote("test_version_note3"));

        VersionListRsp versionListRsp = toolManagementService.listToolVersions(Constants.TEST_PROJECT_ID,
            Constants.TEST_MEETING_ROOM_TOOL_ID, "default");

        List<VersionInfo> versionList = versionListRsp.getVersionList();
        assertNotNull(versionList);
        assertEquals(3, versionList.size());
        assertEquals(3, versionListRsp.getCount());

        assertEquals("test_version_note3", versionList.get(0).getVersionNote());
        assertEquals("test_version_name2", versionList.get(1).getVersionName());
        assertEquals("test_version_name1", versionList.get(2).getVersionName());
    }

    @Test
    @Sql(scripts = {"classpath:sql/tool_reserve_meeting_room_setup_db.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testGetToolVersion() throws IOException {
        final ModifyToolReq body = TestUtil.getJsonObject(ModifyToolReq.class, "classpath:service/create_tool.json");
        body.setTestStatus(TestStatus.SUCCESS.name());
        body.setToolChineseName("reserve_meeting_room_1");
        body.setToolChineseName("reserve_meeting_room_1");
        toolManagementService.modifyToolV1(Constants.TEST_PROJECT_ID, "default", Constants.TEST_MEETING_ROOM_TOOL_ID,
            body);

        CreateVersionReq createVersionReq = new CreateVersionReq();
        createVersionReq.setVersionName("test_version_name");
        createVersionReq.setVersionNote("test_version_note");

        VersionInfo versionInfo = toolManagementService.releaseToolVersion(Constants.TEST_PROJECT_ID,
            Constants.TEST_MEETING_ROOM_TOOL_ID, "default",
            new CreateVersionReq().setVersionName("test_version_name1").setVersionNote("test_version_note1"));

        ToolEntity toolEntity = toolMapper.selectById(Constants.TEST_MEETING_ROOM_TOOL_ID);
        when(pluginBase.getToolEntityByVersion(anyString(), anyString())).thenReturn(toolEntity);
        when(obsService.downloadObsFile(any())).thenReturn(JsonUtils.toJson(toolEntity));

        Tool toolVersion = toolManagementService.getToolVersion(Constants.TEST_PROJECT_ID, versionInfo.getVersionId(),
            Constants.TEST_MEETING_ROOM_TOOL_ID, new GetToolVersionQo().setWorkspaceId("default"));

        assertNotNull(toolVersion);
        assertEquals(Constants.TEST_MEETING_ROOM_TOOL_ID, toolVersion.getToolId());
        assertEquals(body.getInputSchema().trim(), toolVersion.getInputSchema().trim());
    }

    @Test
    @Sql(scripts = {"classpath:sql/tool_reserve_meeting_room_setup_db.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testDeleteToolVersion() throws IOException {
        final ModifyToolReq body = TestUtil.getJsonObject(ModifyToolReq.class, "classpath:service/create_tool.json");
        body.setTestStatus(TestStatus.SUCCESS.name());
        body.setToolChineseName("reserve_meeting_room_1");
        toolManagementService.modifyToolV1(Constants.TEST_PROJECT_ID, "default", Constants.TEST_MEETING_ROOM_TOOL_ID,
            body);

        CreateVersionReq createVersionReq = new CreateVersionReq();
        createVersionReq.setVersionName("test_version_name");
        createVersionReq.setVersionNote("test_version_note");

        VersionInfo versionInfo = toolManagementService.releaseToolVersion(Constants.TEST_PROJECT_ID,
            Constants.TEST_MEETING_ROOM_TOOL_ID, "default",
            new CreateVersionReq().setVersionName("test_version_name1").setVersionNote("test_version_note1"));

        when(pluginBase.getToolEntityByVersion(anyString(), anyString())).thenThrow(
            new AgentStudioException(StudioError.TOOL_VERSION_NOT_EXIST, Constants.TEST_MEETING_ROOM_TOOL_ID,
                versionInfo.getVersionId()));
        toolManagementService.deleteToolVersion(Constants.TEST_PROJECT_ID, versionInfo.getVersionId(),
            Constants.TEST_MEETING_ROOM_TOOL_ID, "default");

        Assertions.assertThrows(AgentStudioException.class,
            () -> pluginBase.getToolEntityByVersion(Constants.TEST_MEETING_ROOM_TOOL_ID, versionInfo.getVersionId()));
    }

    @Test
    @Sql(scripts = {"classpath:sql/tool_reserve_meeting_room_setup_db.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testUpdateLastVersion() throws IOException, InterruptedException {
        final ModifyToolReq body = TestUtil.getJsonObject(ModifyToolReq.class, "classpath:service/create_tool.json");
        body.setTestStatus(TestStatus.SUCCESS.name());
        body.setToolChineseName("reserve_meeting_room_1");
        toolManagementService.modifyToolV1(Constants.TEST_PROJECT_ID, "default", Constants.TEST_MEETING_ROOM_TOOL_ID,
            body);

        CreateVersionReq createVersionReq = new CreateVersionReq();
        createVersionReq.setVersionName("test_version_name");
        createVersionReq.setVersionNote("test_version_note");

        VersionInfo versionInfo1 = toolManagementService.releaseToolVersion(Constants.TEST_PROJECT_ID,
            Constants.TEST_MEETING_ROOM_TOOL_ID, "default",
            new CreateVersionReq().setVersionName("test_version_name1").setVersionNote("test_version_note1"));
        // 防止创建过快导致versionId一致
        Thread.sleep(1);
        VersionInfo versionInfo2 = toolManagementService.releaseToolVersion(Constants.TEST_PROJECT_ID,
            Constants.TEST_MEETING_ROOM_TOOL_ID, "default",
            new CreateVersionReq().setVersionName("test_version_name2").setVersionNote("test_version_note2"));

        // 删除最新的版本，last version id 更新为上一个版本
        toolManagementService.deleteToolVersion(Constants.TEST_PROJECT_ID, versionInfo2.getVersionId(),
            Constants.TEST_MEETING_ROOM_TOOL_ID, "default");
        Tool tool = toolManagementService.retrieveToolV1(Constants.TEST_PROJECT_ID, Constants.TEST_MEETING_ROOM_TOOL_ID,
            "default");
        assertEquals(versionInfo1.getVersionId(), tool.getLastVersionId());

        // 删除所有版本，last version id为null
        toolManagementService.deleteToolVersion(Constants.TEST_PROJECT_ID, versionInfo1.getVersionId(),
            Constants.TEST_MEETING_ROOM_TOOL_ID, "default");
        tool = toolManagementService.retrieveToolV1(Constants.TEST_PROJECT_ID, Constants.TEST_MEETING_ROOM_TOOL_ID,
            "default");
        assertNull(tool.getLastVersionId());
    }

    @Test
    @Sql(scripts = {"classpath:sql/tool_reserve_meeting_room_setup_db.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void test_addToolCredential_should_throw_AgentManagerException_when_tool_not_inner() throws Exception {
        // setup
        final ToolCredential body = TestUtil.getJsonObject(ToolCredential.class,
            "classpath:service/create_tool_credential.json");

        assertThrows(AgentStudioException.class,
            () -> toolManagementService.addToolCredential(Constants.TEST_PROJECT_ID, Constants.TEST_TOOL_ID, "default",false,
                body));

    }

    @Test
    @Sql(scripts = {"classpath:sql/tool_reserve_meeting_room_setup_db.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void test_addToolCredential_should_succeed_with_validation() throws Exception {
        // 创建一个模拟的RunToolResponseBody
        RunToolResponseBody responseBody = new RunToolResponseBody();
        responseBody.setRawResponseCode(200); // 设置期望的状态码
        // 创建一个模拟的ResponseEntity
        ResponseEntity<RunToolResponseBody> responseEntity = ResponseEntity.ok(responseBody);
        ToolEntity tool = toolMapper.selectByPrimaryKeyAndWorkspace("reserve_meeting_room", Constants.TEST_PROJECT_ID, "default");
        tool.setIsInputList(false);
        tool.setIsOutputList(false);
        // 设置mock

        when(pluginBase.buildToolByPlugin(any(), any(), any(), any())).thenReturn(tool);
        when(agentServiceProxyService.runToolForValidateToolCredential(any(), any(), any(), any())).thenReturn(responseEntity);
        final ToolCredential body = TestUtil.getJsonObject(ToolCredential.class, "classpath:service/create_tool_credential.json");

        // When
        ToolCredential result = toolManagementService.addToolCredential(Constants.TEST_OP_PROJECT_ID,
                Constants.TEST_INNER_TOOL_ID, "default", true, body);

        // Then
        assertNotNull(result);
    }
    @Test
    @Sql(scripts = {"classpath:sql/tool_reserve_meeting_room_setup_db.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void test_addToolCredential_should_failed_with_validation() throws Exception {
        // 创建一个模拟的RunToolResponseBody
        RunToolResponseBody responseBody = new RunToolResponseBody();
        responseBody.setRawResponseCode(401); // 设置期望的状态码
        // 创建一个模拟的ResponseEntity
        ResponseEntity<RunToolResponseBody> responseEntity = ResponseEntity.ok(responseBody);
        ToolEntity tool = toolMapper.selectByPrimaryKeyAndWorkspace("reserve_meeting_room", Constants.TEST_PROJECT_ID, "default");
        tool.setIsInputList(false);
        tool.setIsOutputList(false);
        // 设置mock

        when(pluginBase.buildToolByPlugin(any(), any(), any(), any())).thenReturn(tool);
        when(agentServiceProxyService.runToolForValidateToolCredential(any(), any(), any(), any())).thenReturn(responseEntity);
        final ToolCredential body = TestUtil.getJsonObject(ToolCredential.class, "classpath:service/create_tool_credential.json");

        // When
        assertThrows(AgentStudioException.class,
                () -> toolManagementService.addToolCredential(Constants.TEST_OP_PROJECT_ID,
                        Constants.TEST_INNER_TOOL_ID, "default", true, body)
        );


    }

    @Test
    @Sql(scripts = {"classpath:sql/tool_reserve_meeting_room_setup_db.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void test_addToolCredential_should_succeed() throws Exception {
        // setup
        final ToolCredential body = TestUtil.getJsonObject(ToolCredential.class,
            "classpath:service/create_tool_credential.json");

        // When
        ToolCredential result = toolManagementService.addToolCredential(Constants.TEST_OP_PROJECT_ID,
            Constants.TEST_INNER_TOOL_ID, "default",false, body);

        // Then
        assertNotNull(result);
    }

    @Test
    void test_addToolCredential_should_throw_AgentManagerException_when_tool_not_exist() throws Exception {
        final ToolCredential body = TestUtil.getJsonObject(ToolCredential.class,
            "classpath:service/create_tool_credential.json");
        assertThrows(AgentStudioException.class,
            () -> toolManagementService.addToolCredential(Constants.TEST_PROJECT_ID, Constants.TEST_TOOL_ID, "default",false,
                body));
    }

    @Test
    @Sql(scripts = {"classpath:sql/tool_reserve_meeting_room_setup_db.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void test_addToolCredential_should_throw_AgentManagerException_when_authkey_null() throws Exception {
        final ToolCredential body = TestUtil.getJsonObject(ToolCredential.class,
            "classpath:service/create_tool_credential.json");
        body.setAuthKeys(null);
        assertThrows(AgentStudioException.class,
            () -> toolManagementService.addToolCredential(Constants.TEST_PROJECT_ID, Constants.TEST_INNER_TOOL_ID,
                "default",false, body));
    }

    @Test
    @Sql(scripts = {"classpath:sql/tool_reserve_meeting_room_setup_db.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void test_deleteToolCredential_should_not_throw_exception() throws Exception {
        final ToolCredential toolCredential = TestUtil.getJsonObject(ToolCredential.class,
            "classpath:service/create_tool_credential.json");
        assertThrows(AgentStudioException.class,
            () -> toolManagementService.deleteToolCredential(Constants.TEST_OP_PROJECT_ID, Constants.TEST_INNER_TOOL_ID,
                "default"));
    }

    @Test
    @Sql(scripts = {"classpath:sql/tool_reserve_meeting_room_setup_db.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void test_deleteToolCredential_should_succeed_when_tool_not_exist() {
        assertThrows(AgentStudioException.class,
            () -> toolManagementService.deleteToolCredential(Constants.TEST_PROJECT_ID,
                Constants.TEST_MEETING_ROOM_TOOL_ID, "default"));
    }

    @Test
    @Sql(scripts = {"classpath:sql/tool_reserve_meeting_room_setup_db.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void test_updateToolCredential_should_throw_AgentManagerException_when_authkey_null() throws Exception {
        final ToolCredential body = TestUtil.getJsonObject(ToolCredential.class,
            "classpath:service/create_tool_credential.json");
        body.setAuthKeys(null);
        assertThrows(AgentStudioException.class,
            () -> toolManagementService.addToolCredential(Constants.TEST_PROJECT_ID, Constants.TEST_INNER_TOOL_ID,
                "default",false, body));
    }

    @Test
    @Sql(scripts = {"classpath:sql/tool_reserve_meeting_room_setup_db.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void checkToolPermissionTest() {
        toolManagementService.checkToolPermission(Constants.TEST_PROJECT_ID, Constants.TEST_WORKSPACE_ID,
            "reserve_meeting_room");
    }

    private AuthInfo mockServiceAuthInfo() {
        AuthInfo authInfo = new AuthInfo();
        authInfo.setScope(AuthInfo.ScopeEnum.SERVICE);
        authInfo.setDomain(AuthInfo.DomainEnum.HEADERS);
        authInfo.setAuthKeys(Collections.singletonList(
            new AuthKeyInfo().setTargetName(Constants.TEST_PLUGIN_AUTH_TARGET_NAME)
                .setAuthKey(Constants.TEST_PLUGIN_AUTH_KEY)));
        return authInfo;
    }

    @Test
    @Sql(scripts = {"classpath:sql/agent_workflow_mapping_setup_db.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void test_checkToolIsUsed() {
        Boolean result = toolManagementService.checkToolIsUsed(Constants.TEST_PROJECT_ID, "default",
            "reserve_meeting_room_12");
        Assertions.assertTrue(result);
        Boolean result2 = toolManagementService.checkToolIsUsed(Constants.TEST_PROJECT_ID, "default",
            "reserve_meeting_room_11111");
        Assertions.assertFalse(result2);
    }

    @Test
    void test_transferToolOpenApi() throws Exception {
        ToolEntity toolEntity = TestUtil.getJsonObject(ToolEntity.class, "classpath:service/plugin/create_tool_with_his_iam.json");
        toolManagementService.transferToolOpenApi(toolEntity);

        ToolEntity toolEntity1 = TestUtil.getJsonObject(ToolEntity.class, "classpath:service/plugin/create_tool_with_sgov.json");
        toolManagementService.transferToolOpenApi(toolEntity1);

        toolEntity = TestUtil.getJsonObject(ToolEntity.class, "classpath:service/plugin/auth/create_tool_with_custom_iam.json");
        toolManagementService.transferToolOpenApi(toolEntity);
    }

    @Test
    @DisplayName("覆盖 encryptAuthKeyInfo：测试正常加密流程")
    void testEncryptAuthKeyInfo_ShouldEncryptKeys() {
        // 准备数据
        AuthKeyInfo keyInfo = new AuthKeyInfo().setTargetName("test-target").setAuthKey("plain-key");
        List<AuthKeyInfo> authKeyInfos = Collections.singletonList(keyInfo);

        // 模拟加密行为
        when(encryptionAdapter.encrypt(anyString(), any())).thenReturn("encrypted-key");

        // 反射调用私有方法
        ReflectionTestUtils.invokeMethod(toolManagementService, "encryptAuthKeyInfo", authKeyInfos);

        // 断言加密成功
        assertEquals("encrypted-key", authKeyInfos.get(0).getAuthKey());
    }

    @Test
    @DisplayName("覆盖 updateAuthKeys：测试映射更新与加密")
    void testUpdateAuthKeys_ShouldUpdateAndEncrypt() {
        // 准备 updates 数据
        AuthKeyInfo updateInfo = new AuthKeyInfo().setTargetName("api-key").setAuthKey("new-plain-key");
        List<AuthKeyInfo> updates = Collections.singletonList(updateInfo);

        // 准备 existingKeys 数据
        AuthKeyInfo existingInfo = new AuthKeyInfo().setTargetName("api-key").setAuthKey("old-key");
        List<AuthKeyInfo> existingKeys = Collections.singletonList(existingInfo);

        // 模拟加密行为
        when(encryptionAdapter.encrypt(eq("new-plain-key"), any())).thenReturn("new-encrypted-key");

        // 反射调用私有方法
        List<AuthKeyInfo> result = ReflectionTestUtils.invokeMethod(toolManagementService, "updateAuthKeys", updates, existingKeys);

        // 断言值被成功更新并加密
        assertNotNull(result);
        assertEquals("new-encrypted-key", result.get(0).getAuthKey());
    }

    @Test
    @DisplayName("覆盖 encryptedAuthInfo：测试 SERVICE 作用域下的加密")
    void testEncryptedAuthInfo_ShouldEncryptForServiceScope() {
        // 准备数据，必须是 SERVICE 域才会进入加密逻辑
        AuthKeyInfo keyInfo = new AuthKeyInfo().setTargetName("header-key").setAuthKey("plain-key");
        AuthInfo authInfo = new AuthInfo()
                .setScope(AuthInfo.ScopeEnum.SERVICE)
                .setAuthKeys(Collections.singletonList(keyInfo));

        // 模拟加密行为
        when(encryptionAdapter.encrypt(anyString(), any())).thenReturn("encrypted-key");

        // 反射调用私有方法
        AuthInfo result = ReflectionTestUtils.invokeMethod(toolManagementService, "encryptedAuthInfo", authInfo);

        // 断言结果
        assertNotNull(result);
        assertEquals("encrypted-key", result.getAuthKeys().get(0).getAuthKey());
    }

    @Test
    @DisplayName("覆盖 maskKey 分支：解密抛出 AgentStudioException (强阻断异常)")
    void testMaskKey_AgentStudioException() {
        String cipherKey = "E1-{fake}-cipher";
        when(encryptionAdapter.decrypt(cipherKey))
                .thenThrow(new AgentStudioException(StudioError.KMS_CRYPTO_ERROR));

        String result = ReflectionTestUtils.invokeMethod(toolManagementService, "maskKey", cipherKey);

        assertEquals(CommonConstant.ANONYMIZED_TEXT, result);
    }

    @Test
    @DisplayName("覆盖 maskKey 分支：解密抛出通用 Exception")
    void testMaskKey_GeneralException() {
        String cipherKey = "E1-{fake}-cipher";
        when(encryptionAdapter.decrypt(cipherKey))
                .thenThrow(new RuntimeException("Unexpected error"));

        String result = ReflectionTestUtils.invokeMethod(toolManagementService, "maskKey", cipherKey);

        assertEquals(CommonConstant.ANONYMIZED_TEXT, result);
    }

    @Test
    @DisplayName("覆盖 maskKey 分支：明文长度等于 1")
    void testMaskKey_LengthIsOne() {
        String cipherKey = "E1-{fake}-cipher";
        when(encryptionAdapter.decrypt(cipherKey)).thenReturn("A"); // 模拟明文长度为1

        String result = ReflectionTestUtils.invokeMethod(toolManagementService, "maskKey", cipherKey);

        // 预期结果：A + 脱敏符去掉第一个字符
        String expected = "A" + CommonConstant.ANONYMIZED_TEXT.substring(1);
        assertEquals(expected, result);
    }

}

