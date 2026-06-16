/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */
package com.openjiuwen.studio.agent.manager.utils;

import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.manager.dto.McpServerBaseInfoDto;
import com.openjiuwen.studio.agent.manager.dto.McpServerDetailInfoDto;
import com.openjiuwen.studio.agent.manager.dto.McpServerRuntimeVo;
import com.openjiuwen.studio.agent.manager.dto.McpServiceBaseInfoDto;
import com.openjiuwen.studio.agent.manager.dto.McpServiceDetailInfoDto;
import com.openjiuwen.studio.agent.manager.dto.McpServiceSkinnyEntity;
import com.openjiuwen.studio.agent.manager.dto.McpServiceToolsEntity;
import com.openjiuwen.studio.agent.manager.entity.McpServerEntity;
import com.openjiuwen.studio.agent.manager.entity.McpServiceEntity;
import com.openjiuwen.studio.agent.manager.service.mcp.infrastructure.apig.ApigService;
import com.openjiuwen.studio.agent.manager.service.mcp.model.ImportMcpServiceDependencyCheck;
import com.openjiuwen.studio.agent.manager.service.mcp.model.McpServiceFromProject;
import com.openjiuwen.studio.agent.manager.service.mcp.model.dao.McpServerDao;
import com.openjiuwen.studio.agent.manager.service.mcp.model.dao.McpServiceDao;
import com.openjiuwen.studio.common.service.service.EncryptionAdapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
class McpUtilTest {

    @Mock
    private McpServerDao serverDao;

    @Mock
    private McpServiceDao serviceDao;

    @Mock
    private ApigService apigService;

    @Mock
    private EncryptionAdapter encryptionAdapter;

