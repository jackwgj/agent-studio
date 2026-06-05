/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.constant.Constants;
import com.openjiuwen.studio.agent.manager.dto.McpServerDetailReq;
import com.openjiuwen.studio.agent.manager.dto.McpServiceDetailInfoDto;
import com.openjiuwen.studio.agent.manager.entity.McpServerEntity;
import com.openjiuwen.studio.agent.manager.entity.McpServiceEntity;
import com.openjiuwen.studio.agent.manager.service.mcp.infrastructure.apig.ApigService;
import com.openjiuwen.studio.agent.manager.service.mcp.model.McpServiceFromProject;
import com.openjiuwen.studio.agent.manager.service.mcp.model.apig.ApigApiGroupDetailResp;
import com.openjiuwen.studio.agent.manager.service.mcp.model.apig.ApigBindDomainCertificateReq;
import com.openjiuwen.studio.agent.manager.service.mcp.model.apig.ApigBindDomainsReq;
import com.openjiuwen.studio.agent.manager.service.mcp.model.apig.ApigBindDomainsResp;
import com.openjiuwen.studio.agent.manager.service.mcp.model.apig.UrlDomain;
import com.openjiuwen.studio.agent.manager.service.mcp.model.dao.McpServiceDao;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@MockitoSettings(strictness = Strictness.LENIENT)
class McpUtilTest extends BaseTest {
    @Autowired
    private McpUtil mcpUtil;

    @MockitoBean
    ApigService apigService;

    @MockitoBean
    private McpServiceDao serviceDao;

    private static MockedStatic<RequestContextUtils> mockedRequestContextUtils;

    @BeforeAll
    public static void init() {
        RequestContextUtils.setRequestAuthTokenAndProjectId(Constants.TEST_TOKEN, Constants.TEST_PROJECT_ID);
        mockedRequestContextUtils = Mockito.mockStatic(RequestContextUtils.class);

    }

    @BeforeEach
    void setUp() throws Exception {
        ReflectionTestUtils.setField(mcpUtil, "bindDomainSwitch", "true");
        ReflectionTestUtils.setField(mcpUtil, "mcpSecondDomain", "test.com");
        ReflectionTestUtils.setField(mcpUtil, "apigwDomainCertId", "123");
    }

    @AfterAll
    static void end() {
        mockedRequestContextUtils.close();
    }

    @Test
    void testMcpServiceFromProject2ServiceEntity() {
        McpServiceFromProject mcpServiceEntity = new McpServiceFromProject();
        McpServiceEntity result = mcpUtil.mcpServiceFromProject2ServiceEntity(mcpServiceEntity);
        Assertions.assertNotNull(result);
    }

    @Test
    void testServiceEntity2McpServiceFromProject() {
        McpServiceEntity mcpServiceEntity = new McpServiceEntity();
        McpServiceFromProject result = mcpUtil.serviceEntity2McpServiceFromProject(mcpServiceEntity);
        Assertions.assertNotNull(result);
    }

    @Test
    void testServiceEntity2McpServiceRuntimeDetailInfoDto() {
        McpServiceEntity mcpServiceEntity = new McpServiceEntity();
        mcpServiceEntity.setNameEn("test");
        mcpServiceEntity.setName("test");
        McpServiceDetailInfoDto result = mcpUtil.serviceEntity2McpServiceRuntimeDetailInfoDto(mcpServiceEntity);
        Assertions.assertNotNull(result);
    }

    @Test
    void testMcpDetailInfoDto2ServerEntity() {
        McpServerDetailReq mcpServiceEntity = new McpServerDetailReq();
        McpServerEntity result = mcpUtil.mcpDetailInfoDto2ServerEntity(mcpServiceEntity);
        Assertions.assertNotNull(result);
    }

    @Test
    public void test_insertAuthHeadersIntoMcpServer_exception_case()
        throws JsonProcessingException, JsonMappingException {
        // Given
        String jsonString = """
            {
              "mcpServers": {
                "example-npx": {
                  "command": "npx",
                  "args":
                  [
                    "-y",
                    "@dandeliongold/mcp-time"
                  ]
                }
              }
            }""";

        // When
        String result = mcpUtil.insertAuthHeadersIntoMcpServer(jsonString);

        // Then
        Assertions.assertTrue(result.contains("\"headers\""));
        Assertions.assertTrue(result.contains("\"X-HW-ID\""));
        Assertions.assertTrue(result.contains("\"X-HW-APPKEY\""));
    }

