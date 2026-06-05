/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service.plugin;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.redis.RedisClient;
import com.openjiuwen.studio.agent.common.redis.RedisLock;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.common.utils.RequestHeaderHolderUtils;
import com.openjiuwen.studio.agent.manager.constant.Constants;
import com.openjiuwen.studio.agent.common.dto.auth.AuthInfo;
import com.openjiuwen.studio.agent.common.dto.auth.AuthKeyInfo;
import com.openjiuwen.studio.agent.manager.dto.BaseResp;
import com.openjiuwen.studio.agent.manager.dto.BatchCreatePluginToolReq;
import com.openjiuwen.studio.agent.manager.dto.BatchCreatePluginToolRsp;
import com.openjiuwen.studio.agent.manager.dto.CommonDeleteRsp;
import com.openjiuwen.studio.agent.manager.dto.CreatePluginToolReq;
import com.openjiuwen.studio.agent.manager.dto.CreatePluginToolRsp;
import com.openjiuwen.studio.agent.manager.dto.CreateVersionReq;
import com.openjiuwen.studio.agent.manager.dto.ExportParams;
import com.openjiuwen.studio.agent.manager.dto.ExtraMsg;
import com.openjiuwen.studio.agent.manager.dto.GetPluginVersionQo;
import com.openjiuwen.studio.agent.common.dto.auth.HisIamInfo;
import com.openjiuwen.studio.agent.common.dto.auth.HisSgov;
import com.openjiuwen.studio.agent.manager.dto.ListPluginsQo;
import com.openjiuwen.studio.agent.manager.dto.ListToolsQo;
import com.openjiuwen.studio.agent.manager.dto.ModifyPluginReq;
import com.openjiuwen.studio.agent.manager.dto.ModifyPluginRsp;
import com.openjiuwen.studio.agent.manager.dto.ParsePluginReq;
import com.openjiuwen.studio.agent.common.dto.auth.PluginIAMAuthInfo;
import com.openjiuwen.studio.agent.manager.dto.PluginListRsp;
import com.openjiuwen.studio.agent.manager.dto.VersionInfo;
import com.openjiuwen.studio.agent.manager.dto.plugin.PluginDTO;
import com.openjiuwen.studio.agent.manager.dto.plugin.ToolInfo;
import com.openjiuwen.studio.agent.manager.dto.plugin.ToolRequestInfo;
import com.openjiuwen.studio.agent.manager.entity.ReleaseVersion;
import com.openjiuwen.studio.agent.manager.entity.ShareResourceEntity;
import com.openjiuwen.studio.agent.manager.entity.plugin.PluginEntity;
import com.openjiuwen.studio.agent.manager.entity.plugin.PluginExportEntity;
import com.openjiuwen.studio.agent.manager.mapper.DependencyMapper;
import com.openjiuwen.studio.agent.manager.mapper.MappingMapper;
import com.openjiuwen.studio.agent.manager.mapper.ReleaseVersionMapper;
import com.openjiuwen.studio.agent.manager.mapper.ToolCredentialMapper;
import com.openjiuwen.studio.agent.manager.mapper.plugin.PluginMapper;
import com.openjiuwen.studio.agent.manager.obs.MgObsService;
import com.openjiuwen.studio.agent.manager.service.ToolManagementService;
import com.openjiuwen.studio.agent.manager.service.plugin.impl.PluginBaseImpl;
import com.openjiuwen.studio.agent.manager.service.share.ShareInnerService;
import com.openjiuwen.studio.agent.manager.utils.BaseTest;
import com.openjiuwen.studio.agent.manager.utils.TestUtil;
import com.openjiuwen.studio.agent.manager.utils.WorkflowUtils;
import com.openjiuwen.studio.common.service.service.EncryptionAdapter;

import org.apache.commons.lang3.math.NumberUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Transactional
@MockitoSettings(strictness = Strictness.LENIENT)
public class PluginServiceTest extends BaseTest {
    private static MockedStatic<RequestContextUtils> mockedStatic;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private PluginMapper pluginMapper;

    private static MockedStatic<RequestHeaderHolderUtils> mockedHeaderStatic;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ObjectMapper jacksonObjectMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DependencyMapper dependencyMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private PluginService pluginServiceMock;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ReleaseVersionMapper releaseVersionMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShareInnerService shareInnerService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MappingMapper mappingMapper;

    @MockitoSpyBean
    private EncryptionAdapter encryptionAdapter;

    @Autowired
    private ToolManagementService toolManagementService;

    @Autowired
    private PluginService pluginService;

    @Autowired
    private PluginMapper pluginMapperAuto;

    @Mock
    private MgObsService obsService;

    @Mock
    private PluginBaseImpl pluginBase;

    @Mock
    private ToolCredentialMapper toolCredentialMapper;

    @Mock
    private RedisClient redisClient;

    @Mock
    private RedisLock redisLock;