    @InjectMocks
    private McpUtil mcpUtil;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(mcpUtil, "mcpSecondDomain", "test.domain.com");
        ReflectionTestUtils.setField(mcpUtil, "bindDomainSwitch", "false");
        ReflectionTestUtils.setField(mcpUtil, "apigwDomainCertId", "cert-123");
        ReflectionTestUtils.setField(mcpUtil, "mcpAuthSecret", "test-secret");
        ReflectionTestUtils.setField(mcpUtil, "mcpAuthKey", "test-key");
        ReflectionTestUtils.setField(mcpUtil, "mcpNameEnCheck", false);
        ReflectionTestUtils.setField(mcpUtil, "isSoftDelete", true);
    }

    @Test
    void testServerEntity2McpBaseInfoDto_Normal() {
        McpServerEntity entity = new McpServerEntity();
        entity.setId("server-1");
        entity.setName("TestServer");
        entity.setDescription("desc");

        McpServerBaseInfoDto result = mcpUtil.serverEntity2McpBaseInfoDto(entity);

        assertNotNull(result);
        assertEquals("TestServer", result.getName());
    }

    @Test
    void testServiceEntity2McpBaseInfoDto_Normal() {
        McpServiceEntity entity = new McpServiceEntity();
        entity.setId("svc-1");
        entity.setName("TestService");

        McpServiceBaseInfoDto result = mcpUtil.serviceEntity2McpBaseInfoDto(entity);

        assertNotNull(result);
        assertEquals("TestService", result.getName());
    }

    @Test
    void testServiceEntity2McpBaseInfoDto_WithFailReason() {
        McpServiceEntity entity = new McpServiceEntity();
        entity.setId("svc-1");
        entity.setName("TestService");
        entity.setFailReason("{\"code\":\"1161\", \"debug_info\":\"some debug\"}");

        McpServiceBaseInfoDto result = mcpUtil.serviceEntity2McpBaseInfoDto(entity);

        assertNotNull(result);
    }

    @Test
    void testServiceEntity2McpServiceFromProject_Normal() {
        McpServiceEntity entity = new McpServiceEntity();
        entity.setId("svc-1");
        entity.setName("TestService");

        McpServiceFromProject result = mcpUtil.serviceEntity2McpServiceFromProject(entity);

        assertNotNull(result);
    }

    @Test
    void testMcpServiceEntity2ImportMcpServiceFromProject_Normal() {
        McpServiceEntity entity = new McpServiceEntity();
        entity.setId("svc-1");

        ImportMcpServiceDependencyCheck result = mcpUtil.mcpServiceEntity2ImportMcpServiceFromProject(entity);

        assertNotNull(result);
    }

    @Test
    void testMcpServiceEntity2McpServiceToolsEntity_Normal() {
        McpServiceEntity entity = new McpServiceEntity();
        entity.setId("svc-1");
        entity.setTools("[{\"name\":\"tool1\"}]");

        McpServiceToolsEntity result = mcpUtil.mcpServiceEntity2McpServiceToolsEntity(entity);

        assertNotNull(result);
    }

    @Test
    void testMcpServiceFromProject2ServiceEntity_Normal() {
        McpServiceFromProject fromProject = new McpServiceFromProject();

        McpServiceEntity result = mcpUtil.mcpServiceFromProject2ServiceEntity(fromProject);

        assertNotNull(result);
    }

    @Test
    void testServiceEntity2McpBaseRuntimeInfoDto_Normal() {
        McpServiceEntity entity = new McpServiceEntity();
        entity.setId("svc-1");
        entity.setName("TestService");
        entity.setDeployType("sse");
        entity.setDescription("desc");

        McpServiceBaseInfoDto result = mcpUtil.serviceEntity2McpBaseRuntimeInfoDto(entity);

        assertNotNull(result);
        assertNotNull(result.getMcpServers());
        assertEquals("svc-1", result.getMcpServers().getId());
    }

    @Test
    void testServerEntity2McpServerDetailInfoDto_Normal() {
        McpServerEntity entity = new McpServerEntity();
        entity.setId("server-1");
        entity.setName("TestServer");

        McpServerDetailInfoDto result = mcpUtil.serverEntity2McpServerDetailInfoDto(entity);

        assertNotNull(result);
    }

    @Test
    void testServiceEntity2McpServiceDetailInfoDto_Normal() {
        McpServiceEntity entity = new McpServiceEntity();
        entity.setId("svc-1");
        entity.setName("TestService");

        McpServiceDetailInfoDto result = mcpUtil.serviceEntity2McpServiceDetailInfoDto(entity);

        assertNotNull(result);
    }

    @Test
    void testServiceEntity2ServiceEntitySkinny_Normal() {
        McpServiceEntity entity = new McpServiceEntity();
        entity.setId("svc-1");
        entity.setName("TestService");

        McpServiceSkinnyEntity result = mcpUtil.serviceEntity2ServiceEntitySkinny(entity);

        assertNotNull(result);
    }

    @Test
    void testUpdateServiceDeletedInfoFromFG_SoftDelete() {
        ReflectionTestUtils.setField(mcpUtil, "isSoftDelete", true);

        mcpUtil.updateServiceDeletedInfoFromFG("id-1", "tenant-1", "dept-1");

        verify(serviceDao).markAsDeleted("id-1", "tenant-1", "dept-1");
        verify(serviceDao, never()).hardDeleteSingleMcpServiceById(anyString(), anyString(), anyString());
    }

    @Test
    void testUpdateServiceDeletedInfoFromFG_HardDelete() {
        ReflectionTestUtils.setField(mcpUtil, "isSoftDelete", false);

        mcpUtil.updateServiceDeletedInfoFromFG("id-1", "tenant-1", "dept-1");

        verify(serviceDao).hardDeleteSingleMcpServiceById("id-1", "tenant-1", "dept-1");
        verify(serviceDao, never()).markAsDeleted(anyString(), anyString(), anyString());
    }

    @Test
    void testUpdateServiceStatus_Normal() {
        mcpUtil.updateServiceStatus("svc-1", "running", "tenant-1", "dept-1");

        verify(serviceDao).updateStatus("svc-1", "running", "tenant-1", "dept-1");
    }

    @Test
    void testInsertAuthHeadersIntoMcpServer_WithCommand() {
        String json = "{\"mcpServers\":{\"server1\":{\"command\":\"npx\",\"args\":[\"-y\",\"test\"]}}}";

        String result = mcpUtil.insertAuthHeadersIntoMcpServer(json);

        assertNotNull(result);
        assertTrue(result.contains("X-HW-ID"));
        assertTrue(result.contains("X-HW-APPKEY"));
    }

    @Test
    void testInsertAuthHeadersIntoMcpServer_NoMcpServers() {
        String json = "{\"other\":\"value\"}";

        String result = mcpUtil.insertAuthHeadersIntoMcpServer(json);

        assertNotNull(result);
        assertFalse(result.contains("X-HW-ID"));
    }

    @Test
    void testInsertAuthHeadersIntoMcpServer_InvalidJson() {
        String json = "not-json";

        String result = mcpUtil.insertAuthHeadersIntoMcpServer(json);

        assertEquals("not-json", result);
    }

    @Test
    void testDeleteAuthHeadersFromMcpServer_WithHeaders() {
        String json = "{\"mcpServers\":{\"server1\":{\"command\":\"npx\",\"headers\":{\"X-HW-ID\":\"key\"}}}}";

        String result = mcpUtil.deleteAuthHeadersFromMcpServer(json);

        assertNotNull(result);
        assertFalse(result.contains("X-HW-ID"));
    }

    @Test
    void testDeleteAuthHeadersFromMcpServer_NoHeaders() {
        String json = "{\"mcpServers\":{\"server1\":{\"command\":\"npx\"}}}";

        String result = mcpUtil.deleteAuthHeadersFromMcpServer(json);

        assertNotNull(result);
    }

    @Test
    void testDeleteAuthHeadersFromMcpServer_InvalidJson() {
        String json = "invalid";

        String result = mcpUtil.deleteAuthHeadersFromMcpServer(json);

        assertEquals("invalid", result);
    }

    @Test
    void testFillMcpRuntimeVoWithConfig_ValidConfig() {
        McpServerRuntimeVo vo = new McpServerRuntimeVo();
        vo.setName("test");
        String config = "{\"mcpServers\":{\"server1\":{\"command\":\"npx\",\"args\":[\"-y\",\"test\"],\"env\":{\"KEY\":\"val\"}}}}";

        mcpUtil.fillMcpRuntimeVoWithConfig(config, vo);

        assertEquals("npx", vo.getCommand());
        assertNotNull(vo.getArgs());
        assertNotNull(vo.getEnv());
    }

    @Test
    void testFillMcpRuntimeVoWithConfig_InvalidConfig() {
        McpServerRuntimeVo vo = new McpServerRuntimeVo();
        vo.setName("test");

        assertThrows(Exception.class, () -> mcpUtil.fillMcpRuntimeVoWithConfig("not-json", vo));
    }

    @Test
    void testFillMcpRuntimeVo_Normal() {
        McpServiceEntity entity = new McpServiceEntity();
        entity.setId("svc-1");
        entity.setName("TestService");
        entity.setDeployType("sse");
        entity.setDescription("desc");
        entity.setFcInstanceUrl("http://test.com");
        entity.setServerConfig("encrypted-config");

        when(encryptionAdapter.decrypt("encrypted-config"))
            .thenReturn("{\"mcpServers\":{\"server1\":{\"command\":\"npx\"}}}");

        McpServerRuntimeVo result = mcpUtil.fillMcpRuntimeVo(entity);

        assertNotNull(result);
        assertEquals("svc-1", result.getId());
        assertEquals("http://test.com", result.getBaseUrl());
    }

    @Test
    void testCheckNameDuplication_NoDuplicate() {
        McpServiceDetailInfoDto dto = new McpServiceDetailInfoDto();
        dto.setName("unique-name");

        when(serviceDao.getMcpServiceByNameAndTenant(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> mcpUtil.checkNameDuplication(dto, "ws-1"));
    }

    @Test
    void testCheckNameDuplication_Duplicate() {
        McpServiceDetailInfoDto dto = new McpServiceDetailInfoDto();
        dto.setName("dup-name");

        McpServiceEntity existing = new McpServiceEntity();
        when(serviceDao.getMcpServiceByNameAndTenant(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(Collections.singletonList(existing));

        assertThrows(AgentStudioException.class, () -> mcpUtil.checkNameDuplication(dto, "ws-1"));
    }

    @Test
    void testCheckNameEnDuplication_NoDuplicate() {
        McpServiceDetailInfoDto dto = new McpServiceDetailInfoDto();
        dto.setNameEn("unique-en");

        when(serviceDao.getMcpServiceByNameEnAndTenant(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> mcpUtil.checkNameEnDuplication(dto, "ws-1"));
    }

    @Test
    void testCheckNameEnDuplication_Duplicate() {
        McpServiceDetailInfoDto dto = new McpServiceDetailInfoDto();
        dto.setNameEn("dup-en");

        McpServiceEntity existing = new McpServiceEntity();
        when(serviceDao.getMcpServiceByNameEnAndTenant(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(Collections.singletonList(existing));

        assertThrows(AgentStudioException.class, () -> mcpUtil.checkNameEnDuplication(dto, "ws-1"));
    }

    @Test
    void testMcpServiceInfoDto2ServerEntity_Normal() {
        McpServiceDetailInfoDto dto = new McpServiceDetailInfoDto();
        dto.setName("test");

        McpServerEntity result = mcpUtil.mcpServiceInfoDto2ServerEntity(dto);

        assertNotNull(result);
    }
}