    @Test
    public void test_deleteAuthHeadersFromMcpServer_no_mcp_servers_node()
        throws JsonMappingException, JsonProcessingException {
        // Given
        String jsonString = """
            123""";

        // When
        String result = mcpUtil.deleteAuthHeadersFromMcpServer(jsonString);

        // Then
        assertEquals(jsonString, result);
    }

    @Test
    public void test_deleteAuthHeadersFromMcpServer_exception_case()
        throws JsonMappingException, JsonProcessingException {
        // Given
        String jsonString = """
            {
              "mcpServers": {
                "example-npx": {
                  "command": "npx",
                  "args":
                  [
                    "-y",
                    "@dandeliongold/mcp-time"
                  ],
                  "headers":{
                    "key":"value"
                  }
                }
              }
            }""";

        // When
        String result = mcpUtil.deleteAuthHeadersFromMcpServer(jsonString);

        // Then
        assertFalse(result.contains("headers"));
    }

    @Test
    public void testMcpServiceDetailInfoDto2ServiceEntity() {
        McpServiceDetailInfoDto mcpServiceDetailInfoDto = new McpServiceDetailInfoDto();
        mcpServiceDetailInfoDto.setName("test_name");
        mcpServiceDetailInfoDto.setNameEn("test_name_en");
        mcpServiceDetailInfoDto.setDescription("test_description");
        mcpServiceDetailInfoDto.setOrgType("SSE");
        mcpUtil.mcpServiceDetailInfoDto2ServiceEntity(mcpServiceDetailInfoDto, "test_workspacer_id");
        mcpServiceDetailInfoDto.setId("test_id");
        mcpUtil.mcpServiceDetailInfoDto2ServiceEntity(mcpServiceDetailInfoDto, "test_workspacer_id");
        mcpServiceDetailInfoDto.setDeployType("sse");
        Assertions.assertThrows(AgentStudioException.class,
                () ->mcpUtil.mcpServiceDetailInfoDto2ServiceEntity(mcpServiceDetailInfoDto, "test_workspacer_id"));
        when(serviceDao.getMcpServiceByNameAndTenant(any(),any(), any(), any())).thenReturn(new ArrayList<>());
        Assertions.assertThrows(AgentStudioException.class,
                () ->mcpUtil.mcpServiceDetailInfoDto2ServiceEntity(mcpServiceDetailInfoDto, "test_workspacer_id"));
    }

    @Test
    public void testCheckNameEnDuplication() {
        McpServiceDetailInfoDto mcpServiceDetailInfoDto = new McpServiceDetailInfoDto();
        mcpServiceDetailInfoDto.setName("test_name");
        mcpServiceDetailInfoDto.setNameEn("test_name_en");
        mcpUtil.checkNameDuplication(mcpServiceDetailInfoDto,"test_workspace_id");
        mcpUtil.checkNameEnDuplication(mcpServiceDetailInfoDto,"test_workspace_id");
        List<McpServiceEntity> list = new ArrayList<>();
        McpServiceEntity existEntity = new McpServiceEntity();
        existEntity.setName("test_name");
        list.add(existEntity);
        list.add(existEntity);
        when(serviceDao.getMcpServiceByNameEnAndTenant(any(),any(), any(), any())).thenReturn(list);
        Assertions.assertThrows(AgentStudioException.class,
                () ->mcpUtil.checkNameEnDuplication(mcpServiceDetailInfoDto,"test_workspace_id"));
    }



    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private org.springframework.context.MessageSource messageSource; // 1. 注入 Mock 的 MessageSource

