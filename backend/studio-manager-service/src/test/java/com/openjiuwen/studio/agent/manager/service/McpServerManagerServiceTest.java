/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */

package com.openjiuwen.studio.agent.manager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.dto.auth.AuthKeyInfo;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.LanguageUtils;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.common.utils.UrlCheckUtils;
import com.openjiuwen.studio.agent.common.dto.auth.AuthInfo;
import com.openjiuwen.studio.agent.manager.dto.CommonDeleteRsp;
import com.openjiuwen.studio.agent.manager.dto.ListMcpServersQo;
import com.openjiuwen.studio.agent.manager.dto.McpCallToolReq;
import com.openjiuwen.studio.agent.common.dto.mcp.McpCallToolResp;
import com.openjiuwen.studio.agent.manager.dto.McpServerConfig;
import com.openjiuwen.studio.agent.manager.dto.McpServerInfo;
import com.openjiuwen.studio.agent.manager.dto.McpServerInfoList;
import com.openjiuwen.studio.agent.manager.dto.McpServerReq;
import com.openjiuwen.studio.agent.manager.dto.McpServerTool;
import com.openjiuwen.studio.agent.manager.dto.McpServerTools;
import com.openjiuwen.studio.agent.manager.mapper.AgentMapper;
import com.openjiuwen.studio.agent.manager.mapper.MappingMapper;
import com.openjiuwen.studio.agent.manager.mapper.McpServerConfigMapper;
import com.openjiuwen.studio.agent.manager.mapper.McpServerMapper;
import com.openjiuwen.studio.agent.manager.mapper.WorkflowMapper;
import com.openjiuwen.studio.agent.manager.obs.MgObsService;
import com.openjiuwen.studio.agent.manager.rce.client.AgentRuntimeClient;
import com.openjiuwen.studio.common.service.service.EncryptionAdapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@ExtendWith(MockitoExtension.class)
class McpServerManagerServiceTest {

    @Mock
    private McpServerMapper serverMapper;

    @Mock
    private UrlCheckUtils urlCheckUtils;

    @Mock
    private MappingMapper mappingMapper;

    @Mock
    private McpServerConfigMapper configMapper;

    @Mock
    private WorkflowMapper workflowMapper;

    @Mock
    private MgObsService obsService;

    @Mock
    private AgentMapper agentMapper;

    @Mock
    private AgentRuntimeClient runtimeClient;

    @Mock
    private MessageSource messageSource;

    @Mock
    private WorkflowCommonService workflowCommonService;

    @Mock
    private EncryptionAdapter encryptionAdapter;

    private McpServerManagerService mcpServerManagerService;

    @BeforeEach
    void setUp() {
        mcpServerManagerService = new McpServerManagerService(
            serverMapper, urlCheckUtils, mappingMapper, configMapper,
            workflowMapper, obsService, agentMapper, runtimeClient,
            messageSource, workflowCommonService);
        ReflectionTestUtils.setField(mcpServerManagerService, "encryptionAdapter", encryptionAdapter);
        ReflectionTestUtils.setField(mcpServerManagerService, "mcpDomainId", "mcp-service");
        ReflectionTestUtils.setField(mcpServerManagerService, "opProjectId", "op-proj");
        ReflectionTestUtils.setField(mcpServerManagerService, "defaultIcon", "default-icon");
    }

    @Test
    void testRecoveryAuthInfo_McpServerInfo_Success() {
        McpServerInfo serverInfo = new McpServerInfo();
        serverInfo.setServerId("server-1");

        McpServerConfig serverConfig = new McpServerConfig();
        AuthKeyInfo configKey = new AuthKeyInfo();
        configKey.setTargetName("header1");
        configKey.setAuthKey("secret-value");
        serverConfig.setAuthKeys(List.of(configKey));
        serverInfo.setConfigs(serverConfig);

        AuthInfo authInfo = new AuthInfo();
        AuthKeyInfo authKey = new AuthKeyInfo();
        authKey.setTargetName("header1");
        authKey.setAuthKey("");
        authInfo.setAuthKeys(List.of(authKey));
        serverInfo.setAuth(authInfo);

        McpServerManagerService.recoveryAuthInfo(serverInfo);

        assertEquals("secret-value", authInfo.getAuthKeys().get(0).getAuthKey());
    }

