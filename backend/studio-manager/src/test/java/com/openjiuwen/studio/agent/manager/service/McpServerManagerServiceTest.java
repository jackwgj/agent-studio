/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.MockitoAnnotations.openMocks;

import com.alibaba.fastjson2.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.page.PageMethod;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.common.utils.RequestHeaderHolderUtils;
import com.openjiuwen.studio.agent.common.utils.UrlCheckUtils;
import com.openjiuwen.studio.agent.manager.constant.Constants;
import com.openjiuwen.studio.agent.common.dto.auth.AuthInfo;
import com.openjiuwen.studio.agent.common.dto.auth.AuthKeyInfo;
import com.openjiuwen.studio.agent.manager.dto.ListMcpServersQo;
import com.openjiuwen.studio.agent.manager.dto.McpCallToolReq;
import com.openjiuwen.studio.agent.common.dto.mcp.McpCallToolResp;
import com.openjiuwen.studio.agent.common.dto.mcp.McpCallToolRespContent;
import com.openjiuwen.studio.agent.manager.dto.McpSearchCriteria;
import com.openjiuwen.studio.agent.manager.dto.McpServerConfig;
import com.openjiuwen.studio.agent.manager.dto.McpServerInfo;
import com.openjiuwen.studio.agent.manager.dto.McpServerInfoList;
import com.openjiuwen.studio.agent.manager.dto.McpServerReq;
import com.openjiuwen.studio.agent.manager.dto.McpServerTool;
import com.openjiuwen.studio.agent.manager.dto.McpServerTools;
import com.openjiuwen.studio.agent.manager.entity.Agent;
import com.openjiuwen.studio.agent.manager.entity.MappingEntity;
import com.openjiuwen.studio.agent.manager.entity.McpServiceEntity;
import com.openjiuwen.studio.agent.manager.entity.WorkflowEntity;
import com.openjiuwen.studio.agent.manager.mapper.AgentMapper;
import com.openjiuwen.studio.agent.manager.mapper.MappingMapper;
import com.openjiuwen.studio.agent.manager.mapper.McpServerConfigMapper;
import com.openjiuwen.studio.agent.manager.mapper.McpServerMapper;
import com.openjiuwen.studio.agent.manager.mapper.WorkflowMapper;
import com.openjiuwen.studio.agent.manager.obs.MgObsService;
import com.openjiuwen.studio.agent.manager.rce.client.AgentRuntimeClient;
import com.openjiuwen.studio.agent.common.utils.CryptoUtils;
import com.openjiuwen.studio.agent.manager.utils.TestUtil;
import com.openjiuwen.studio.common.service.service.EncryptionAdapter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 功能描述
 *
 * 这段代码现在通过 Adapter 的 Mock 生效：
 * Mockito.lenient().when(encryptionAdapter.encrypt(anyString(), anyString())).thenReturn("Encrypted String");
 * 你可以在具体的 Test 方法里覆盖 setUp 中的默认行为，如果需要特定的返回值。
 */
