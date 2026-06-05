/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2018-2020. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.type.TypeReference;
import com.openjiuwen.studio.agent.common.enums.NodeType;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.common.utils.RequestHeaderHolderUtils;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.manager.constant.Constants;
import com.openjiuwen.studio.agent.manager.dto.ControllerNodeConfigVO;
import com.openjiuwen.studio.agent.manager.dto.ControllerNodeVO;
import com.openjiuwen.studio.agent.manager.dto.ControllerVO;
import com.openjiuwen.studio.agent.manager.dto.ImportListInfo;
import com.openjiuwen.studio.agent.manager.dto.ModelConfigVO;
import com.openjiuwen.studio.agent.manager.dto.ModelServiceProviderReq;
import com.openjiuwen.studio.agent.manager.dto.WorkflowNodeVO;
import com.openjiuwen.studio.agent.manager.dto.WorkflowVO;
import com.openjiuwen.studio.agent.manager.entity.Agent;
import com.openjiuwen.studio.agent.manager.entity.AgentExportEntity;
import com.openjiuwen.studio.agent.manager.entity.WorkflowExportEntity;
import com.openjiuwen.studio.agent.manager.entity.md.ModelServiceBase;
import com.openjiuwen.studio.agent.manager.entity.md.ModelServiceProvider;
import com.openjiuwen.studio.agent.manager.entity.md.ProviderAuthMetadata;
import com.openjiuwen.studio.agent.manager.entity.plugin.PluginEntity;
import com.openjiuwen.studio.agent.manager.mapper.ToolMapper;
import com.openjiuwen.studio.agent.manager.mapper.md.FreeModelServiceMapper;
import com.openjiuwen.studio.agent.manager.mapper.md.ModelServiceMapper;
import com.openjiuwen.studio.agent.manager.mapper.md.ProviderAuthMetadataMapper;
import com.openjiuwen.studio.agent.manager.mapper.md.UserModelServiceProviderMapper;
import com.openjiuwen.studio.agent.manager.obs.MgObsService;
import com.openjiuwen.studio.agent.manager.service.md.ModelServiceManager;
import com.openjiuwen.studio.agent.manager.service.md.ModelServiceMgmtService;
import com.openjiuwen.studio.agent.manager.service.md.ProviderAuthService;
import com.openjiuwen.studio.agent.manager.service.md.ProviderMgmtService;
import com.openjiuwen.studio.agent.manager.utils.BaseTest;
import com.openjiuwen.studio.agent.manager.utils.JsonUtils;
import com.openjiuwen.studio.agent.manager.utils.TestUtil;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AgentImportExportServiceTest extends BaseTest {

    private static MockedStatic<RequestContextUtils> mockedStatic;

    private static MockedStatic<RequestHeaderHolderUtils> mockedHeaderStatic;

    private AutoCloseable mockitoCloseable;

    @Autowired
    ToolMapper toolMapper;

    @InjectMocks
    private AgentImportExportService agentImportExportService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MgObsService mgObsService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ModelServiceMapper modelServiceMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ProviderAuthService authService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ProviderMgmtService providerMgmtService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ModelServiceMgmtService modelServiceMgmtService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private UserModelServiceProviderMapper userProviderMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ProviderAuthMetadataMapper metadataMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ModelServiceManager modelServiceManager;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private FreeModelServiceMapper freeModelServiceMapper;

    @BeforeAll
    public static void init() {
        RequestContextUtils.setRequestAuthTokenAndProjectId(Constants.TEST_TOKEN, Constants.TEST_PROJECT_ID);
        mockedStatic = Mockito.mockStatic(RequestContextUtils.class);
        mockedHeaderStatic = Mockito.mockStatic(RequestHeaderHolderUtils.class);
        mockedStatic.when(RequestContextUtils::getRequestProjectId).thenReturn(Constants.TEST_PROJECT_ID);
        mockedStatic.when(RequestContextUtils::getRequestAuthToken).thenReturn(Constants.TEST_TOKEN);
        mockedStatic.when(RequestContextUtils::getRequestUserDomainId).thenReturn(Constants.TEST_DOMAIN_ID);
        mockedStatic.when(RequestContextUtils::getRequestUserName).thenReturn(Constants.TEST_USER_NAME);
        mockedStatic.when(RequestContextUtils::getRequestUserId).thenReturn("test_copy_user_id");
        mockedHeaderStatic.when(RequestHeaderHolderUtils::getRequestLanguage).thenReturn("zh-cn");
    }

    @AfterAll
    static void end() {
        mockedStatic.close();
        mockedHeaderStatic.close();
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @BeforeEach
    void setUp() {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
    }

    @Test
    void testExtractModelByWorkflow() throws IOException {
        final WorkflowVO workflowVO =
            TestUtil.getJsonObject(WorkflowVO.class, "classpath:flow_dsl/test_workflow_llm_dsl.json");
        WorkflowExportEntity workflowExportEntity = new WorkflowExportEntity();
        workflowExportEntity.setDsl(workflowVO);
        Map<String, Map<String, String>> models = agentImportExportService.extractModelByWorkflow(workflowExportEntity);

        assertNotNull(models);
        assertFalse(models.isEmpty());
        assertEquals("c08d9f96-a571-4dba-80b0-b9ba4901b306",
            models.get("c08d9f96-a571-4dba-80b0-b9ba4901b306").get("model_deployment_id"));
    }

    @Test
    void testExtractModelByWorkflowByNoModel() throws IOException {
        final WorkflowVO workflowVO =
            TestUtil.getJsonObject(WorkflowVO.class, "classpath:flow_dsl/test_workflow_flow_dsl.json");
        WorkflowExportEntity workflowExportEntity = new WorkflowExportEntity();
        workflowExportEntity.setDsl(workflowVO);
        Map<String, Map<String, String>> models = agentImportExportService.extractModelByWorkflow(workflowExportEntity);

        assertNotNull(models);
        assertTrue(models.isEmpty());
    }

    @Test
    void TestReplaceModelId() throws IOException {
        final WorkflowVO workflowVO =
            TestUtil.getJsonObject(WorkflowVO.class, "classpath:flow_dsl/test_workflow_llm_dsl.json");
        Map<String, String> replaceModelIdRecord = new HashMap<>();
        replaceModelIdRecord.put("c08d9f96-a571-4dba-80b0-b9ba4901b306", "a08d9f96-a571-4dba-80b0-b9ba4901b306");
        WorkflowExportEntity workflowExportEntity = new WorkflowExportEntity();
        workflowExportEntity.setDsl(workflowVO);
        workflowExportEntity = agentImportExportService.replaceModelId(workflowExportEntity, replaceModelIdRecord);
        Map<String, Map<String, String>> models = agentImportExportService.extractModelByWorkflow(workflowExportEntity);
        assertNull(models.get("c08d9f96-a571-4dba-80b0-b9ba4901b306"));
        assertNotNull(models.get("a08d9f96-a571-4dba-80b0-b9ba4901b306"));
        assertNotNull(models.get("a08d9f96-a571-4dba-80b0-b9ba4901b306").get("model_deployment_id"));
        assertEquals(models.get("a08d9f96-a571-4dba-80b0-b9ba4901b306").get("model_deployment_id"),
            "a08d9f96-a571-4dba-80b0-b9ba4901b306");
    }

    @Test
    void TestReplaceModelIdNone() throws IOException {
        final WorkflowVO workflowVO =
            TestUtil.getJsonObject(WorkflowVO.class, "classpath:flow_dsl/test_workflow_llm_dsl.json");
        Map<String, String> replaceModelIdRecord = new HashMap<>();
        WorkflowExportEntity workflowExportEntity = new WorkflowExportEntity();
        workflowExportEntity.setDsl(workflowVO);
        workflowExportEntity = agentImportExportService.replaceModelId(workflowExportEntity, replaceModelIdRecord);
        Map<String, Map<String, String>> models = agentImportExportService.extractModelByWorkflow(workflowExportEntity);
        assertNull(models.get("a08d9f96-a571-4dba-80b0-b9ba4901b306"));
        assertNotNull(models.get("c08d9f96-a571-4dba-80b0-b9ba4901b306"));
        assertNotNull(models.get("c08d9f96-a571-4dba-80b0-b9ba4901b306").get("model_deployment_id"));
        assertEquals(models.get("c08d9f96-a571-4dba-80b0-b9ba4901b306").get("model_deployment_id"),
            "c08d9f96-a571-4dba-80b0-b9ba4901b306");
    }

    @Test
    void TestModelServiceExist() {
        ModelServiceBase modelServiceBase = null;
        assertFalse(agentImportExportService.hasModelServiceExist(modelServiceBase, "", ""));
        modelServiceBase = new ModelServiceBase();
        modelServiceBase.setProjectId("test");
        modelServiceBase.setWorkspaceId("test");
        assertTrue(agentImportExportService.hasModelServiceExist(modelServiceBase, "test", "test"));
        assertFalse(agentImportExportService.hasModelServiceExist(modelServiceBase, "test-project", "test"));
        assertFalse(agentImportExportService.hasModelServiceExist(modelServiceBase, "test", "test-workspace"));
        assertFalse(agentImportExportService.hasModelServiceExist(modelServiceBase, "test-project", "test-workspace"));

        modelServiceBase.setProjectId("SYSTEM");
        modelServiceBase.setWorkspaceId("SYSTEM");

        assertTrue(agentImportExportService.hasModelServiceExist(modelServiceBase, "test", "test"));
        assertTrue(agentImportExportService.hasModelServiceExist(modelServiceBase, "test-project", "test"));
        assertTrue(agentImportExportService.hasModelServiceExist(modelServiceBase, "test", "test-workspace"));
        assertTrue(agentImportExportService.hasModelServiceExist(modelServiceBase, "test-project", "test-workspace"));
    }

    @Test
    void TestImportModel() throws IOException {
        Map<String, String> replaceModelIdRecord = new HashMap<>();
        final WorkflowVO workflowVO =
            TestUtil.getJsonObject(WorkflowVO.class, "classpath:flow_dsl/test_workflow_llm_dsl.json");
        WorkflowExportEntity workflowExportEntity = new WorkflowExportEntity();
        workflowExportEntity.setDsl(workflowVO);
        String projectId = "project";
        String workSpace = "workspace";
        String modelId = "c08d9f96-a571-4dba-80b0-b9ba4901b306";
        ModelServiceBase modelServiceBase = new ModelServiceBase();
        modelServiceBase.setProjectId("project");
        modelServiceBase.setWorkspaceId("workspace");

        when(modelServiceMapper.queryById(modelId)).thenReturn(modelServiceBase);

        agentImportExportService.importModelByFlow(projectId, workflowExportEntity, workSpace, replaceModelIdRecord);

        // Then
        assertTrue(replaceModelIdRecord.isEmpty());
    }

    @Test
    void TestImportModelExistId() throws IOException {
        Map<String, String> replaceModelIdRecord = new HashMap<>();
        final WorkflowVO workflowVO =
            TestUtil.getJsonObject(WorkflowVO.class, "classpath:flow_dsl/test_workflow_llm_dsl.json");
        WorkflowExportEntity workflowExportEntity = new WorkflowExportEntity();
        workflowExportEntity.setDsl(workflowVO);
        String projectId = "project";
        String workSpace = "workspace";
        String modelId = "c08d9f96-a571-4dba-80b0-b9ba4901b306";
        ModelServiceBase modelServiceBase = new ModelServiceBase();
        modelServiceBase.setId(modelId);
        modelServiceBase.setProjectId("project");
        modelServiceBase.setWorkspaceId("workspace2");

        when(modelServiceMapper.queryById(modelId)).thenReturn(modelServiceBase);
        when(
            modelServiceMapper.queryByName(CommonConstant.SYSTEM_DEFAULT, CommonConstant.SYSTEM_DEFAULT, "modelimport",modelServiceBase.getProviderId()))
                .thenReturn(new ArrayList<>());
        when(modelServiceMapper.queryByName(projectId, workSpace, "modelimport",modelServiceBase.getProviderId())).thenReturn(new ArrayList<>());

        agentImportExportService.importModelByFlow(projectId, workflowExportEntity, workSpace, replaceModelIdRecord);

        // Then
        assertFalse(replaceModelIdRecord.isEmpty());
        assertTrue(replaceModelIdRecord.containsKey(modelId));
    }

    @Test
    void TestImportModelExistName() throws IOException {
        Map<String, String> replaceModelIdRecord = new HashMap<>();
        final WorkflowVO workflowVO =
            TestUtil.getJsonObject(WorkflowVO.class, "classpath:flow_dsl/test_workflow_llm_dsl.json");
        WorkflowExportEntity workflowExportEntity = new WorkflowExportEntity();
        workflowExportEntity.setDsl(workflowVO);
        String projectId = "project";
        String workSpace = "workspace";
        String modelId = "c08d9f96-a571-4dba-80b0-b9ba4901b306";
        ModelServiceBase modelServiceBase = new ModelServiceBase();
        modelServiceBase.setId("c08d9f96-a571-4dba-test");
        modelServiceBase.setProjectId("project");
        modelServiceBase.setWorkspaceId("workspace");

        when(modelServiceMapper.queryById(modelId)).thenReturn(null);
        when(modelServiceMapper.queryByName(projectId, workSpace, "modelimport",modelServiceBase.getProviderId()))
            .thenReturn(Collections.singletonList(modelServiceBase));
        when(modelServiceMapper.queryByName("SYSTEM", "SYSTEM", "modelimport",modelServiceBase.getProviderId())).thenReturn(Collections.emptyList());
        agentImportExportService.importModelByFlow(projectId, workflowExportEntity, workSpace, replaceModelIdRecord);

        // Then
        assertFalse(replaceModelIdRecord.isEmpty());
        assertEquals(replaceModelIdRecord.get("c08d9f96-a571-4dba-80b0-b9ba4901b306"), "c08d9f96-a571-4dba-test");
    }

    @Test
    void TestImportModelExistSysTemName() throws IOException {
        Map<String, String> replaceModelIdRecord = new HashMap<>();
        final WorkflowVO workflowVO =
            TestUtil.getJsonObject(WorkflowVO.class, "classpath:flow_dsl/test_workflow_llm_dsl.json");
        WorkflowExportEntity workflowExportEntity = new WorkflowExportEntity();
        workflowExportEntity.setDsl(workflowVO);
        String projectId = "project";
        String workSpace = "workspace";
        String modelId = "c08d9f96-a571-4dba-80b0-b9ba4901b306";
        ModelServiceBase modelServiceBase = new ModelServiceBase();
        modelServiceBase.setId("c08d9f96-a571-4dba-test");
        modelServiceBase.setProjectId("SYSTEM");
        modelServiceBase.setWorkspaceId("SYSTEM");

        when(modelServiceMapper.queryById(modelId)).thenReturn(null);
        when(modelServiceMapper.queryByName(projectId, workSpace, "modelimport",modelServiceBase.getProviderId())).thenReturn(Collections.emptyList());
        when(modelServiceMapper.queryByName("SYSTEM", "SYSTEM", "modelimport",modelServiceBase.getProviderId()))
            .thenReturn(Collections.singletonList(modelServiceBase));

        agentImportExportService.importModelByFlow(projectId, workflowExportEntity, workSpace, replaceModelIdRecord);

        // Then
        assertFalse(replaceModelIdRecord.isEmpty());
        assertEquals(replaceModelIdRecord.get("c08d9f96-a571-4dba-80b0-b9ba4901b306"), "c08d9f96-a571-4dba-test");
    }

    @Test
    void TestCreateModel() {
        Map<String, String> modeConfig = new HashMap<>();
        modeConfig.put(CommonConstant.ModelParam.MODEL_NAME, "import");
        modeConfig.put(CommonConstant.ModelParam.MODEL_TYPE, "LLM");
        modeConfig.put(CommonConstant.ModelParam.MODEL_DEPLOYMENT_ID, "c08d9f96-a571-4dba-80b0-b9ba4901b306");
        String projectId = "project";
        String workSpace = "workspace";
        String providerId = "proverId";
        ProviderAuthMetadata providerAuthMetadata = new ProviderAuthMetadata();
        providerAuthMetadata.setId("id");
        providerAuthMetadata.setProjectId(projectId);
        providerAuthMetadata.setWorkspaceId(workSpace);
        when(metadataMapper.selectByProvider(providerId)).thenReturn(Collections.singletonList(providerAuthMetadata));
        when(authService.selectProviderMetadataAndPermissionCheck(projectId, workSpace, providerId, false))
            .thenReturn(Collections.singletonList(providerAuthMetadata));
        ModelServiceBase modelServiceBase =
            agentImportExportService.buildImportModelService(modeConfig, providerId, projectId, workSpace);
        assertEquals("c08d9f96-a571-4dba-80b0-b9ba4901b306", modelServiceBase.getId());
    }

    @Test
    void TestCreateProviderExist() {
        ModelServiceProvider modelServiceProvider = new ModelServiceProvider();
        modelServiceProvider.setId("proverId");
        String projectId = "project";
        String workSpace = "workspace";
        when(userProviderMapper.selectUserDataByName(projectId, workSpace, "import-default"))
            .thenReturn(Collections.singletonList(modelServiceProvider));
        String provider = agentImportExportService.createProvider(projectId, workSpace);
        assertEquals("proverId", provider);
    }

    @Test
    void TestCreateProvider() {
        ModelServiceProvider modelServiceProvider = new ModelServiceProvider();
        modelServiceProvider.setId("proverId");
        String projectId = "project";
        String workSpace = "workspace";
        when(userProviderMapper.selectUserDataByName(projectId, workSpace, "import-default"))
            .thenReturn(Collections.singletonList(modelServiceProvider));
        when(providerMgmtService.createModelServiceProviders(anyString(), anyString(),
            any(ModelServiceProviderReq.class))).thenReturn("proverId");
        String provider = agentImportExportService.createProvider(projectId, workSpace);
        assertEquals("proverId", provider);
    }

    @Test
    void TestParseModelId() throws IOException {
        final WorkflowVO workflowVO =
            TestUtil.getJsonObject(WorkflowVO.class, "classpath:flow_dsl/test_workflow_detection_dsl.json");
        WorkflowExportEntity workflowExportEntity = new WorkflowExportEntity();
        workflowExportEntity.setDsl(workflowVO);
        Set<String> modelIds = agentImportExportService.parseModelId(workflowExportEntity);
        assertEquals(2, modelIds.size());
    }

    @Test
    public void testProcessControllerModelWithValidModel() {
        // 模拟数据
        String projectId = "test-project";
        String targetWorkspaceId = "test-workspace";

        // 创建 AgentExportEntity
        AgentExportEntity agentExportEntity = new AgentExportEntity();
        ControllerVO dsl = new ControllerVO();
        List<ControllerNodeVO> nodes = new ArrayList<>();

        // 创建一个有效的 ControllerNodeVO
        ControllerNodeVO controllerNodeVO = new ControllerNodeVO();
        controllerNodeVO.setType(CommonConstant.ControllerConstants.CONTROLLER_TYPE);

        // 创建 ControllerNodeConfigVO
        ControllerNodeConfigVO configVO = new ControllerNodeConfigVO();
        ModelConfigVO modelConfigVO = new ModelConfigVO();
        modelConfigVO.setModelDeploymentId("valid-model-id");
        modelConfigVO.setModelName("valid-model-name");
        configVO.setModel(modelConfigVO);
        controllerNodeVO.setConfigs(configVO);

        nodes.add(controllerNodeVO);
        dsl.setNodes(nodes);
        agentExportEntity.setDsl(dsl);

        // 模拟 getModelIdInDatabase 返回与 modelDeploymentId 匹配的值
        when(agentImportExportService.getModelIdInDatabase(projectId, targetWorkspaceId, "valid-model-id",
            "valid-model-name")).thenReturn("valid-model-id");

        // 调用方法
        agentImportExportService.processControllerModel(projectId, targetWorkspaceId, agentExportEntity);
        ControllerNodeConfigVO controllerNodeConfigVO =
            JsonUtils.objectToClassRef(controllerNodeVO.getConfigs(), new TypeReference<ControllerNodeConfigVO>() {});
        assertEquals(controllerNodeConfigVO.getModel().getModelDeploymentId(), "valid-model-id");
        assertEquals(controllerNodeConfigVO.getModel().getModelName(), "valid-model-name");
    }

    @Test
    public void testProcessControllerModelWithModelIdMismatch() {
        String projectId = "test-project";
        String targetWorkspaceId = "test-workspace";

        // 创建 AgentExportEntity
        AgentExportEntity agentExportEntity = new AgentExportEntity();
        ControllerVO dsl = new ControllerVO();
        List<ControllerNodeVO> nodes = new ArrayList<>();

        // 创建一个 ControllerNodeVO
        ControllerNodeVO controllerNodeVO = new ControllerNodeVO();
        controllerNodeVO.setType(CommonConstant.ControllerConstants.CONTROLLER_TYPE);

        // 创建 ControllerNodeConfigVO
        ControllerNodeConfigVO configVO = new ControllerNodeConfigVO();
        ModelConfigVO modelConfigVO = new ModelConfigVO();
        modelConfigVO.setModelDeploymentId("invalid-model-id");
        modelConfigVO.setModelName("valid-model-name");
        configVO.setModel(modelConfigVO);

        controllerNodeVO.setConfigs(configVO);

        nodes.add(controllerNodeVO);
        dsl.setNodes(nodes);
        agentExportEntity.setDsl(dsl);

        // 模拟 getModelIdInDatabase 返回一个不同的 modelId
        when(agentImportExportService.getModelIdInDatabase(projectId, targetWorkspaceId, "invalid-model-id",
            "valid-model-name")).thenReturn("valid-model-id");

        // 调用方法
        agentImportExportService.processControllerModel(projectId, targetWorkspaceId, agentExportEntity);

        // 验证 configs 被更新
        ControllerNodeConfigVO updatedConfig =
            JsonUtils.objectToClassRef(controllerNodeVO.getConfigs(), new TypeReference<ControllerNodeConfigVO>() {});
        assertEquals("valid-model-id", updatedConfig.getModel().getModelDeploymentId());
    }

    @Test
    public void testProcessControllerModelWithModelIdNotFound() {
        String projectId = "test-project";
        String targetWorkspaceId = "test-workspace";
        AgentExportEntity agentExportEntity = new AgentExportEntity();
        ControllerVO dsl = new ControllerVO();
        List<ControllerNodeVO> nodes = new ArrayList<>();

        // 创建一个模型节点
        ControllerNodeVO controllerNodeVO = new ControllerNodeVO();
        controllerNodeVO.setType(CommonConstant.ControllerConstants.CONTROLLER_TYPE);
        ControllerNodeConfigVO configVO = new ControllerNodeConfigVO();
        ModelConfigVO modelConfigVO = new ModelConfigVO();
        modelConfigVO.setModelDeploymentId("non-existent-id");
        modelConfigVO.setModelName("non-existent-name");
        configVO.setModel(modelConfigVO);
        controllerNodeVO.setConfigs(configVO);

        nodes.add(controllerNodeVO);
        dsl.setNodes(nodes);
        agentExportEntity.setDsl(dsl);

        // 模拟 getModelIdInDatabase 返回空
        when(agentImportExportService.getModelIdInDatabase(projectId, targetWorkspaceId, "non-existent-id",
            "non-existent-name")).thenReturn(null);

        // 调用方法
        agentImportExportService.processControllerModel(projectId, targetWorkspaceId, agentExportEntity);

        // 验证 configs 被更新
        ControllerNodeConfigVO updatedConfig =
            JsonUtils.objectToClassRef(controllerNodeVO.getConfigs(), new TypeReference<ControllerNodeConfigVO>() {});
        assertEquals("non-existent-id", updatedConfig.getModel().getModelDeploymentId());
    }

    @Test
    public void testProcessControllerModelWithModelIdNewId() {
        // 模拟数据
        String projectId = "test-project";
        String targetWorkspaceId = "test-workspace";
        AgentExportEntity agentExportEntity = new AgentExportEntity();
        ControllerVO dsl = new ControllerVO();
        List<ControllerNodeVO> nodes = new ArrayList<>();

        // 创建一个模型节点
        ControllerNodeVO controllerNodeVO = new ControllerNodeVO();
        controllerNodeVO.setType(CommonConstant.ControllerConstants.CONTROLLER_TYPE);
        ControllerNodeConfigVO configVO = new ControllerNodeConfigVO();
        ModelConfigVO modelConfigVO = new ModelConfigVO();
        modelConfigVO.setModelDeploymentId("new-id");
        modelConfigVO.setModelName("new-name");
        configVO.setModel(modelConfigVO);
        controllerNodeVO.setConfigs(configVO);

        nodes.add(controllerNodeVO);
        dsl.setNodes(nodes);
        agentExportEntity.setDsl(dsl);

        // 模拟 getModelIdInDatabase 返回空值
        when(agentImportExportService.getModelIdInDatabase(projectId, targetWorkspaceId, "new-id", "new-name"))
            .thenReturn(null);

        // 模拟 createProvider 返回 providerId
        when(agentImportExportService.createProvider(projectId, targetWorkspaceId)).thenReturn("provider-123");

        // 调用方法
        agentImportExportService.processControllerModel(projectId, targetWorkspaceId, agentExportEntity);
        ControllerNodeConfigVO updatedConfig = JsonUtils.objectToClassRef(
            agentExportEntity.getDsl().getNodes().get(0).getConfigs(), new TypeReference<ControllerNodeConfigVO>() {});
        assertNotNull(updatedConfig.getModel());
        assertEquals("new-id", updatedConfig.getModel().getModelDeploymentId());
    }

    @Test
    public void testProcessControllerModelWithModelIdAsUUID() {
        String projectId = "test-project";
        String targetWorkspaceId = "test-workspace";

        // 创建 AgentExportEntity
        AgentExportEntity agentExportEntity = new AgentExportEntity();
        ControllerVO dsl = new ControllerVO();
        List<ControllerNodeVO> nodes = new ArrayList<>();

        // 创建一个 ControllerNodeVO
        ControllerNodeVO controllerNodeVO = new ControllerNodeVO();
        controllerNodeVO.setType(CommonConstant.ControllerConstants.CONTROLLER_TYPE);

        // 创建 ControllerNodeConfigVO
        ControllerNodeConfigVO configVO = new ControllerNodeConfigVO();
        ModelConfigVO modelConfigVO = new ModelConfigVO();
        modelConfigVO.setModelDeploymentId("mockid");
        modelConfigVO.setModelName("uuid-model");
        configVO.setModel(modelConfigVO);
        controllerNodeVO.setConfigs(configVO);

        nodes.add(controllerNodeVO);
        dsl.setNodes(nodes);
        agentExportEntity.setDsl(dsl);

        // 模拟 getModelIdInDatabase 返回 "uuid"
        when(agentImportExportService.getModelIdInDatabase(projectId, targetWorkspaceId, "mockid", "uuid-model"))
            .thenReturn("uuid");

        // 调用方法
        agentImportExportService.processControllerModel(projectId, targetWorkspaceId, agentExportEntity);

        // 验证 configs 被更新
        ControllerNodeConfigVO updatedConfig =
            JsonUtils.objectToClassRef(controllerNodeVO.getConfigs(), new TypeReference<ControllerNodeConfigVO>() {});
        assertNotEquals("uuid", updatedConfig.getModel().getModelDeploymentId());
        assertNotEquals("mockid", updatedConfig.getModel().getModelDeploymentId());
    }

    @Test
    public void testProcessControllerModelWithInvalidModel() {
        String projectId = "test-project";
        String targetWorkspaceId = "test-workspace";

        // 创建 AgentExportEntity
        AgentExportEntity agentExportEntity = new AgentExportEntity();
        ControllerVO dsl = new ControllerVO();
        List<ControllerNodeVO> nodes = new ArrayList<>();

        // 创建一个 ControllerNodeVO
        ControllerNodeVO controllerNodeVO = new ControllerNodeVO();
        controllerNodeVO.setType(CommonConstant.ControllerConstants.CONTROLLER_TYPE);

        // 创建 ControllerNodeConfigVO
        ControllerNodeConfigVO configVO = new ControllerNodeConfigVO();
        ModelConfigVO modelConfigVO = new ModelConfigVO();
        modelConfigVO.setModelDeploymentId("invalid-id");
        modelConfigVO.setModelName("invalid-name");
        configVO.setModel(modelConfigVO);

        controllerNodeVO.setConfigs(configVO);

        nodes.add(controllerNodeVO);
        dsl.setNodes(nodes);
        agentExportEntity.setDsl(dsl);

        // 模拟 getModelIdInDatabase 返回空
        when(agentImportExportService.getModelIdInDatabase(projectId, targetWorkspaceId, "invalid-id", "invalid-name"))
            .thenReturn(null);

        // 调用方法
        agentImportExportService.processControllerModel(projectId, targetWorkspaceId, agentExportEntity);

        // 验证 configs 被更新
        ControllerNodeConfigVO updatedConfig =
            JsonUtils.objectToClassRef(controllerNodeVO.getConfigs(), new TypeReference<ControllerNodeConfigVO>() {});
        assertEquals("invalid-id", updatedConfig.getModel().getModelDeploymentId());
    }

    @Test
    public void testProcessControllerModelWithEmptyNodes() {
        // 模拟数据
        String projectId = "test-project";
        String targetWorkspaceId = "test-workspace";
        AgentExportEntity agentExportEntity = new AgentExportEntity();
        ControllerVO dsl = new ControllerVO();
        dsl.setNodes(new ArrayList<>());
        agentExportEntity.setDsl(dsl);

        // 调用方法
        agentImportExportService.processControllerModel(projectId, targetWorkspaceId, agentExportEntity);

        // 验证
        assertTrue(agentExportEntity.getDsl().getNodes().isEmpty());
    }

    @Test
    public void testProcessControllerModelWithEmptyConfig() {
        // 模拟数据
        String projectId = "test-project";
        String targetWorkspaceId = "test-workspace";
        AgentExportEntity agentExportEntity = new AgentExportEntity();
        ControllerVO dsl = new ControllerVO();
        List<ControllerNodeVO> nodes = new ArrayList<>();

        // 创建一个模型节点，但配置为空
        ControllerNodeVO controllerNodeVO = new ControllerNodeVO();
        controllerNodeVO.setType(CommonConstant.ControllerConstants.CONTROLLER_TYPE);
        controllerNodeVO.setConfigs(null);

        nodes.add(controllerNodeVO);
        dsl.setNodes(nodes);
        agentExportEntity.setDsl(dsl);

        // 调用方法
        agentImportExportService.processControllerModel(projectId, targetWorkspaceId, agentExportEntity);

        // 验证
        assertNull(controllerNodeVO.getConfigs());
    }

    @Test
    public void testGetModelIdInDatabase_ModelServiceExists() {
        // Arrange
        String projectId = "projectId";
        String workspaceId = "workspaceId";
        String modelId = "modelId";
        String modelName = "modelName";
        ModelServiceBase modelServiceBase = new ModelServiceBase();
        modelServiceBase.setId(modelId);
        modelServiceBase.setProjectId(projectId);
        modelServiceBase.setWorkspaceId(workspaceId);

        when(modelServiceMapper.queryById(modelId)).thenReturn(modelServiceBase);
        when(freeModelServiceMapper.countByModelId(modelId)).thenReturn(0);

        // Act
        String result = agentImportExportService.getModelIdInDatabase(projectId, workspaceId, modelId, modelName);

        // Assert
        assertEquals(modelId, result);
    }

    @Test
    public void testGetModelIdInDatabase_ModelServiceExistsByNameInSystem() {
        // Arrange
        String projectId = "projectId";
        String workspaceId = "workspaceId";
        String modelId = "modelId";
        String modelName = "modelName";
        ModelServiceBase modelServiceBase = new ModelServiceBase();
        modelServiceBase.setId(modelId);
        modelServiceBase.setProjectId(projectId);
        modelServiceBase.setWorkspaceId("iuuid");

        when(modelServiceMapper.queryById(modelId)).thenReturn(modelServiceBase);
        when(freeModelServiceMapper.countByModelId(modelId)).thenReturn(0);

        List<ModelServiceBase> existMSBySystem = Arrays.asList(new ModelServiceBase());
        when(modelServiceMapper.queryByName(CommonConstant.SYSTEM_DEFAULT, CommonConstant.SYSTEM_DEFAULT, modelName,existMSBySystem.get(0).getProviderId()))
            .thenReturn(existMSBySystem);

        // Act
        String result = agentImportExportService.getModelIdInDatabase(projectId, workspaceId, modelId, modelName);

        // Assert
        assertEquals(existMSBySystem.get(0).getId(), result);
    }

    @Test
    public void testGetModelIdInDatabase_ModelServiceExistsByName() {
        // Arrange
        String projectId = "projectId";
        String workspaceId = "workspaceId";
        String modelId = "modelId";
        String modelName = "modelName";
        ModelServiceBase modelServiceBase = new ModelServiceBase();
        modelServiceBase.setId("uuid");
        modelServiceBase.setProjectId("uuid");

        when(modelServiceMapper.queryById(modelId)).thenReturn(modelServiceBase);
        when(freeModelServiceMapper.countByModelId(modelId)).thenReturn(0);

        when(modelServiceMapper.queryByName(CommonConstant.SYSTEM_DEFAULT, CommonConstant.SYSTEM_DEFAULT, modelName,null))
            .thenReturn(Collections.emptyList());

        List<ModelServiceBase> existMS = Arrays.asList(new ModelServiceBase());
        when(modelServiceMapper.queryByName(projectId, workspaceId, modelName,null)).thenReturn(existMS);

        // Act
        String result = agentImportExportService.getModelIdInDatabase(projectId, workspaceId, modelId, modelName);

        // Assert
        assertEquals(existMS.get(0).getId(), result);
    }

    @Test
    public void testGetModelIdInDatabase_ModelServiceDoesNotExist() {
        // Arrange
        String projectId = "projectId";
        String workspaceId = "workspaceId";
        String modelId = "modelId";
        String modelName = "modelName";
        ModelServiceBase modelServiceBase = new ModelServiceBase();
        modelServiceBase.setId(modelId);
        modelServiceBase.setProjectId(projectId);
        modelServiceBase.setWorkspaceId("uuid");

        when(modelServiceMapper.queryById(modelId)).thenReturn(modelServiceBase);
        when(freeModelServiceMapper.countByModelId(modelId)).thenReturn(0);
        when(modelServiceMapper.queryByName(CommonConstant.SYSTEM_DEFAULT, CommonConstant.SYSTEM_DEFAULT, modelName,null))
            .thenReturn(Collections.emptyList());
        when(modelServiceMapper.queryByName(projectId, workspaceId, modelName,null)).thenReturn(Collections.emptyList());

        // Act
        String result = agentImportExportService.getModelIdInDatabase(projectId, workspaceId, modelId, modelName);

        // Assert
        assertEquals("uuid", result);
    }

    @Test
    public void testGetModelIdInDatabase_ModelServiceIsNull() {
        // Arrange
        String projectId = "projectId";
        String workspaceId = "workspaceId";
        String modelId = "modelId";
        String modelName = "modelName";

        when(modelServiceMapper.queryById(modelId)).thenReturn(null);
        when(freeModelServiceMapper.countByModelId(modelId)).thenReturn(0);
        when(modelServiceMapper.queryByName(CommonConstant.SYSTEM_DEFAULT, CommonConstant.SYSTEM_DEFAULT, modelName,null))
            .thenReturn(Collections.emptyList());
        when(modelServiceMapper.queryByName(projectId, workspaceId, modelName,null)).thenReturn(Collections.emptyList());

        String result = agentImportExportService.getModelIdInDatabase(projectId, workspaceId, modelId, modelName);

        assertEquals(null, result);
    }

    @Test
    public void testImportModelByFlow_NoModels() {
        // 测试没有模型的情况
        Map<String, String> configMap = new HashMap<>();
        WorkflowExportEntity workflowExportEntity = generateWorkFlow(configMap);
        String projectId = "projectId";
        String workspaceId = "workspaceId";
        Map<String, String> replaceModelIdRecord = new HashMap<>();

        agentImportExportService.importModelByFlow(projectId, workflowExportEntity, workspaceId, replaceModelIdRecord);

        // 验证replaceModelIdRecord是否为空
        assertTrue(replaceModelIdRecord.isEmpty());
    }

    @Test
    public void testImportModelByFlow_ModelWithNoName() {
        // 测试模型没有名称的情况
        Map<String, Map<String, String>> models = new HashMap<>();
        Map<String, String> modelMap = new HashMap<>();
        modelMap.put("modelId", "modelId");
        models.put("modelId", modelMap);
        WorkflowExportEntity workflowExportEntity = generateWorkFlow(modelMap);

        String projectId = "projectId";
        String workspaceId = "workspaceId";
        Map<String, String> replaceModelIdRecord = new HashMap<>();

        agentImportExportService.importModelByFlow(projectId, workflowExportEntity, workspaceId, replaceModelIdRecord);

        // 验证replaceModelIdRecord是否为空
        assertTrue(replaceModelIdRecord.isEmpty());
    }

    @Test
    public void testImportModelByFlow_ModelWithExistingId() {
        Map<String, Map<String, String>> models = new HashMap<>();
        Map<String, String> modelMap = new HashMap<>();
        modelMap.put(CommonConstant.ModelParam.MODEL_NAME, "modelName");
        modelMap.put(CommonConstant.ModelParam.MODEL_DEPLOYMENT_ID, "modelId");
        models.put("modelId", modelMap);
        WorkflowExportEntity workflowExportEntity = generateWorkFlow(modelMap);

        String projectId = "projectId";
        String workspaceId = "workspaceId";
        Map<String, String> replaceModelIdRecord = new HashMap<>();

        // 模拟数据库中的模型ID
        when(agentImportExportService.getModelIdInDatabase(projectId, workspaceId, "modelId", "modelName"))
            .thenReturn("existingModelId");

        agentImportExportService.importModelByFlow(projectId, workflowExportEntity, workspaceId, replaceModelIdRecord);

        // 验证replaceModelIdRecord是否包含已存在的模型ID
        assertEquals("existingModelId", replaceModelIdRecord.get("modelId"));
    }

    @Test
    public void testImportModelByFlow_ModelWithNewId() {
        Map<String, Map<String, String>> models = new HashMap<>();
        Map<String, String> modelMap = new HashMap<>();
        modelMap.put(CommonConstant.ModelParam.MODEL_NAME, "modelName");
        modelMap.put(CommonConstant.ModelParam.MODEL_DEPLOYMENT_ID, "modelId");
        models.put("modelId", modelMap);
        WorkflowExportEntity workflowExportEntity = generateWorkFlow(modelMap);

        String projectId = "projectId";
        String workspaceId = "workspaceId";
        Map<String, String> replaceModelIdRecord = new HashMap<>();

        // 模拟数据库中的模型ID为空
        when(agentImportExportService.getModelIdInDatabase(projectId, workspaceId, "modelId", "modelName"))
            .thenReturn(null);

        agentImportExportService.importModelByFlow(projectId, workflowExportEntity, workspaceId, replaceModelIdRecord);

        // 无需替换
        String newModelId = replaceModelIdRecord.get("modelId");
        assertEquals(modelMap.get(CommonConstant.ModelParam.MODEL_NAME), "modelName");
    }

    @Test
    public void testImportModelByFlow_ModelWithUUIDId() {
        // 测试模型ID为UUID的情况
        Map<String, Map<String, String>> models = new HashMap<>();
        Map<String, String> modelMap = new HashMap<>();
        modelMap.put(CommonConstant.ModelParam.MODEL_NAME, "modelName");
        modelMap.put(CommonConstant.ModelParam.MODEL_DEPLOYMENT_ID, "modelId");
        models.put("modelId", modelMap);
        WorkflowExportEntity workflowExportEntity = generateWorkFlow(modelMap);

        String projectId = "projectId";
        String workspaceId = "workspaceId";
        Map<String, String> replaceModelIdRecord = new HashMap<>();

        // 模拟数据库中的模型ID为UUID
        when(agentImportExportService.getModelIdInDatabase(projectId, workspaceId, "uuid", "modelName"))
            .thenReturn("uuid");

        agentImportExportService.importModelByFlow(projectId, workflowExportEntity, workspaceId, replaceModelIdRecord);

        // 验证replaceModelIdRecord是否包含新创建的模型ID
        String newModelId = replaceModelIdRecord.get("modelId");
        assertNotNull(newModelId);
        assertEquals(modelMap.get(CommonConstant.ModelParam.MODEL_NAME), "modelName");
    }

    private WorkflowExportEntity generateWorkFlow(Map<String, String> modelMap) {
        WorkflowExportEntity workflowExportEntity = new WorkflowExportEntity();
        WorkflowVO dsl = new WorkflowVO();
        List<WorkflowNodeVO> nodes = new ArrayList<WorkflowNodeVO>();
        WorkflowNodeVO workflowNodeVO = new WorkflowNodeVO();
        workflowNodeVO.setType(NodeType.LLM.getType());
        Map<String, Object> configs = new HashMap<>();
        configs.put(CommonConstant.ModelParam.MODEL, modelMap);
        workflowNodeVO.setConfigs(configs);
        nodes.add(workflowNodeVO);
        dsl.setNodes(nodes);
        workflowExportEntity.setDsl(dsl);
        return workflowExportEntity;
    }

    @Test
    public void testImportModeByAgentWithBlankModelId() {
        // 测试模型ID为空的情况
        Agent agent = new Agent();
        agent.setModelDeploymentId("");
        agent.setModelName("TestModel");
        Agent result = agentImportExportService.importModeByAgent("projectId", agent, "workspaceId");
        assertEquals("", result.getModelDeploymentId());
        assertEquals("TestModel", result.getModelName());
    }

    @Test
    public void testImportModeByAgentWithBlankModelName() {
        // 测试模型名称为空的情况
        Agent agent = new Agent();
        agent.setModelDeploymentId("TestModelId");
        agent.setModelName("");
        Agent result = agentImportExportService.importModeByAgent("projectId", agent, "workspaceId");
        assertEquals("TestModelId", result.getModelDeploymentId());
        assertEquals("", result.getModelName());
    }

    @Test
    public void testImportModeByAgentWithModelIdInDb() {
        // 测试模型ID在数据库中存在的情况
        Agent agent = new Agent();
        agent.setModelDeploymentId("TestModelId");
        agent.setModelName("TestModel");
        when(agentImportExportService.getModelIdInDatabase("projectId", "workspaceId", "TestModelId", "TestModel"))
            .thenReturn("TestModelId");
        Agent result = agentImportExportService.importModeByAgent("projectId", agent, "workspaceId");
        assertEquals("TestModelId", result.getModelDeploymentId());
        assertEquals("TestModel", result.getModelName());
    }

    @Test
    public void testImportModeByAgentWithModelIdNotInDb() {
        // 测试模型ID在数据库中不存在的情况
        Agent agent = new Agent();
        agent.setModelDeploymentId("TestModelId");
        agent.setModelName("TestModel");
        when(agentImportExportService.getModelIdInDatabase("projectId", "workspaceId", "TestModelId", "TestModel"))
            .thenReturn("");
        Agent result = agentImportExportService.importModeByAgent("projectId", agent, "workspaceId");
        assertEquals("TestModelId", result.getModelDeploymentId());
        assertEquals("TestModel", result.getModelName());
    }

    @Test
    public void testImportModeByAgentWithModelIdInDbAsUuid() {
        // 测试模型ID在数据库中为UUID的情况
        Agent agent = new Agent();
        agent.setModelDeploymentId("TestModelId");
        agent.setModelName("TestModel");
        when(agentImportExportService.getModelIdInDatabase("projectId", "workspaceId", "TestModelId", "TestModel"))
            .thenReturn("uuid");
        Agent result = agentImportExportService.importModeByAgent("projectId", agent, "workspaceId");
        assertNotEquals("uuid", result.getModelDeploymentId());
        assertNotEquals("TestModelId", result.getModelDeploymentId());
        assertEquals("TestModel", result.getModelName());
    }

    @Test
    public void testImportModeByAgentWithModelNotExist() {
        // 测试模型不存在的情况
        Agent agent = new Agent();
        agent.setModelDeploymentId("TestModelId");
        agent.setModelName("TestModel");
        when(agentImportExportService.getModelIdInDatabase("projectId", "workspaceId", "TestModelId", "TestModel"))
            .thenReturn("");
        Agent result = agentImportExportService.importModeByAgent("projectId", agent, "workspaceId");
        assertEquals("TestModelId", result.getModelDeploymentId());
        assertEquals("TestModel", result.getModelName());
    }

    @Test
    public void testProcessDefaultModel_WhenDefaultModelNotPresent() {
        WorkflowExportEntity workflowExportEntity = generateDefaultModel(null);
        // Arrange
        workflowExportEntity.getDsl().getConfigs().remove(CommonConstant.ModelParam.DEFAULT_MODEL);

        // Act
        WorkflowExportEntity result =
            agentImportExportService.processDefaultModel(workflowExportEntity, "projectId", "workspaceId");

        // Assert
        assertEquals(workflowExportEntity, result);
    }

    @Test
    public void testProcessDefaultModel_WhenDefaultModelIsEmpty() {
        WorkflowExportEntity workflowExportEntity = generateDefaultModel(new HashMap<>());

        // Act
        WorkflowExportEntity result =
            agentImportExportService.processDefaultModel(workflowExportEntity, "projectId", "workspaceId");

        // Assert
        assertEquals(workflowExportEntity, result);
    }

    @Test
    public void testProcessDefaultModel_WhenModelIdOrModelNameIsBlank() {
        // Arrange
        Map<String, String> defaultMode = new HashMap<>();
        defaultMode.put(CommonConstant.ModelParam.MODEL_DEPLOYMENT_ID, "modelId");
        defaultMode.put(CommonConstant.ModelParam.MODEL_NAME, "");
        WorkflowExportEntity workflowExportEntity = generateDefaultModel(defaultMode);

        // Act
        WorkflowExportEntity result =
            agentImportExportService.processDefaultModel(workflowExportEntity, "projectId", "workspaceId");

        // Assert
        assertEquals(workflowExportEntity, result);
    }

    @Test
    public void testProcessDefaultModel_WhenModelIdMatchesInDatabase() {
        // Arrange
        Map<String, String> defaultMode = new HashMap<>();
        defaultMode.put(CommonConstant.ModelParam.MODEL_DEPLOYMENT_ID, "modelId");
        defaultMode.put(CommonConstant.ModelParam.MODEL_NAME, "modelName");
        WorkflowExportEntity workflowExportEntity = generateDefaultModel(defaultMode);

        when(agentImportExportService.getModelIdInDatabase("projectId", "workspaceId", "modelId", "modelName"))
            .thenReturn("modelId");

        // Act
        WorkflowExportEntity result =
            agentImportExportService.processDefaultModel(workflowExportEntity, "projectId", "workspaceId");

        Map<String, String> mode =
            JsonUtils.objectToClass(result.getDsl().getConfigs().get(CommonConstant.ModelParam.DEFAULT_MODEL));
        // Assert
        assertEquals(defaultMode.get(CommonConstant.ModelParam.MODEL_DEPLOYMENT_ID),
            mode.get(CommonConstant.ModelParam.MODEL_DEPLOYMENT_ID));
    }

    @Test
    public void testProcessDefaultModel_WhenModelIdDoesNotMatchInDatabase() {
        // Arrange
        Map<String, String> defaultMode = new HashMap<>();
        defaultMode.put(CommonConstant.ModelParam.MODEL_DEPLOYMENT_ID, "modelId");
        defaultMode.put(CommonConstant.ModelParam.MODEL_NAME, "modelName");
        WorkflowExportEntity workflowExportEntity = generateDefaultModel(defaultMode);

        when(agentImportExportService.getModelIdInDatabase("projectId", "workspaceId", "modelId", "modelName"))
            .thenReturn("differentModelId");

        // Act
        WorkflowExportEntity result =
            agentImportExportService.processDefaultModel(workflowExportEntity, "projectId", "workspaceId");
        Map<String, String> mode =
            JsonUtils.objectToClass(result.getDsl().getConfigs().get(CommonConstant.ModelParam.DEFAULT_MODEL));
        // Assert
        assertNotEquals(defaultMode.get(CommonConstant.ModelParam.MODEL_DEPLOYMENT_ID),
            mode.get(CommonConstant.ModelParam.MODEL_DEPLOYMENT_ID));
        assertEquals("differentModelId", mode.get(CommonConstant.ModelParam.MODEL_DEPLOYMENT_ID));
    }

    @Test
    public void testProcessDefaultModel_WhenModelIdInDatabaseIsBlank() {
        // Arrange
        Map<String, String> defaultMode = new HashMap<>();
        defaultMode.put(CommonConstant.ModelParam.MODEL_DEPLOYMENT_ID, "modelId");
        defaultMode.put(CommonConstant.ModelParam.MODEL_NAME, "modelName");
        WorkflowExportEntity workflowExportEntity = generateDefaultModel(defaultMode);
        when(agentImportExportService.getModelIdInDatabase("projectId", "workspaceId", "modelId", "modelName"))
            .thenReturn("");

        // Act
        WorkflowExportEntity result =
            agentImportExportService.processDefaultModel(workflowExportEntity, "projectId", "workspaceId");
        Map<String, String> mode =
            JsonUtils.objectToClass(result.getDsl().getConfigs().get(CommonConstant.ModelParam.DEFAULT_MODEL));
        // Assert
        assertEquals(defaultMode.get(CommonConstant.ModelParam.MODEL_DEPLOYMENT_ID),
            mode.get(CommonConstant.ModelParam.MODEL_DEPLOYMENT_ID));
    }

    @Test
    public void testProcessDefaultModel_WhenModelIdInDatabaseIsUuid() {
        // Arrange
        Map<String, String> defaultMode = new HashMap<>();
        defaultMode.put(CommonConstant.ModelParam.MODEL_DEPLOYMENT_ID, "modelId");
        defaultMode.put(CommonConstant.ModelParam.MODEL_NAME, "modelName");
        WorkflowExportEntity workflowExportEntity = generateDefaultModel(defaultMode);
        when(agentImportExportService.getModelIdInDatabase("projectId", "workspaceId", "modelId", "modelName"))
            .thenReturn("uuid");

        // Act
        WorkflowExportEntity result =
            agentImportExportService.processDefaultModel(workflowExportEntity, "projectId", "workspaceId");
        Map<String, String> mode =
            JsonUtils.objectToClass(result.getDsl().getConfigs().get(CommonConstant.ModelParam.DEFAULT_MODEL));
        // Assert
        assertNotEquals(defaultMode.get(CommonConstant.ModelParam.MODEL_DEPLOYMENT_ID),
            mode.get(CommonConstant.ModelParam.MODEL_DEPLOYMENT_ID));
    }

    @Test
    public void testProcessDefaultModel_WhenModelDoesNotExist() {
        // Arrange
        Map<String, String> defaultMode = new HashMap<>();
        defaultMode.put(CommonConstant.ModelParam.MODEL_DEPLOYMENT_ID, "modelId");
        defaultMode.put(CommonConstant.ModelParam.MODEL_NAME, "modelName");
        WorkflowExportEntity workflowExportEntity = generateDefaultModel(defaultMode);
        when(agentImportExportService.getModelIdInDatabase("projectId", "workspaceId", "modelId", "modelName"))
            .thenReturn(null);

        // Act
        WorkflowExportEntity result =
            agentImportExportService.processDefaultModel(workflowExportEntity, "projectId", "workspaceId");

        Map<String, String> mode =
            JsonUtils.objectToClass(result.getDsl().getConfigs().get(CommonConstant.ModelParam.DEFAULT_MODEL));

        assertNotNull(mode);
        // Assert
        assertEquals(defaultMode.get(CommonConstant.ModelParam.MODEL_DEPLOYMENT_ID),
            mode.get(CommonConstant.ModelParam.MODEL_DEPLOYMENT_ID));
    }

    private WorkflowExportEntity generateDefaultModel(Map<String, String> modelMap) {
        WorkflowExportEntity workflowExportEntity = new WorkflowExportEntity();
        WorkflowVO dsl = new WorkflowVO();
        Map<String, Object> configs = new HashMap<>();
        configs.put(CommonConstant.ModelParam.DEFAULT_MODEL, modelMap);
        dsl.setConfigs(configs);
        workflowExportEntity.setDsl(dsl);
        return workflowExportEntity;
    }

    @Test
    public void testCreateModel() {
        String projectId = "projectId";
        String workspaceId = "workspaceId";
        Map<String, String> modeConfig = new HashMap<>();
        modeConfig.put(CommonConstant.ModelParam.MODEL_NAME, "value");
        modeConfig.put(CommonConstant.ModelParam.MODEL_DEPLOYMENT_ID, "value");
        String providerId = "providerId";

        assertDoesNotThrow(() -> {
            agentImportExportService.createModel(projectId, workspaceId, modeConfig, providerId);
        });

    }

    @Test
    public void testCreateModelWithNullConfig() {
        String projectId = "projectId";
        String workspaceId = "workspaceId";
        String providerId = "providerId";

        assertThrows(NullPointerException.class, () -> {
            agentImportExportService.createModel(projectId, workspaceId, null, providerId);
        });
    }

    @Test
    public void testResolvePlugins_PluginNotExists() {
        // Given
        String projectId = "project1";
        List<ImportListInfo> importList = new ArrayList<>();
        PluginEntity plugin = new PluginEntity();
        plugin.setPluginId("pluginId");
        plugin.setPluginDisplayName("displayName");
        plugin.setPluginDesc("description");
        plugin.setType("TYPE");

        // Mock static method
        when(RequestContextUtils.getRequestWorkspaceId()).thenReturn("workspaceId");

        // Mock method call
        AgentImportExportService spyService = spy(agentImportExportService);
        doReturn(false).when(spyService).isPluginExistInWorkspace(projectId, "workspaceId", plugin);

        // When
        spyService.resolvePlugins(projectId, importList, plugin);

        // Then
        assertTrue(importList.size() == 1);
        ImportListInfo result = importList.get(0);
        assertTrue("pluginId".equals(result.getId()));
        assertTrue("displayName".equals(result.getName()));
        assertTrue("description".equals(result.getDescription()));
        assertTrue(CommonConstant.PLUGIN.equals(result.getType()));
    }

    @Test
    public void testIsPluginExistInWorkspace_NonINNERTypeNotExists() {
        assertThrows(NullPointerException.class, () -> {
            PluginEntity tool = new PluginEntity();
            tool.setType("not_inner");

            // When
            Boolean result = agentImportExportService.isPluginExistInWorkspace("project_id", "workspace_id", tool);
        });
    }
}