    @Test
    void testRecoveryAuthInfo_McpServerInfo_NullConfig() {
        McpServerInfo serverInfo = new McpServerInfo();
        serverInfo.setServerId("server-1");
        serverInfo.setConfigs(null);
        serverInfo.setAuth(new AuthInfo());

        McpServerManagerService.recoveryAuthInfo(serverInfo);
        assertNotNull(serverInfo);
    }

    @Test
    void testRecoveryAuthInfo_McpServerInfo_NullAuthKeys() {
        McpServerInfo serverInfo = new McpServerInfo();
        serverInfo.setServerId("server-1");

        McpServerConfig serverConfig = new McpServerConfig();
        serverConfig.setAuthKeys(null);
        serverInfo.setConfigs(serverConfig);
        serverInfo.setAuth(null);

        McpServerManagerService.recoveryAuthInfo(serverInfo);
        assertNotNull(serverInfo);
    }

    @Test
    void testDeleteMcpServer_Success() {
        try (MockedStatic<RequestContextUtils> reqCtx = mockStatic(RequestContextUtils.class)) {
            reqCtx.when(RequestContextUtils::getRequestUserId).thenReturn("user-1");

            McpServerInfo info = new McpServerInfo();
            info.setServerId("server-1");
            info.setCreatorId("user-1");
            when(serverMapper.selectByPrimaryKey("proj-1", "server-1", "user-1")).thenReturn(info);

            CommonDeleteRsp result = mcpServerManagerService.deleteMcpServer("proj-1", "server-1");
            assertNotNull(result);
            assertEquals("server-1", result.getId());
            verify(serverMapper).deleteByPrimaryKey("proj-1", "server-1", "user-1");
        }
    }

    @Test
    void testDeleteMcpServer_NotFound() {
        try (MockedStatic<RequestContextUtils> reqCtx = mockStatic(RequestContextUtils.class)) {
            reqCtx.when(RequestContextUtils::getRequestUserId).thenReturn("user-1");
            when(serverMapper.selectByPrimaryKey("proj-1", "server-1", "user-1")).thenReturn(null);

            CommonDeleteRsp result = mcpServerManagerService.deleteMcpServer("proj-1", "server-1");
            assertNull(result);
        }
    }

    @Test
    void testDeleteMcpServer_PermissionDenied() {
        try (MockedStatic<RequestContextUtils> reqCtx = mockStatic(RequestContextUtils.class)) {
            reqCtx.when(RequestContextUtils::getRequestUserId).thenReturn("user-2");

            McpServerInfo info = new McpServerInfo();
            info.setServerId("server-1");
            info.setCreatorId("user-1");
            when(serverMapper.selectByPrimaryKey("proj-1", "server-1", "user-2")).thenReturn(info);

            assertThrows(AgentStudioException.class,
                () -> mcpServerManagerService.deleteMcpServer("proj-1", "server-1"));
        }
    }

    @Test
    void testDeleteMcpServerConfig_Success() {
        try (MockedStatic<RequestContextUtils> reqCtx = mockStatic(RequestContextUtils.class)) {
            reqCtx.when(RequestContextUtils::getRequestUserId).thenReturn("user-1");

            McpServerConfig config = new McpServerConfig();
            config.setServerId("server-1");
            when(configMapper.selectByUniqueIds("proj-1", "user-1", "server-1")).thenReturn(config);

            CommonDeleteRsp result = mcpServerManagerService.deleteMcpServerConfig("proj-1", "server-1");
            assertNotNull(result);
            assertEquals("server-1", result.getId());
        }
    }