    @BeforeAll
    public static void init() {
        RequestContextUtils.setRequestAuthTokenAndProjectId(Constants.TEST_TOKEN, Constants.TEST_PROJECT_ID);
        mockedStatic = Mockito.mockStatic(RequestContextUtils.class);
        mockedStatic.when(RequestContextUtils::getRequestUserName).thenReturn(Constants.TEST_CREATOR);
        mockedStatic.when(RequestContextUtils::getRequestUserId).thenReturn(Constants.TEST_CREATOR_ID);
        mockedHeaderStatic = Mockito.mockStatic(RequestHeaderHolderUtils.class);
        mockedHeaderStatic.when(RequestHeaderHolderUtils::getRequestLanguage).thenReturn("zh-CN");
    }

    @BeforeEach
    void setUp() throws IOException {
        ReflectionTestUtils.setField(pluginService, "mgObsService", obsService);
        ReflectionTestUtils.setField(toolManagementService, "obsService", obsService);
        ReflectionTestUtils.setField(pluginService, "pluginChoice", true);
        ReflectionTestUtils.setField(pluginService, "allowPluginCrossPermissionQuery", true);
        ReflectionTestUtils.setField(pluginService, "shareInnerService", shareInnerService);

        Mockito.doReturn("E1-{fake-uuid}-fake_encrypt_result").when(encryptionAdapter).encrypt(anyString(), any());

        Mockito.doReturn("{}").when(encryptionAdapter).decrypt(anyString());

        Mockito.doReturn("{}").when(encryptionAdapter).decrypt(anyString(), any());
    }

    @AfterAll
    static void end() {
        mockedStatic.close();
        mockedHeaderStatic.close();
    }