    /**
     * 测试场景：详情页 DTO 转换 + 正常 JSON 解析 + I18N 成功
     */
    @Test
    void testServiceEntity2McpServiceDetailInfoDto_WithFailReason() {
        // A. 准备环境：初始化 McpErrorFactory 的静态依赖
        ReflectionTestUtils.setField(McpErrorFactory.class, "messageSource", messageSource);
        // 让 Mock 的 MessageSource 直接返回 Key 作为文案 (简化验证)
        when(messageSource.getMessage(anyString(), any(), any(), any())).thenAnswer(i -> i.getArgument(0));

        // B. 准备数据
        McpServiceEntity entity = new McpServiceEntity();
        entity.setId("test-id");
        entity.setName("test-service");
        // 构造一个标准的 JSON 字符串
        entity.setFailReason("{\"code\":\"1161\", \"debug_info\":\"stack trace info\"}");

        // C. 执行测试
        McpServiceDetailInfoDto dto = mcpUtil.serviceEntity2McpServiceDetailInfoDto(entity);

        // D. 验证结果
        Assertions.assertNotNull(dto.getFailReasonDetail(), "failReasonDetail should not be null");
        // 验证 Code 是否被正确补全前缀
        Assertions.assertEquals("openjiuwen.02401161", dto.getFailReasonDetail().getCode());
        // 验证 debug_info 是否正确提取
        Assertions.assertEquals("stack trace info", dto.getFailReasonDetail().getDebugInfo());
    }

    /**
     * 测试场景：列表页 DTO 转换 + 旧版 JSON 字段兼容 (驼峰 debugInfo)
     */
    @Test
    void testServiceEntity2McpBaseInfoDto_LegacyFieldCompatibility() {
        // A. 准备环境
        ReflectionTestUtils.setField(McpErrorFactory.class, "messageSource", messageSource);
        when(messageSource.getMessage(anyString(), any(), any(), any())).thenAnswer(i -> i.getArgument(0));

        // B. 准备数据 (模拟旧数据：使用 debugInfo 而不是 debug_info)
        McpServiceEntity entity = new McpServiceEntity();
        entity.setId("legacy-id");
        entity.setFailReason("{\"code\":\"1162\", \"debugInfo\":\"legacy stack trace\"}");

        // C. 执行测试
        com.openjiuwen.studio.agent.manager.dto.McpServiceBaseInfoDto dto = mcpUtil.serviceEntity2McpBaseInfoDto(entity);

        // D. 验证结果
        Assertions.assertNotNull(dto.getFailReasonDetail());
        Assertions.assertEquals("openjiuwen.02401162", dto.getFailReasonDetail().getCode());
        Assertions.assertEquals("legacy stack trace", dto.getFailReasonDetail().getDebugInfo());
    }

    /**
     * 测试场景：JSON 解析异常 (Malformed JSON)
     * 预期：异常被捕获，不影响主流程，failReasonDetail 为 null
     */
    @Test
    void testFillFailReasonDetail_JsonException() {
        McpServiceEntity entity = new McpServiceEntity();
        entity.setId("123");
        // 错误的 JSON 格式 (缺右括号)
        entity.setFailReason("{\"code\":\"1161\"");

        com.openjiuwen.studio.agent.manager.dto.McpServiceBaseInfoDto dto = mcpUtil.serviceEntity2McpBaseInfoDto(entity);

        // 验证异常被吞掉，字段为 null
        Assertions.assertNull(dto.getFailReasonDetail());
    }

    /**
     * 测试场景：FailReason 为空或 JSON 内容缺失
     */
    @Test
    void testFillFailReasonDetail_Empty() {
        McpServiceEntity entity = new McpServiceEntity();

        // 1. 测试 null
        entity.setFailReason(null);
        com.openjiuwen.studio.agent.manager.dto.McpServiceBaseInfoDto dto1 = mcpUtil.serviceEntity2McpBaseInfoDto(entity);
        Assertions.assertNull(dto1.getFailReasonDetail());

        // 2. 测试空 JSON 对象 (无 code)
        entity.setFailReason("{}");
        com.openjiuwen.studio.agent.manager.dto.McpServiceBaseInfoDto dto2 = mcpUtil.serviceEntity2McpBaseInfoDto(entity);
        Assertions.assertNull(dto2.getFailReasonDetail());
    }
}