    @Test
    void testDeleteMcpServerConfig_NotFound() {
        try (MockedStatic<RequestContextUtils> reqCtx = mockStatic(RequestContextUtils.class)) {
            reqCtx.when(RequestContextUtils::getRequestUserId).thenReturn("user-1");
            when(configMapper.selectByUniqueIds("proj-1", "user-1", "server-1")).thenReturn(null);

            CommonDeleteRsp result = mcpServerManagerService.deleteMcpServerConfig("proj-1", "server-1");
            assertNull(result);
        }
    }

    @Test
    void testGetMcpServer_Success() {
        try (MockedStatic<RequestContextUtils> reqCtx = mockStatic(RequestContextUtils.class);
             MockedStatic<LanguageUtils> langUtils = mockStatic(LanguageUtils.class)) {

            reqCtx.when(RequestContextUtils::getRequestUserId).thenReturn("user-1");
            langUtils.when(LanguageUtils::getLanguageLocale).thenReturn(Locale.CHINESE);

            McpServerInfo info = new McpServerInfo();
            info.setServerId("server-1");
            info.setCreator("custom-user");
            info.setType("custom");
            when(serverMapper.selectByPrimaryKey("proj-1", "server-1", "user-1")).thenReturn(info);

            McpServerInfo result = mcpServerManagerService.getMcpServer("proj-1", "server-1");
            assertNotNull(result);
            assertEquals("server-1", result.getServerId());
        }
    }

    @Test
    void testGetMcpServer_NotFound() {
        try (MockedStatic<RequestContextUtils> reqCtx = mockStatic(RequestContextUtils.class)) {
            reqCtx.when(RequestContextUtils::getRequestUserId).thenReturn("user-1");
            when(serverMapper.selectByPrimaryKey("proj-1", "server-1", "user-1")).thenReturn(null);

            assertThrows(AgentStudioException.class,
                () -> mcpServerManagerService.getMcpServer("proj-1", "server-1"));
        }
    }

    @Test
    void testGetMcpServer_OfficialCreator() {
        try (MockedStatic<RequestContextUtils> reqCtx = mockStatic(RequestContextUtils.class);
             MockedStatic<LanguageUtils> langUtils = mockStatic(LanguageUtils.class)) {

            reqCtx.when(RequestContextUtils::getRequestUserId).thenReturn("user-1");
            langUtils.when(LanguageUtils::getLanguageLocale).thenReturn(Locale.CHINESE);

            McpServerInfo info = new McpServerInfo();
            info.setServerId("server-1");
            info.setCreator("系统预置");
            info.setType("custom");
            when(serverMapper.selectByPrimaryKey("proj-1", "server-1", "user-1")).thenReturn(info);
            when(messageSource.getMessage("mcpserver.creator.official", null, Locale.CHINESE)).thenReturn("官方");

            McpServerInfo result = mcpServerManagerService.getMcpServer("proj-1", "server-1");
            assertNotNull(result);
            assertEquals("官方", result.getCreator());
        }
    }

    @Test
    void testListMcpServerTools_BlankUrl() {
        McpServerReq body = new McpServerReq();
        body.setUrl("");

        assertThrows(AgentStudioException.class,
            () -> mcpServerManagerService.listMcpServerTools("proj-1", 0, 10, body));
    }

    @Test
    void testCallTool_ServerNotFound() {
        try (MockedStatic<RequestContextUtils> reqCtx = mockStatic(RequestContextUtils.class)) {
            reqCtx.when(RequestContextUtils::getRequestUserId).thenReturn("user-1");
            when(serverMapper.selectByPrimaryKey("proj-1", "server-1", "user-1")).thenReturn(null);

            McpCallToolReq body = new McpCallToolReq();
            body.setName("tool-1");

            assertThrows(AgentStudioException.class,
                () -> mcpServerManagerService.callTool("proj-1", "server-1", body));
        }
    }