    @Test
    @Sql(scripts = {"classpath:sql/plugin/create_tool_db.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void test_createTool_succeed() throws IOException {
        // setup
        final CreatePluginToolReq body = TestUtil.getJsonObject(CreatePluginToolReq.class,
            "classpath:service/plugin/create_tool.json");

        // run the test
        CreatePluginToolRsp result = pluginService.createTool(Constants.TEST_PROJECT_ID, "default", body);

        // verify the results
        assertNotNull(result.getToolId());
    }

    @Test
    @Sql(scripts = {"classpath:sql/plugin/create_tool_db.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void test_delete_origin_tool_succeed() {

        BaseResp resp = pluginService.deleteTool(Constants.TEST_PROJECT_ID, "preset_tdfen", "1231", "default");

        assertNotNull(resp);
        assertEquals(200, resp.getCode());
        assertEquals("success", resp.getMessage());
    }

    @Test
    @Sql(scripts = {"classpath:sql/plugin/create_tool_db.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void test_modify_new_tool_succeed2() throws IOException {
        final CreatePluginToolReq body = TestUtil.getJsonObject(CreatePluginToolReq.class,
            "classpath:service/plugin/modify_tool.json");
        body.setPluginId("preset_testtest1hhh");
        BaseResp resp = pluginService.modifyTool(Constants.TEST_PROJECT_ID, "default",
            "438e8569-1801-41da-83db-3e0700dc9721", body);
        assertEquals(200, resp.getCode());
        assertEquals("success", resp.getMessage());
    }

    @Test
    @Sql(scripts = {"classpath:sql/plugin/create_tool_db.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void test_delete_new_tool_succeed() {

        BaseResp resp = pluginService.deleteTool(Constants.TEST_PROJECT_ID, "preset_tdfen2",
            "210fcc6d-ac3d-49b3-ba75-d27f1c935da9", "default");

        assertNotNull(resp);
        assertEquals(200, resp.getCode());
        assertEquals("success", resp.getMessage());
    }

    @Test
    @Sql(scripts = {"classpath:sql/plugin/create_tool_db.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void test_list_origin_tool_succeed() throws IOException {

        final ListToolsQo body = TestUtil.getJsonObject(ListToolsQo.class,
            "classpath:service/plugin/list_origin_tool_query_param.json");

        BaseResp resp = pluginService.listTools(Constants.TEST_PROJECT_ID, body);

        PluginDTO pluginDTO = TestUtil.getJsonObject(PluginDTO.class,
            "classpath:service/plugin/list_origin_tool_res.json");

        assertEquals(200, resp.getCode());
        assertEquals("success", resp.getMessage());

    }

    @Test
    @Sql(scripts = {"classpath:sql/plugin/create_tool_db.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void test_list_new_tool_succeed() throws IOException {

        final ListToolsQo body = TestUtil.getJsonObject(ListToolsQo.class,
            "classpath:service/plugin/list_new_tool_query_param.json");

        BaseResp resp = pluginService.listTools(Constants.TEST_PROJECT_ID, body);

        PluginDTO pluginDTO = TestUtil.getJsonObject(PluginDTO.class,
            "classpath:service/plugin/list_new_tool_res.json");

        assertEquals(200, resp.getCode());
        assertEquals("success", resp.getMessage());
        String expectedJson = objectMapper.writeValueAsString(pluginDTO);
        String actualJson = objectMapper.writeValueAsString(resp.getData());
    }

    @Test
    @Sql(scripts = {"classpath:sql/plugin/create_tool_db.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void test_modify_new_tool_succeed() throws IOException {
        final CreatePluginToolReq body = TestUtil.getJsonObject(CreatePluginToolReq.class,
            "classpath:service/plugin/modify_tool.json");
        BaseResp resp = pluginService.modifyTool(Constants.TEST_PROJECT_ID, "default",
            "438e8569-1801-41da-83db-3e0700dc9721", body);

        assertEquals(200, resp.getCode());
        assertEquals("success", resp.getMessage());
    }




    @Test
    @Sql(scripts = {"classpath:sql/plugin/create_tool_db.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void test_create_tool_openapi_should_succeed_when_tool_exist() throws IOException {
        when(obsService.uploadObsFile(anyString(), anyString(), anyInt())).thenReturn("mock_file_path");
        BaseResp resp = pluginService.createToolOpenAPIById(Constants.TEST_PROJECT_ID, "default", "preset_tdfen2",
            "438e8569-1801-41da-83db-3e0700dc9721");
        assertEquals(200, resp.getCode());
        assertEquals("success", resp.getMessage());
        assertEquals("preset_tdfen2#438e8569-1801-41da-83db-3e0700dc9721", resp.getData());
    }

    @Test
    void test_create_tool_openapi_should_throw_agentManagerException_when_tool_not_exist() {
        assertThrows(AgentStudioException.class,
            () -> pluginService.createToolOpenAPIById(Constants.TEST_PROJECT_ID, "default", "preset_tdfen2",
                "438e8569-1801-41da-83db-3e0700dc9721"));
    }

    @Test
    @Sql(scripts = {"classpath:sql/plugin/create_plugin_db.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void test_createPlugin() throws IOException {
        final CreatePluginToolReq body = TestUtil.getJsonObject(CreatePluginToolReq.class,
            "classpath:service/plugin/create_plugin_tool_req.json");
        CreatePluginToolRsp result = pluginService.createPlugin(Constants.TEST_PROJECT_ID, "default", body);
        assertNotNull(result.getToolId());
    }

    @Test
    @Sql(scripts = {"classpath:sql/plugin/create_plugin_db.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void test_modifyPlugin() throws IOException {
        final ModifyPluginReq body = TestUtil.getJsonObject(ModifyPluginReq.class,
            "classpath:service/plugin/create_plugin_tool_req.json");
        ModifyPluginRsp result = pluginService.modifyPlugin(Constants.TEST_PROJECT_ID, "default", "preset_test", body);
    }

    @Test
    @Sql(scripts = {"classpath:sql/plugin/create_plugin_empty_db.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void test_deletePlugin_success_failture() throws IOException {
        CommonDeleteRsp result = pluginService.deletePlugin(Constants.TEST_PROJECT_ID, "preset_test", "default");
        assertNotNull(result);
    }

    @Test
    @Sql(scripts = {"classpath:sql/plugin/create_plugin_empty_db.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void test_deletePlugin_hard_delete() throws Exception {
        // Given
        ReflectionTestUtils.setField(pluginService, "isSoftDelete", false);
        List<ReleaseVersion> releaseVersions = Arrays.asList(new ReleaseVersion(), new ReleaseVersion());
        ShareResourceEntity shareResourceEntity = new ShareResourceEntity();
        shareResourceEntity.setResourceId("preset_test");
        when(releaseVersionMapper.selectByAppId(eq(Constants.TEST_PROJECT_ID))).thenReturn(releaseVersions);
        when(shareInnerService.cancelPluginShared(anyString(), anyString(), anyString())).thenReturn(true);
        // When
        CommonDeleteRsp result = pluginService.deletePlugin(Constants.TEST_PROJECT_ID, "preset_test", "default");

        // Then
        assertNotNull(result);
    }

    @Test
    @Sql(scripts = {"classpath:sql/plugin/create_plugin_db.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void test_listPlugins() throws IOException {
        PluginListRsp pluginListRsp = pluginService.listPlugins(Constants.TEST_PROJECT_ID, new ListPluginsQo());
        assertEquals(3, pluginListRsp.getCount());
    }

    @Test
    void test_decryptedExportPlugin() throws IOException {
        ExportParams body = mock(ExportParams.class);
        List<String> pluginIds = Arrays.asList("id1", "id2");
        when(body.getToolIds()).thenReturn(pluginIds);
        PluginDTO pluginDTO = TestUtil.getJsonObject(PluginDTO.class,
            "classpath:service/plugin/list_new_tool_res.json");
        pluginService.decryptedExportPlugin(pluginDTO);
    }

    @Test
    @Sql(scripts = {"classpath:sql/plugin/export_plugin_db.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void test_exportPlugins_Failure() throws IOException {
        ExportParams body = mock(ExportParams.class);
        List<String> pluginIds = Arrays.asList("preset_test", "preset_test");
        when(body.getToolIds()).thenReturn(pluginIds);

        PluginEntity pluginEntity = new PluginEntity();
        PluginDTO pluginDTO = new PluginDTO();
        when(pluginMapper.selectByPrimaryKey(eq("preset_test"), eq(Constants.TEST_PROJECT_ID))).thenReturn(
            pluginEntity);

        when(jacksonObjectMapper.writeValueAsString(any())).thenReturn("jsonString");
        assertNotNull(pluginService.exportplugins(Constants.TEST_PROJECT_ID, Constants.TEST_WORKSPACE_ID, body));
    }

    @Test
    @Sql(scripts = {"classpath:sql/plugin/create_plugin_db.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testReleaseToolVersionShouldFailedWhenNotTested() {
        CreateVersionReq createVersionReq = new CreateVersionReq();
        createVersionReq.setVersionName("test_version_name");
        createVersionReq.setVersionNote("test_version_note");

        assertThrows(AgentStudioException.class,
            () -> pluginService.releasePluginVersion(Constants.TEST_PROJECT_ID, "preset_test", "default",
                createVersionReq));
    }

    @Test
    public void test_releasePluginVersionHandler_empty_version_id() throws Exception {
        // Given
        PluginEntity pluginEntity = mock(PluginEntity.class);
        when(pluginEntity.getPluginId()).thenReturn("pluginId");

        String versionId = "";
        String versionName = "versionName";
        String versionNote = "versionNote";

        String dslPath = "path/to/dsl";
        when(obsService.uploadObsFile(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(
            dslPath);

        ReleaseVersion releaseVersion = new ReleaseVersion();

        // When
        ReleaseVersion result = pluginService.releasePluginVersionHandler(pluginEntity, versionId, versionName,
            versionNote);

        // Then
        assertNotNull(result);
        assertNotEquals(versionId, result.getVersionId());
        assertEquals(versionName, result.getVersionName());
        assertEquals(versionNote, result.getVersionNote());
    }

    @Test
    void test_importPluginsHandler() throws Exception {
        String projectId = "project1";
        String targetWorkspaceId = "workspace1";
        Set<ExtraMsg> pluginsOfAuth = new HashSet<>();
        PluginEntity plugin = new PluginEntity();
        plugin.setPluginChineseName("pluginName");
        plugin.setPluginDisplayName("pluginDisplayName");
        plugin.setPluginChineseName("pluginName");
        plugin.setRequestInfo(
            "{\"basic_info\":{\"host\":\"\",\"path\":\"\",\"protocol\":\"https\"},\"tool_info\":[{\"tool_id\":\"0\",\"tool_display_name\":\"nlp_1\",\"tool_chinese_name\":\"NLP模型_1\",\"tool_desc\":\"nlp大模型\",\"method\":\"POST\",\"path\":\"/api/v2/chat/completions\"}]}");

        when(pluginMapper.selectByChineseNameAndWorkspaceIdAndProjectId(anyString(), anyString(),
            anyString())).thenReturn(0);
        when(pluginMapper.selectByDisplayNameAndWorkspaceIdAndProjectId(anyString(), anyString(),
            anyString())).thenReturn(0);
        when(pluginMapper.selectByProjectIdAndSearchCriteria(anyString(), any(), any(), anyString())).thenReturn(
            Collections.emptyList());

        boolean result = pluginService.importPluginsHandler(projectId, targetWorkspaceId, plugin, pluginsOfAuth);

        assertFalse(result);
    }

    @Test
    void test_importPluginsHandler3() throws Exception {
        String projectId = "project1";
        String targetWorkspaceId = "workspace1";
        Set<ExtraMsg> pluginsOfAuth = new HashSet<>();
        PluginEntity plugin = new PluginEntity();
        plugin.setPluginChineseName("pluginName");
        plugin.setPluginDisplayName("pluginDisplayName");
        plugin.setPluginChineseName("pluginName");
        plugin.setRequestInfo(
            "{\"basic_info\":{\"host\":\"\",\"path\":\"\",\"protocol\":\"https\"},\"tool_info\":[{\"tool_id\":\"0\",\"tool_display_name\":\"nlp_1\",\"tool_chinese_name\":\"NLP模型_1\",\"tool_desc\":\"nlp大模型\",\"method\":\"POST\",\"path\":\"/api/v2/chat/completions\"}]}");

        when(pluginMapper.selectByChineseNameAndWorkspaceIdAndProjectId(anyString(), anyString(),
            anyString())).thenReturn(0);
        when(pluginMapper.selectByDisplayNameAndWorkspaceIdAndProjectId(anyString(), anyString(),
            anyString())).thenReturn(0);
        when(pluginMapper.selectByProjectIdAndSearchCriteria(anyString(), any(), any(), anyString())).thenReturn(
            Collections.emptyList());
        boolean result = pluginService.importPluginsHandler(projectId, targetWorkspaceId, plugin, pluginsOfAuth);

        assertFalse(result);
    }

    @Test
    void test_importPluginsHandler4() throws Exception {
        String projectId = "project1";
        String targetWorkspaceId = "workspace1";
        Set<ExtraMsg> pluginsOfAuth = new HashSet<>();
        PluginEntity plugin = new PluginEntity();
        plugin.setPluginChineseName("pluginName");
        plugin.setPluginDisplayName("pluginDisplayName");
        plugin.setPluginChineseName("pluginName");
        plugin.setRequestInfo(
            "{\"basic_info\":{\"host\":\"\",\"path\":\"\",\"protocol\":\"https\"},\"tool_info\":[{\"tool_id\":\"0\",\"tool_display_name\":\"nlp_1\",\"tool_chinese_name\":\"NLP模型_1\",\"tool_desc\":\"nlp大模型\",\"method\":\"POST\",\"path\":\"/api/v2/chat/completions\"}]}");
        plugin.setPluginId("pluginId");
        plugin.setVersionId("versionId");
        when(pluginMapper.selectByChineseNameAndWorkspaceIdAndProjectId(anyString(), anyString(),
            anyString())).thenReturn(0);
        when(pluginMapper.selectByDisplayNameAndWorkspaceIdAndProjectId(anyString(), anyString(),
            anyString())).thenReturn(0);
        when(pluginMapper.selectByProjectIdAndSearchCriteria(anyString(), any(), any(), anyString())).thenReturn(
            Collections.emptyList());

        boolean result = pluginService.importPluginsHandler(projectId, targetWorkspaceId, plugin, pluginsOfAuth);

        assertFalse(result);
    }

    @Test
    void test_importPluginsHandler5() throws Exception {
        String projectId = "project1";
        String targetWorkspaceId = "workspace1";
        Set<ExtraMsg> pluginsOfAuth = new HashSet<>();
        PluginEntity plugin = new PluginEntity();
        plugin.setPluginChineseName("pluginName");
        plugin.setPluginDisplayName("pluginDisplayName");
        plugin.setPluginChineseName("pluginName");
        plugin.setRequestInfo(
            "{\"basic_info\":{\"host\":\"\",\"path\":\"\",\"protocol\":\"https\"},\"tool_info\":[{\"tool_id\":\"0\",\"tool_display_name\":\"nlp_1\",\"tool_chinese_name\":\"NLP模型_1\",\"tool_desc\":\"nlp大模型\",\"method\":\"POST\",\"path\":\"/api/v2/chat/completions\"}]}");
        plugin.setPluginId("pluginId");
        plugin.setVersionId("versionId");
        when(pluginMapper.selectByChineseNameAndWorkspaceIdAndProjectId(anyString(), anyString(),
            anyString())).thenReturn(0);
        when(pluginMapper.selectByDisplayNameAndWorkspaceIdAndProjectId(anyString(), anyString(),
            anyString())).thenReturn(0);
        when(pluginMapper.selectByProjectIdAndSearchCriteria(anyString(), any(), any(), anyString())).thenReturn(
            Collections.emptyList());

        boolean result = pluginService.importPluginsHandler(projectId, targetWorkspaceId, plugin, pluginsOfAuth);

        assertFalse(result);
    }

    @Test
    void test_importPluginsHandler2() throws Exception {
        String projectId = "project1";
        String targetWorkspaceId = "workspace1";
        Set<ExtraMsg> pluginsOfAuth = new HashSet<>();
        PluginEntity plugin = new PluginEntity();
        plugin.setPluginChineseName("pluginName");
        plugin.setPluginDisplayName("pluginDisplayName");
        plugin.setPluginChineseName("pluginName");
        plugin.setRequestInfo(
            "{\"basic_info\":{\"host\":\"\",\"path\":\"\",\"protocol\":\"https\"},\"tool_info\":[{\"tool_id\":\"0\",\"tool_display_name\":\"nlp_1\",\"tool_chinese_name\":\"NLP模型_1\",\"tool_desc\":\"nlp大模型\",\"method\":\"POST\",\"path\":\"/api/v2/chat/completions\"}]}");
        plugin.setPluginId("pluginId");
        plugin.setVersionId("versionId");
        plugin.setTestStatus("[{\"tool_id\":\"0\",\"test_status\":0}]");
        when(pluginMapper.selectByChineseNameAndWorkspaceIdAndProjectId(anyString(), anyString(),
            anyString())).thenReturn(0);
        when(pluginMapper.selectByDisplayNameAndWorkspaceIdAndProjectId(anyString(), anyString(),
            anyString())).thenReturn(0);
        when(pluginMapper.selectByProjectIdAndSearchCriteria(anyString(), any(), any(), anyString())).thenReturn(
            Collections.emptyList());

        boolean result = pluginService.importPluginsHandler(projectId, targetWorkspaceId, plugin, pluginsOfAuth);

        assertFalse(result);
    }

    @Test
    public void test_deletePluginVersion_plugin_entity_is_null() throws Exception {
        // Given
        String projectId = "proj1";
        String versionId = "ver1";
        String pluginId = "plug1";
        String workspaceId = "ws1";

        when(pluginMapper.selectByPrimaryKeyAndWorkspace(eq(pluginId), eq(projectId), eq(workspaceId))).thenReturn(
            null);

        // When
        CommonDeleteRsp result = pluginService.deletePluginVersion(projectId, versionId, pluginId, workspaceId);

        // Then
        assertNull(result);
    }

    @Test
    @Sql(scripts = {"classpath:sql/plugin_reserve_meeting_room_setup_db.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testGetToolVersion() throws IOException {
        PluginEntity plugin = new PluginEntity();
        plugin.setPluginId("reserve_meeting_room");
        pluginService.adapterOldPlugin(plugin);
    }

    @Test
    @Sql(scripts = {"classpath:sql/plugin_reserve_meeting_room_setup_db.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testGetToolVersionThrow() throws IOException {
        VersionInfo versionInfo = pluginService.releasePluginVersion(Constants.TEST_PROJECT_ID,
            Constants.TEST_MEETING_ROOM_TOOL_ID, "default",
            new CreateVersionReq().setVersionName("test_version_name1").setVersionNote("test_version_note1"));
        assertThrows(AgentStudioException.class, () -> {
            pluginService.getPluginVersion(Constants.TEST_PROJECT_ID, versionInfo.getVersionId(),
                Constants.TEST_MEETING_ROOM_TOOL_ID, new GetPluginVersionQo().setWorkspaceId("default"));
        });
    }

    @Test
    @Sql(scripts = {"classpath:sql/plugin/create_plugin_db.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testRetrievePlugin() throws IOException {
        BaseResp resp = pluginService.retrievePlugin(Constants.TEST_PROJECT_ID, "preset_test3", "default");
        assertEquals(200, resp.getCode());
        assertEquals("success", resp.getMessage());
    }

    @Test
    void test_isPluginExist_should_throw_NullPointerException() {
        assertThrows(NullPointerException.class, () -> {
            pluginService.isPluginExist(null, null, null);
        });
    }

    @Test
    public void test_listPluginVersions_permission_check_fails() throws Exception {
        // Given
        String projectId = "proj1";
        String pluginId = "plug1";
        String workspaceId = "ws1";
        // When & Then
        assertThrows(AgentStudioException.class, () -> {
            pluginService.listPluginVersions(projectId, pluginId, workspaceId);
        });
    }

    @Test
    public void testCheckPluginPermission_PluginExists() {
        // Given
        String projectId = "proj1";
        String workspaceId = "ws1";
        String pluginId = "plug1";
        PluginEntity mockPluginEntity = new PluginEntity();
        // When & Then
        assertThrows(AgentStudioException.class, () -> {
            pluginService.checkPluginPermission(projectId, pluginId, workspaceId);
        });
    }

    @Test
    void test_deletePluginVersion_permission_denied() {
        /* ---------- 数据准备 ---------- */
        String projectId = "project1";
        String versionId = "version1";
        String pluginId = "plugin1";
        String workspaceId = "workspace1";
        String creatorId = "creator1";   // 记录里的创建人
        String currentUser = "creator2";   // 当前登录人（无权限）

        // 1. 把当前用户塞进上下文（根据你的 RequestContextUtils 实现）
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("userId", currentUser);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // 2. 真实插入一条插件记录（创建人是 creator1）
        PluginEntity plugin = new PluginEntity();
        plugin.setPluginId(pluginId);
        plugin.setProjectId(projectId);
        plugin.setWorkspaceId(workspaceId);
        plugin.setCreatorId(creatorId);
        pluginMapper.insert(plugin);

        /* ---------- 执行 & 断言 ---------- */
        assertDoesNotThrow(() -> pluginService.deletePluginVersion(projectId, versionId, pluginId, workspaceId));

    }

    @Test
    void test_importPlugins() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file",  // 参数名
            "test.jsonl",  // 原始文件名
            "text/plain",  // MIME类型
            Constants.TEST_NEW_PLUGIN_EXPORT.getBytes()  // 文件内容
        );

        pluginService.importplugins(Constants.TEST_WORKSPACE_ID, Constants.TEST_PROJECT_ID, file, "id1,id2");
    }

    @Test
    void test_importOldPlugins() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file",  // 参数名
            "test.jsonl",  // 原始文件名
            "text/plain",  // MIME类型
            Constants.TEST_OLD_PLUGIN_EXPORT.getBytes()  // 文件内容
        );

        pluginService.importplugins(Constants.TEST_WORKSPACE_ID, Constants.TEST_PROJECT_ID, file, "id1,id2");
    }

    @Test
    void test_importPluginVersion() throws Exception {
        PluginEntity pluginEntity = mock(PluginEntity.class);
        when(pluginEntity.getPluginId()).thenReturn("pluginId");

        pluginService.importPluginVersion(Constants.TEST_PROJECT_ID, pluginEntity);
    }

    @Test
    @Sql(scripts = {"classpath:sql/plugin/export_plugin_db.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void test_validPlugins() throws Exception {
        Set<String> uniquePluginIds = new HashSet<>();
        uniquePluginIds.add("preset_test");

        assertThrows(AgentStudioException.class, () -> {
            pluginService.validPlugins(uniquePluginIds, Constants.TEST_PROJECT_ID, "test");
        });
    }

    @Test
    void test_encryptedAuthInfo() throws Exception {
        AuthInfo authInfo = new AuthInfo();
        HisIamInfo hisIamInfo = new HisIamInfo();
        hisIamInfo.setIamAccount("test");
        hisIamInfo.setIamProject("project");
        hisIamInfo.setIamSecret("te");
        authInfo.setScope(AuthInfo.ScopeEnum.HIS_IAM);
        authInfo.setHisIamInfo(hisIamInfo);

        pluginService.encryptedAuthInfo(authInfo);

        HisSgov hisSgov = new HisSgov();
        hisSgov.setCredential("test");
        authInfo.setScope(AuthInfo.ScopeEnum.SGOV);
        authInfo.setHisSgov(hisSgov);
        pluginService.encryptedAuthInfo(authInfo);

        pluginService.anonymizeAuthInfo(authInfo);
    }

    @Test
    void test_anonymizeAuthInfo() throws Exception {
        AuthInfo authInfo = new AuthInfo();
        HisIamInfo hisIamInfo = new HisIamInfo();
        hisIamInfo.setIamAccount("test");
        hisIamInfo.setIamProject("project");
        hisIamInfo.setIamSecret("te");
        authInfo.setScope(AuthInfo.ScopeEnum.HIS_IAM);
        authInfo.setHisIamInfo(hisIamInfo);
        pluginService.anonymizeAuthInfo(authInfo);

        PluginIAMAuthInfo pluginIAMAuthInfo = new PluginIAMAuthInfo();
        pluginIAMAuthInfo.setIamPassword("test");
        authInfo.setCustomIamCredentials(pluginIAMAuthInfo);
        authInfo.setScope(AuthInfo.ScopeEnum.CUSTOM_IAM);
        pluginService.anonymizeAuthInfo(authInfo);
    }

    @Test
    void test_parseAuthInfo() throws Exception {
        AuthInfo authInfo = new AuthInfo();
        HisIamInfo hisIamInfo = new HisIamInfo();
        hisIamInfo.setIamAccount("test");
        hisIamInfo.setIamProject("project");
        hisIamInfo.setIamSecret("te");
        authInfo.setScope(AuthInfo.ScopeEnum.HIS_IAM);
        authInfo.setHisIamInfo(hisIamInfo);
        WorkflowUtils.parseAuthInfo(authInfo);

        HisSgov hisSgov = new HisSgov();
        hisSgov.setCredential("test");
        authInfo.setScope(AuthInfo.ScopeEnum.SGOV);
        authInfo.setHisSgov(hisSgov);
        WorkflowUtils.parseAuthInfo(authInfo);

        PluginIAMAuthInfo pluginIAMAuthInfo = new PluginIAMAuthInfo();
        authInfo.setScope(AuthInfo.ScopeEnum.CUSTOM_IAM);
        authInfo.setCustomIamCredentials(pluginIAMAuthInfo);
        WorkflowUtils.parseAuthInfo(authInfo);
    }

    @Test
    @Sql(scripts = {"classpath:sql/plugin/modify_plugin_db.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void test_modifyPlugin_auth() throws IOException {
        final ModifyPluginReq body = TestUtil.getJsonObject(ModifyPluginReq.class,
            "classpath:service/plugin/modify_plugin_auth_req.json");
        ModifyPluginRsp result = pluginService.modifyPlugin(Constants.TEST_PROJECT_ID, "default", "preset_test", body);
    }

    @Test
    void test_anonymizeAuthInfo_CUSTOM_IAM() throws Exception {
        AuthInfo authInfo = new AuthInfo();

        PluginIAMAuthInfo pluginIAMAuthInfo = new PluginIAMAuthInfo();
        pluginIAMAuthInfo.setIamAk("test");
        pluginIAMAuthInfo.setIamSk("test");
        authInfo.setCustomIamCredentials(pluginIAMAuthInfo);
        authInfo.setScope(AuthInfo.ScopeEnum.CUSTOM_IAM);
        pluginService.anonymizeAuthInfo(authInfo);
    }

    @Test
    void test_encryptedAuthInfo_CUSTOM_IAM() throws Exception {
        AuthInfo authInfo = new AuthInfo();
        PluginIAMAuthInfo pluginIAMAuthInfo = new PluginIAMAuthInfo();
        pluginIAMAuthInfo.setIamAk("test");
        pluginIAMAuthInfo.setIamSk("test");
        authInfo.setCustomIamCredentials(pluginIAMAuthInfo);
        authInfo.setScope(AuthInfo.ScopeEnum.CUSTOM_IAM);
        pluginService.encryptedAuthInfo(authInfo);

        pluginIAMAuthInfo.setIamPassword("test");
        pluginService.encryptedAuthInfo(authInfo);
    }


    @Test
    @Sql(scripts = {"classpath:sql/plugin/create_plugin_db.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testRetrievePlugin_failed() throws IOException {
        assertThrows(AgentStudioException.class, () -> {
            pluginService.retrievePlugin(Constants.TEST_PROJECT_ID, "preset_test", "test");
        });

    }

    @Test
    void testParsePlugin_NullContent() {
        // Arrange
        String projectId = "proj-123";
        String workspaceId = "ws-456";

        ParsePluginReq body = new ParsePluginReq();
        body.setContent(null);

        // Act & Assert
        Exception exception = assertThrows(AgentStudioException.class, () -> {
            pluginService.parsePlugin(projectId, workspaceId, body);
        });

        assertEquals(StudioError.OPENAPI_FILE_NOT_EXIST.getCode(), "1169");
    }

    @Test
    void testParsePlugin_Success() throws Exception {
        // Arrange
        String projectId = "proj-123";
        String workspaceId = "ws-456";

        String openapiJson = "{\n" + "  \"openapi\": \"3.0.3\",\n"
            + "  \"info\": {\"title\": \"Test API\", \"version\": \"1.0\"},\n" + "  \"paths\": {\n"
            + "    \"/test\": {\n" + "      \"get\": {\n" + "        \"summary\": \"Test endpoint\",\n"
            + "        \"responses\": {\n" + "          \"200\": {\"description\": \"OK\"}\n" + "        }\n"
            + "      }\n" + "    }\n" + "  }\n" + "}";

        ParsePluginReq body = new ParsePluginReq();
        body.setContent(openapiJson);

        // Act
        BaseResp result = pluginService.parsePlugin(projectId, workspaceId, body);

        // Assert
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
        assertNotNull(result.getData());
    }

    @Test
    void test_batchCreateTool_with_plugin_not_exists() {
        String projectId = "project1";
        String workspaceId = "workspace1";
        BatchCreatePluginToolReq body = mock(BatchCreatePluginToolReq.class);
        List<CreatePluginToolReq> tools = new ArrayList<>();
        CreatePluginToolReq toolRequest = mock(CreatePluginToolReq.class);
        tools.add(toolRequest);
        when(body.getTools()).thenReturn(tools);
        when(pluginMapper.selectByPrimaryKeyAndWorkspace(anyString(), eq(projectId), eq(workspaceId))).thenReturn(null);

        assertThrows(AgentStudioException.class, () -> {
            pluginService.batchCreateTool(projectId, workspaceId, body);
        });
    }

    @Test
    @Sql(scripts = {"classpath:sql/plugin/create_tool_db.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void test_batch_createTool_succeed() throws IOException {
        // setup
        final CreatePluginToolReq body = TestUtil.getJsonObject(CreatePluginToolReq.class,
            "classpath:service/plugin/create_tool.json");

        BatchCreatePluginToolReq batchBody = new BatchCreatePluginToolReq();
        batchBody.setTools(Collections.singletonList(body));
        // run the test
        BatchCreatePluginToolRsp result = pluginService.batchCreateTool(Constants.TEST_PROJECT_ID, "default",
            batchBody);

        // verify the results
        assertNotNull(result.getIds());
    }

    @Test
    void test_incrementPluginFreeTrialUsageQuota_should_return_not_null() throws Exception {
        try (MockedStatic<NumberUtils> mockedStaticNumberUtils = mockStatic(NumberUtils.class, RETURNS_DEEP_STUBS)) {
            // Given
            mockedStaticNumberUtils.when(() -> NumberUtils.toInt(anyString())).thenReturn(0);
            // When
            BaseResp result = pluginService.incrementPluginFreeTrialUsageQuota("not_empty", "not_empty", "not_empty",
                "not_empty");

            // Then
            assertNotNull(result);
        }
    }

    @Test
    void test_firstUsageRecordCreated_Success() {
        // Given
        String projectId = "proj-123";
        String domainId = "dom-456";
        String pluginId = "plugin-789";
        String workspaceId = "ws-000";
        // When
        BaseResp result = pluginService.incrementPluginFreeTrialUsageQuota(projectId, domainId, pluginId, workspaceId);

        // Then
        assertNotNull(result);
    }

    @Test
    void test_firstUsageRecordCreated_Success2() {
        // Given
        String projectId = "proj-123";
        String domainId = "dom-456";
        String pluginId = "plugin-789";
        String workspaceId = "ws-000";

        when(redisClient.get(anyString())).thenReturn(null);
        RedisLock redisLock = mock(RedisLock.class);
        when(redisClient.getLock(anyString())).thenReturn(redisLock);

        // When
        BaseResp result = pluginService.incrementPluginFreeTrialUsageQuota(projectId, domainId, pluginId, workspaceId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getCode());
        assertEquals("success", result.getMessage());
    }

    @Test
    void test_usageReachedMaxLimit_ReturnFail() {
        // Given
        String projectId = "proj-123";
        String domainId = "dom-456";
        String pluginId = "plugin-789";
        String workspaceId = "ws-000";

        when(redisClient.get(anyString())).thenReturn("10");

        // When
        BaseResp result = pluginService.incrementPluginFreeTrialUsageQuota(projectId, domainId, pluginId, workspaceId);

        // Then
        assertNotNull(result);
    }

    @Test
    void test_updateHost() {
        PluginDTO pluginDTO = new PluginDTO();
        ToolRequestInfo toolRequestInfo = new ToolRequestInfo();
        ToolInfo toolInfo = new ToolInfo();
        toolRequestInfo.setToolsInfoList(Collections.singletonList(toolInfo));
        pluginDTO.setToolRequestInfo(toolRequestInfo);
        pluginService.updateHost(pluginDTO);
    }

    @Test
    void test_adapterPlugin() {
        PluginEntity pluginEntity = new PluginEntity();
        AuthInfo authInfo = new AuthInfo();
        List<AuthKeyInfo> authKeyInfoList = new ArrayList<>();
        AuthKeyInfo authKeyInfo = new AuthKeyInfo();
        authKeyInfo.setAuthKey("test");
        authKeyInfoList.add(authKeyInfo);
        authInfo.setAuthKeys(authKeyInfoList);
        pluginEntity.setAuthInfo(authInfo);
        pluginService.adapterPlugin(pluginEntity);
    }

    @Test
    @Sql(scripts = {"classpath:sql/plugin/create_tool_db.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void test_checkAndUpdateUuid() {
        PluginExportEntity pluginExportEntity = new PluginExportEntity();
        PluginEntity pluginEntity = new PluginEntity();
        pluginExportEntity.setMetadata(pluginEntity);
        pluginService.checkAndUpdateUuid("default", "test_project_id", "preset_tdfen", pluginExportEntity);
        pluginService.checkAndUpdateUuid("default", "test_project_id", "test", pluginExportEntity);
    }
}