public class McpServerManagerServiceTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MappingMapper mappingMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private McpServerMapper serverMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private McpServerConfigMapper configMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private WorkflowMapper workflowMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private UrlCheckUtils checkUtils;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MgObsService obsService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private AgentMapper agentMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private AgentRuntimeClient runtimeClient;

    // 【新增】Mock EncryptionAdapter
    @Mock
    private EncryptionAdapter encryptionAdapter;

    private McpServerManagerService mcpServerManagerService;

    private AutoCloseable mockitoCloseable;

    private static MockedStatic<RequestHeaderHolderUtils> mockedHeaderStatic;

    private MessageSource messageSource;

    @BeforeEach
    public void setUp() throws Exception {
        mockitoCloseable = openMocks(this);
        mockedHeaderStatic = Mockito.mockStatic(RequestHeaderHolderUtils.class);
        mockedHeaderStatic.when(RequestHeaderHolderUtils::getRequestLanguage).thenReturn("en-US");
        WorkflowCommonService workflowCommonService = new WorkflowCommonService();
        ReflectionTestUtils.setField(workflowCommonService, "opSvcProjectId", "op_project_id");
        ReflectionTestUtils.setField(workflowCommonService, "workflowMapper", workflowMapper);
        mcpServerManagerService = new McpServerManagerService(serverMapper, checkUtils, mappingMapper, configMapper,
            workflowMapper, obsService, agentMapper, runtimeClient, messageSource, workflowCommonService);
        ReflectionTestUtils.setField(mcpServerManagerService, "opProjectId", "op_project_id");

        // 1. 注入 opProjectId (原有逻辑)
        ReflectionTestUtils.setField(mcpServerManagerService, "opProjectId", "op_project_id");

        // 2. 【新增】注入 encryptionAdapter 到实例
        ReflectionTestUtils.setField(mcpServerManagerService, "encryptionAdapter", encryptionAdapter);

        // 3. 【新增】注入 mcpDomainId 配置值
        ReflectionTestUtils.setField(mcpServerManagerService, "mcpDomainId", "mcp-service-test");

        // 4. 【新增】初始化静态桥接变量 (非常重要！否则静态方法调用会报错)
        // 这一步模拟了 @PostConstruct public void init() 的行为
        ReflectionTestUtils.setField(McpServerManagerService.class, "staticEncryptionAdapter", encryptionAdapter);

        // 5. 【新增】配置 Mock 的默认行为，防止 encrypt/decrypt 返回 null 导致后续逻辑报错
        // 使用 lenient() 是因为不是每个测试都会调用加密，防止 Mockito 报错 "Unnecessary Stubbing"
        Mockito.lenient().when(encryptionAdapter.encrypt(anyString(), anyString())).thenAnswer(invocation -> "encrypted_" + invocation.getArgument(0));
        Mockito.lenient().when(encryptionAdapter.decrypt(anyString())).thenAnswer(invocation -> invocation.getArgument(0).toString().replace("encrypted_", ""));
    }

    @AfterEach
    public void tearDown() throws Exception {
        mockitoCloseable.close();
        mockedHeaderStatic.close();
    }

    @Test
    public void test_add_mcp_server_should_return_not_null_when_objects_is_null() throws Exception {
        try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class,
            RETURNS_DEEP_STUBS)) {
            // setup
            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserName()).thenReturn("string");
            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserId()).thenReturn("string");
            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestAuthToken()).thenReturn("token");

            Mockito.when(runtimeClient.queryMcpServerTools(anyString(), anyString(), any())).thenReturn(null);
            Mockito.when(serverMapper.insert(any(McpServerInfo.class))).thenReturn(0);
            Mockito.when(serverMapper.selectByPrimaryKey(anyString(), anyString(), anyString())).thenReturn(null);

            McpServerReq req = new McpServerReq();
            req.setName("aa");
            // run the test
            assertThrows(AgentStudioException.class, () -> {
                mcpServerManagerService.addMcpServer("string", req);
            });
        }
    }

    @Test
    public void test_add_mcp_server_should_return_not_null_when_test_data_combination() throws Exception {
        try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class,
            RETURNS_DEEP_STUBS);
            MockedStatic<CryptoUtils> mockedStaticSccCryptUtils = mockStatic(CryptoUtils.class,
                RETURNS_DEEP_STUBS)) {
            // setup
            mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserName).thenReturn("string");
            mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestUserId).thenReturn("string");
            mockedStaticRequestContextUtils.when(RequestContextUtils::getRequestAuthToken).thenReturn("string");
            mockedStaticSccCryptUtils.when(() -> CryptoUtils.encrypt(anyString())).thenReturn("abc");

            McpServerTools mcpServerTools = new McpServerTools();
            Mockito.when(runtimeClient.queryMcpServerTools(anyString(), anyString(), any()))
                .thenReturn(new ResponseEntity<>(mcpServerTools, HttpStatusCode.valueOf(200)));

            Mockito.when(serverMapper.insert(any(McpServerInfo.class))).thenReturn(0);
            McpServerInfo mcpServerInfo = new McpServerInfo();
            Mockito.when(serverMapper.selectByPrimaryKey(anyString(), anyString(), anyString()))
                .thenReturn(mcpServerInfo.setType("custom"));

            McpServerReq body = new McpServerReq();
            body.setName("name");
            body.setDesc("desc");
            body.setNameEn("name_en");
            body.setUrl("https://fake.com");
            body.setIsTemplate(true);
            body.setAuth(new AuthInfo().setDomain(AuthInfo.DomainEnum.HEADERS)
                .setAuthKeys(List.of(new AuthKeyInfo().setTargetName("key").setAuthKey("123"))));

            // run the test
            McpServerInfo result = mcpServerManagerService.addMcpServer("op_project_id", body);

            // verify the results
            assertNotNull(result);

            // run the test
            result = mcpServerManagerService.addMcpServer("project_id", body);
            // verify the results
            assertNotNull(result);
        }
    }

    @Test
    public void test_delete_mcp_server_should_return_not_null_when_objects_is_null() throws Exception {
        try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class,
            RETURNS_DEEP_STUBS)) {
            // setup
            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserId()).thenReturn("string");

            Mockito.when(serverMapper.selectByPrimaryKey(anyString(), anyString(), anyString())).thenReturn(null);
            Mockito.when(serverMapper.deleteByPrimaryKey(anyString(), anyString(), anyString())).thenReturn(0);

            Mockito.when(mappingMapper.updateValidByResourceIdAndVersionId(anyString(), anyString())).thenReturn(0);

            // run the test
            mcpServerManagerService.deleteMcpServer("string", "string");
        }
    }

    @Test
    public void test_delete_mcp_server_should_return_not_null_when_test_data_combination() throws Exception {
        try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class,
            RETURNS_DEEP_STUBS)) {
            // setup
            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserId()).thenReturn("string");

            McpServerInfo mcpServerInfo = new McpServerInfo().setCreatorId("string");
            Mockito.when(serverMapper.selectByPrimaryKey(anyString(), anyString(), anyString()))
                .thenReturn(mcpServerInfo);
            Mockito.when(serverMapper.deleteByPrimaryKey(anyString(), anyString(), anyString())).thenReturn(0);

            Mockito.when(mappingMapper.updateValidByResourceIdAndVersionId(anyString(), anyString())).thenReturn(0);

            // run the test
            mcpServerManagerService.deleteMcpServer("string", "string");
        }
    }

    @Test
    public void test_get_mcp_server_should_return_not_null_when_objects_is_null() throws Exception {
        try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class,
            RETURNS_DEEP_STUBS)) {
            // setup
            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserId()).thenReturn("string");

            Mockito.when(serverMapper.selectByPrimaryKey(anyString(), anyString(), anyString())).thenReturn(null);

            Mockito.when(mappingMapper.selectByResourceId(anyString())).thenReturn(null);

            assertThrows(AgentStudioException.class, () -> {
                mcpServerManagerService.getMcpServer("string", "string");
            });
        }
    }

    @Test
    public void test_get_mcp_server_should_return_not_null_when_test_data_combination() throws Exception {
        try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class,
            RETURNS_DEEP_STUBS)) {
            // setup
            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserId()).thenReturn("string");

            McpServerInfo mcpServerInfo = new McpServerInfo();
            Mockito.when(serverMapper.selectByPrimaryKey(anyString(), anyString(), anyString()))
                .thenReturn(mcpServerInfo.setType("inner"));

            List<MappingEntity> list = new ArrayList<>();
            MappingEntity mappingEntity = new MappingEntity();
            list.add(mappingEntity);
            Mockito.when(mappingMapper.selectByResourceId(anyString())).thenReturn(list);

            // run the test
            McpServerInfo result = mcpServerManagerService.getMcpServer("string", "string");

            // verify the results
            assertNotNull(result);
        }
    }

    @Test
    public void test_list_mcp_servers_should_return_not_null_when_objects_is_null() throws Exception {
        try (MockedStatic<PageMethod> mockedStaticPageMethod = mockStatic(PageMethod.class, RETURNS_DEEP_STUBS);
            MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class,
                RETURNS_DEEP_STUBS)) {
            // setup
            mockedStaticPageMethod.when(() -> PageMethod.offsetPage(anyInt(), anyInt())).thenReturn(null);

            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserId()).thenReturn("string");

            Mockito.when(serverMapper.selectBySearchCriteria(anyString(), anyString(), any(ListMcpServersQo.class)))
                .thenReturn(new ArrayList<>());

            // run the test
            McpServerInfoList result = mcpServerManagerService.listMcpServers("string", new ListMcpServersQo());

            // verify the results
            assertNotNull(result);
        }
    }

    @Test
    public void test_list_mcp_servers_should_return_not_null_when_test_data_combination() throws Exception {
        try (MockedStatic<PageMethod> mockedStaticPageMethod = mockStatic(PageMethod.class, RETURNS_DEEP_STUBS);
            MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class,
                RETURNS_DEEP_STUBS)) {
            // setup
            Page<Object> list = new Page<>();
            Object object = new Object();
            list.add(object);
            mockedStaticPageMethod.when(() -> PageMethod.offsetPage(anyInt(), anyInt())).thenReturn(list);

            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserId()).thenReturn("string");

            List<McpServerInfo> list1 = new ArrayList<>();
            McpServerInfo mcpServerInfo = new McpServerInfo();
            mcpServerInfo.setType("custom");
            list1.add(mcpServerInfo);
            Mockito.when(serverMapper.selectBySearchCriteria(anyString(), anyString(), any(ListMcpServersQo.class)))
                .thenReturn(list1);

            McpSearchCriteria body = new McpSearchCriteria();

            // run the test
            McpServerInfoList result = mcpServerManagerService.listMcpServers("string", new ListMcpServersQo());

            // verify the results
            assertNotNull(result);
        }
    }

    @Test
    public void test_update_mcp_server_should_return_not_null_when_objects_is_null() throws Exception {
        try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class,
            RETURNS_DEEP_STUBS);
            MockedStatic<CryptoUtils> mockedStaticCryptoUtils = mockStatic(CryptoUtils.class,
                RETURNS_DEEP_STUBS)) {
            // setup
            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserId()).thenReturn("string");
            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestAuthToken()).thenReturn("string");
            mockedStaticCryptoUtils.when(() -> CryptoUtils.encrypt(anyString())).thenReturn("Encrypted String");

            Mockito.when(serverMapper.selectByPrimaryKey(anyString(), anyString(), anyString()))
                .thenReturn(new McpServerInfo().setCreatorId("string").setUrl("https://fake.com"));
            Mockito.when(serverMapper.updateById(anyString(), anyString(), anyString(), any(McpServerInfo.class)))
                .thenReturn(0);

            Mockito.when(runtimeClient.queryMcpServerTools(anyString(), anyString(), any()))
                .thenReturn(new ResponseEntity<>(new McpServerTools(), HttpStatusCode.valueOf(200)));

            McpServerReq req = new McpServerReq();
            req.setIcon("3f2504e0-4f89-11d3-9a0c-0305e82c3301.jpg");
            req.setName("test_name");
            req.setNameEn("test_name_en");
            req.setDesc("test_Desc");
            req.setUrl("https://fake.com");
            req.setIsTemplate(true);
            req.setAuth(new AuthInfo().setDomain(AuthInfo.DomainEnum.HEADERS)
                .setScope(AuthInfo.ScopeEnum.SERVICE)
                .setAuthKeys(List.of(new AuthKeyInfo().setTargetName("key").setAuthKey("123"))));

            // run the test
            McpServerInfo result = mcpServerManagerService.updateMcpServer("op_project_id", "string", req);
            // verify the results
            assertNotNull(result);

            // run the test
            req.setAuth(null);
            result = mcpServerManagerService.updateMcpServer("string", "string", req);
            // verify the results
            assertNotNull(result);

            String serverId = "test_mcp_server_id";
            List<MappingEntity> list = new ArrayList<>();
            MappingEntity mappingEntity = new MappingEntity();
            mappingEntity.setResourceId(serverId);
            mappingEntity.setResourceName("name");
            mappingEntity.setResourceDesc("desc");
            mappingEntity.setAppType("workflow");
            list.add(mappingEntity);
            Mockito.when(mappingMapper.selectByResourceId(anyString())).thenReturn(list);

            mcpServerManagerService.updateMcpServer(Constants.TEST_PROJECT_ID, serverId, req);
            List<MappingEntity> mappingEntities = mappingMapper.selectByResourceId(serverId);

            assertEquals("test_name", mappingEntities.get(0).getResourceName());
            assertEquals("test_Desc", mappingEntities.get(0).getResourceDesc());
        }
    }

    @Test
    public void test_update_mcp_server_auth_ir() throws Exception {
        try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class,
            RETURNS_DEEP_STUBS);
            MockedStatic<CryptoUtils> mockedStaticCryptoUtils = mockStatic(CryptoUtils.class,
                RETURNS_DEEP_STUBS)) {
            // setup
            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserId()).thenReturn("string");
            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestAuthToken()).thenReturn("string");
            mockedStaticCryptoUtils.when(() -> CryptoUtils.encrypt(anyString())).thenReturn("Encrypted String");

            String serverId = "test_mcp_server_id";

            Mockito.when(serverMapper.selectByPrimaryKey(anyString(), anyString(), anyString()))
                .thenReturn(new McpServerInfo().setCreatorId("string")
                    .setUrl("https://fake.com")
                    .setServerId(serverId)
                    .setAuth(new AuthInfo().setScope(AuthInfo.ScopeEnum.SERVICE)
                        .setDomain(AuthInfo.DomainEnum.HEADERS)
                        .setAuthKeys(
                            List.of(new AuthKeyInfo().setTargetName("key1").setSourceName("key").setAuthKey("AAA")))));
            Mockito.when(serverMapper.updateById(anyString(), anyString(), anyString(), any(McpServerInfo.class)))
                .thenReturn(0);

            Mockito.when(runtimeClient.queryMcpServerTools(anyString(), anyString(), any()))
                .thenReturn(new ResponseEntity<>(new McpServerTools(), HttpStatusCode.valueOf(200)));

            McpServerReq req = new McpServerReq();
            req.setIcon("3f2504e0-4f89-11d3-9a0c-0305e82c3301.jpg");
            req.setName("test_name");
            req.setNameEn("test_name_en");
            req.setDesc("test_Desc");
            req.setUrl("https://fake.com");
            req.setAuth(new AuthInfo().setScope(AuthInfo.ScopeEnum.SERVICE)
                .setDomain(AuthInfo.DomainEnum.HEADERS)
                .setAuthKeys(List.of(new AuthKeyInfo().setTargetName("key").setSourceName("key").setAuthKey("AAA"))));

            List<MappingEntity> list = new ArrayList<>();
            MappingEntity mappingEntity = new MappingEntity();
            mappingEntity.setResourceId(serverId);
            mappingEntity.setResourceName("name");
            mappingEntity.setResourceDesc("desc");
            mappingEntity.setResourceType("mcp");
            mappingEntity.setAppType("workflow");
            mappingEntity.setAppId("workflow_id");
            list.add(mappingEntity);

            mappingEntity = new MappingEntity();
            mappingEntity.setResourceId(serverId);
            mappingEntity.setResourceName("name");
            mappingEntity.setResourceDesc("desc");
            mappingEntity.setResourceType("mcp");
            mappingEntity.setAppType("agent");
            mappingEntity.setAppId("agent_id");
            list.add(mappingEntity);
            Mockito.when(mappingMapper.selectByResourceId(anyString())).thenReturn(list);

            WorkflowEntity entity = new WorkflowEntity();
            entity.setId("id");
            entity.setIrPath("irPath");
            Mockito.when(workflowMapper.getWorkflowEntity(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(entity);
            String dslJsonStr = TestUtil.getStringFromFile("classpath:flow_dsl/test_workflow_mcp_ir.json");
            Mockito.when(obsService.downloadObsFile(entity.getIrPath())).thenReturn(dslJsonStr);
            Mockito.when(workflowMapper.updateWorkflowEntity(anyString(), anyString(), any())).thenReturn(1);
            Mockito.when(obsService.uploadObsFile(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn("ir");

            Agent agent = new Agent();
            agent.setIrPath("agentIr");
            Mockito.when(agentMapper.selectByPrimaryKey(anyString(), anyString())).thenReturn(agent);

            String agentIrStr = TestUtil.getStringFromFile("classpath:flow_dsl/test_agent_flow_ir.json");
            Mockito.when(obsService.downloadObsFile(agent.getIrPath())).thenReturn(agentIrStr);

            mcpServerManagerService.updateMcpServer(Constants.TEST_PROJECT_ID, serverId, req);
            List<MappingEntity> mappingEntities = mappingMapper.selectByResourceId(serverId);

            assertEquals("test_name", mappingEntities.get(0).getResourceName());
            assertEquals("test_Desc", mappingEntities.get(0).getResourceDesc());

            assertEquals("test_name", mappingEntities.get(1).getResourceName());
            assertEquals("test_Desc", mappingEntities.get(1).getResourceDesc());
        }
    }

    @Test
    public void test_get_mcp_server_list() throws Exception {
        try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class,
            RETURNS_DEEP_STUBS)) {
            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestAuthToken()).thenReturn("token");
            // setup
            McpServerTools tools1 = new McpServerTools();
            tools1.setCount(1);
            tools1.setTools(List.of(new McpServerTool().setName("11")));
            Mockito.when(runtimeClient.queryMcpServerTools(anyString(), anyString(), any()))
                .thenReturn(new ResponseEntity<>(tools1, HttpStatusCode.valueOf(200)));

            ResponseEntity<McpServerTools> response = new ResponseEntity<>(tools1, HttpStatusCode.valueOf(200));

            Mockito.when(runtimeClient.queryMcpServerTools(anyString(), anyString(), any())).thenReturn(response);

            McpServerReq body = new McpServerReq().setUrl("https://fake.com");

            // run the test
            McpServerTools result = mcpServerManagerService.listMcpServerTools("string", 0, 1, body);

            // verify the results
            assertNotNull(result);
        }
    }

    @Test
    public void test_delete_mcp_server_should_throw_exception() throws Exception {
        try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class,
            RETURNS_DEEP_STUBS)) {
            // setup
            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserId()).thenReturn("user_id_1");

            McpServerInfo mcpServerInfo = new McpServerInfo().setCreatorId("user_id_2");
            Mockito.when(serverMapper.selectByPrimaryKey(anyString(), anyString(), anyString()))
                .thenReturn(mcpServerInfo);
            Mockito.when(serverMapper.deleteByPrimaryKey(anyString(), anyString(), anyString())).thenReturn(0);

            Mockito.when(mappingMapper.updateValidByResourceIdAndVersionId(anyString(), anyString())).thenReturn(0);

            // 抛出权限校验异常
            assertThrows(AgentStudioException.class, () -> {
                mcpServerManagerService.deleteMcpServer("string", "string");
            });
        }
    }

    @Test
    public void test_config_mcp_server_should_return_not_null_when_test_data_combination() throws Exception {
        try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class,
            RETURNS_DEEP_STUBS);
            MockedStatic<CryptoUtils> mockedStaticMgSccCryptoUtils = mockStatic(CryptoUtils.class,
                RETURNS_DEEP_STUBS)) {
            // setup
            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserId()).thenReturn("string");

            mockedStaticMgSccCryptoUtils.when(() -> CryptoUtils.encrypt(anyString())).thenReturn("string");

            Mockito.when(configMapper.insert(any(McpServerConfig.class))).thenReturn(0);
            McpServerConfig mcpServerConfig = new McpServerConfig();
            Mockito.when(configMapper.selectByUniqueIds(anyString(), anyString(), anyString()))
                .thenReturn(mcpServerConfig);

            McpServerInfo mcpServerInfo = new McpServerInfo();
            mcpServerInfo.setAuth(new AuthInfo().setAuthKeys(List.of(new AuthKeyInfo().setTargetName("key"))));
            mcpServerInfo.setType("inner");
            mcpServerInfo.setCategory("template");
            Mockito.when(serverMapper.selectByPrimaryKey(anyString(), anyString(), anyString()))
                .thenReturn(mcpServerInfo);

            McpServerConfig body = new McpServerConfig();
            body.setAuthKeys(List.of(new AuthKeyInfo().setTargetName("key").setAuthKey("value1")));

            // run the test
            McpServerConfig result = mcpServerManagerService.configMcpServer("string", "string", body);

            // verify the results
            assertNotNull(result);
        }
    }

    @Test
    public void test_update_mcp_server_config_should_return_not_null_when_objects_is_null() throws Exception {
        try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class,
            RETURNS_DEEP_STUBS);
            MockedStatic<CryptoUtils> mockedStaticMgSccCryptoUtils = mockStatic(CryptoUtils.class,
                RETURNS_DEEP_STUBS)) {
            // setup
            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserId()).thenReturn("string");
            mockedStaticMgSccCryptoUtils.when(() -> CryptoUtils.encrypt(anyString())).thenReturn("string");

            Mockito.when(configMapper.selectByUniqueIds(anyString(), anyString(), anyString()))
                .thenReturn(new McpServerConfig());
            Mockito.when(configMapper.updateByUniqueIds(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(0);
            McpServerInfo mcpServerInfo = new McpServerInfo();
            mcpServerInfo.setCategory("template");
            mcpServerInfo.setType("inner");
            mcpServerInfo.setAuth(new AuthInfo().setAuthKeys(List.of(new AuthKeyInfo().setTargetName("key"))));
            Mockito.when(serverMapper.selectByPrimaryKey(anyString(), anyString(), anyString()))
                .thenReturn(mcpServerInfo);

            McpServerConfig body = new McpServerConfig();
            body.setAuthKeys(List.of(new AuthKeyInfo().setTargetName("key").setAuthKey("value1")));
            // run the test
            mcpServerManagerService.updateMcpServerConfig("string", "string", body);
        }
    }

    @Test
    public void test_delete_mcp_server_config_should_return_not_null_when_objects_is_null() throws Exception {
        try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class,
            RETURNS_DEEP_STUBS)) {
            // setup
            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserId()).thenReturn("string");

            Mockito.when(configMapper.selectByUniqueIds(anyString(), anyString(), anyString())).thenReturn(null);
            Mockito.when(configMapper.deleteByUniqueIds(anyString(), anyString(), anyString())).thenReturn(0);

            mcpServerManagerService.deleteMcpServerConfig("string", "string");
        }
    }

    @Test
    public void test_delete_mcp_server_config_should_return_not_null_when_test_data_combination() throws Exception {
        try (MockedStatic<RequestContextUtils> mockedStaticRequestContextUtils = mockStatic(RequestContextUtils.class,
            RETURNS_DEEP_STUBS)) {
            // setup
            mockedStaticRequestContextUtils.when(() -> RequestContextUtils.getRequestUserId()).thenReturn("string");

            McpServerConfig mcpServerConfig = new McpServerConfig();
            Mockito.when(configMapper.selectByUniqueIds(anyString(), anyString(), anyString()))
                .thenReturn(mcpServerConfig);
            Mockito.when(configMapper.deleteByUniqueIds(anyString(), anyString(), anyString())).thenReturn(0);

            mcpServerManagerService.deleteMcpServerConfig("string", "string");
        }
    }

    @Test
    public void testCallTool() {
        try (MockedStatic<RequestContextUtils> requestContextUtilsMockedStatic = mockStatic(
            RequestContextUtils.class)) {
            requestContextUtilsMockedStatic.when(RequestContextUtils::getRequestAuthToken).thenReturn("userToken");
            requestContextUtilsMockedStatic.when(RequestContextUtils::getRequestUserId).thenReturn("userId");

            McpServerInfo mcpServerInfo = new McpServerInfo().setCreatorId("user_id_2");
            Mockito.when(serverMapper.selectByPrimaryKey(anyString(), anyString(), anyString()))
                .thenReturn(mcpServerInfo);
            Mockito.when(runtimeClient.callMcpServerTool(anyString(), anyString(), any()))
                .thenReturn(new ResponseEntity<>(new McpCallToolResp().setIsError(false)
                    .setContent(List.of(new McpCallToolRespContent().setType("text").setText("cal tool result"))),
                    HttpStatusCode.valueOf(200)));

            McpCallToolReq req = new McpCallToolReq().setName("toolName").setParams(Map.of("key", "value"));
            McpCallToolResp resp = mcpServerManagerService.callTool("project id", "server_id", req);
            Assertions.assertFalse(resp.isIsError());
            Assertions.assertEquals(resp.getContent().get(0).getText(), "cal tool result");
        }
    }

    @Test
    public void test_recovery_auth_info_should_void_when_objects_is_null() throws Exception {
        try (MockedStatic<CryptoUtils> mockedStaticMgSccCryptoUtils = mockStatic(CryptoUtils.class,
            RETURNS_DEEP_STUBS)) {
            // setup
            mockedStaticMgSccCryptoUtils.when(() -> CryptoUtils.decrypt(anyString())).thenReturn("string");

            McpServerInfo mcpServerInfo = new McpServerInfo();
            mcpServerInfo.setCategory("template");
            mcpServerInfo.setType("inner");
            mcpServerInfo.setAuth(new AuthInfo().setAuthKeys(List.of(new AuthKeyInfo().setTargetName("key"))));
            mcpServerInfo.setConfigs(
                new McpServerConfig().setAuthKeys(List.of(new AuthKeyInfo().setTargetName("key").setAuthKey("123"))));

            // run the test
            McpServerManagerService.recoveryAuthInfo(mcpServerInfo);

            Assertions.assertEquals(mcpServerInfo.getAuth().getAuthKeys().get(0).getAuthKey(), "123");
        }
    }

    @Test
    public void test_recovery_auth_info_should_void_when_test_data_combination() throws Exception {
        try (MockedStatic<CryptoUtils> mockedStaticMgSccCryptoUtils = mockStatic(CryptoUtils.class,
            RETURNS_DEEP_STUBS)) {
            // setup
            mockedStaticMgSccCryptoUtils.when(() -> CryptoUtils.decrypt(anyString())).thenReturn("string");

            McpServerInfo serverInfo = new McpServerInfo();

            // run the test
            McpServerManagerService.recoveryAuthInfo(serverInfo);
        }
    }

    @Test
    public void test_recoveryAuthInfo2() throws Exception {
        try (MockedStatic<CryptoUtils> mockedStaticMgSccCryptoUtils = mockStatic(CryptoUtils.class,
            RETURNS_DEEP_STUBS)) {
            JSONObject jsonObject = new JSONObject();
            Map<String, String> map = new HashMap<>();
            map.put("x-hw", "test");
            jsonObject.put("headers", map);
            // setup
            mockedStaticMgSccCryptoUtils.when(() -> CryptoUtils.decrypt(anyString()))
                .thenReturn(jsonObject.toString());

            McpServiceEntity mcpService = new McpServiceEntity();

            mcpService.setServerConfig(jsonObject.toString());

            // run the test
            McpServerManagerService.recoveryAuthInfo(mcpService);
        }
    }

    @Test
    public void test_recoveryAuthInfo3() throws Exception {
        JSONObject jsonObject = new JSONObject();
        Map<String, String> map = new HashMap<>();
        map.put("x-hw", "test");
        jsonObject.put("headers", map);

        McpServiceEntity mcpService = new McpServiceEntity();

        mcpService.setServerConfig(jsonObject.toString());

        // run the test
        McpServerManagerService.recoveryAuthInfo(mcpService);

    }
}