    @Test
    void testCallTool_Success() {
        try (MockedStatic<RequestContextUtils> reqCtx = mockStatic(RequestContextUtils.class)) {
            reqCtx.when(RequestContextUtils::getRequestUserId).thenReturn("user-1");
            reqCtx.when(RequestContextUtils::getRequestAuthToken).thenReturn("token-1");

            McpServerInfo info = new McpServerInfo();
            info.setServerId("server-1");
            info.setUrl("http://mcp-server:8080");
            info.setType("custom");
            when(serverMapper.selectByPrimaryKey("proj-1", "server-1", "user-1")).thenReturn(info);

            McpCallToolResp resp = new McpCallToolResp();
            when(runtimeClient.callMcpServerTool(anyString(), anyString(), any()))
                .thenReturn(ResponseEntity.ok(resp));

            McpCallToolReq body = new McpCallToolReq();
            body.setName("tool-1");

            McpCallToolResp result = mcpServerManagerService.callTool("proj-1", "server-1", body);
            assertNotNull(result);
        }
    }

    @Test
    void testCallTool_RuntimeError() {
        try (MockedStatic<RequestContextUtils> reqCtx = mockStatic(RequestContextUtils.class)) {
            reqCtx.when(RequestContextUtils::getRequestUserId).thenReturn("user-1");
            reqCtx.when(RequestContextUtils::getRequestAuthToken).thenReturn("token-1");

            McpServerInfo info = new McpServerInfo();
            info.setServerId("server-1");
            info.setUrl("http://mcp-server:8080");
            info.setType("custom");
            when(serverMapper.selectByPrimaryKey("proj-1", "server-1", "user-1")).thenReturn(info);

            McpCallToolResp errorResp = new McpCallToolResp();
            when(runtimeClient.callMcpServerTool(anyString(), anyString(), any()))
                .thenReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResp));

            McpCallToolReq body = new McpCallToolReq();
            body.setName("tool-1");

            McpCallToolResp result = mcpServerManagerService.callTool("proj-1", "server-1", body);
            assertNotNull(result);
        }
    }

    @Test
    void testListMcpServers_Success() {
        try (MockedStatic<RequestContextUtils> reqCtx = mockStatic(RequestContextUtils.class);
             MockedStatic<LanguageUtils> langUtils = mockStatic(LanguageUtils.class)) {

            reqCtx.when(RequestContextUtils::getRequestUserId).thenReturn("user-1");
            langUtils.when(LanguageUtils::getLanguageLocale).thenReturn(Locale.CHINESE);

            McpServerInfo info = new McpServerInfo();
            info.setServerId("server-1");
            info.setCreator("user");
            info.setType("custom");
            when(serverMapper.selectBySearchCriteria(anyString(), anyString(), any())).thenReturn(List.of(info));

            ListMcpServersQo query = new ListMcpServersQo();
            query.setOffset(0);
            query.setLimit(10);

            McpServerInfoList result = mcpServerManagerService.listMcpServers("proj-1", query);
            assertNotNull(result);
            assertNotNull(result.getServers());
        }
    }

    @Test
    void testUpdateMcpServer_NotFound() {
        try (MockedStatic<RequestContextUtils> reqCtx = mockStatic(RequestContextUtils.class)) {
            reqCtx.when(RequestContextUtils::getRequestUserId).thenReturn("user-1");
            when(serverMapper.selectByPrimaryKey("proj-1", "server-1", "user-1")).thenReturn(null);

            McpServerReq body = new McpServerReq();
            body.setUrl("http://new-url");

            assertThrows(AgentStudioException.class,
                () -> mcpServerManagerService.updateMcpServer("proj-1", "server-1", body));
        }
    }

    @Test
    void testUpdateMcpServerConfig_NotFound() {
        try (MockedStatic<RequestContextUtils> reqCtx = mockStatic(RequestContextUtils.class)) {
            reqCtx.when(RequestContextUtils::getRequestUserId).thenReturn("user-1");
            when(configMapper.selectByUniqueIds("proj-1", "user-1", "server-1")).thenReturn(null);

            McpServerConfig body = new McpServerConfig();

            assertThrows(AgentStudioException.class,
                () -> mcpServerManagerService.updateMcpServerConfig("proj-1", "server-1", body));
        }
    }

    @Test
    void testInit_SetsStaticAdapter() {
        mcpServerManagerService.init();
        assertNotNull(mcpServerManagerService);
    }
}
