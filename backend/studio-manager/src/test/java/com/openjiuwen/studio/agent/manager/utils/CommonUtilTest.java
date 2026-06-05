/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.alibaba.fastjson2.JSONObject;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.constant.Constants;
import com.openjiuwen.studio.agent.manager.dto.PageInfoV2;
import com.openjiuwen.studio.agent.manager.entity.McpServerEntity;
import com.openjiuwen.studio.agent.manager.enums.NameSplitInfix;
import com.openjiuwen.studio.agent.manager.service.mcp.model.BaseEntity;
import com.openjiuwen.studio.agent.manager.service.mcp.model.UpdateBaseInfo;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpHeaders;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@MockitoSettings(strictness = Strictness.LENIENT)
class CommonUtilTest {

    private static MockedStatic<RequestContextUtils> mockedRequestContextUtils;

    @BeforeAll
    public static void init() {
        RequestContextUtils.setRequestAuthTokenAndProjectId(Constants.TEST_TOKEN, Constants.TEST_PROJECT_ID);
        mockedRequestContextUtils = Mockito.mockStatic(RequestContextUtils.class);
    }

    @AfterAll
    static void end() {
        mockedRequestContextUtils.close();
    }


    @Test
    void testGenUUID() {
        String result = CommonUtil.genUUID();
        Assertions.assertNotNull(result);
    }

    @Test
    void testGetTenantId() {
        when(CommonUtil.getTenantId()).thenReturn("system");
        String result = CommonUtil.getTenantId();
        Assertions.assertNotNull(result);
    }

    @Test
    void testGetUserId() {
        mockedRequestContextUtils.when(RequestContextUtils::getRequestUserId).thenReturn("test");
        String result = CommonUtil.getUserId();
        Assertions.assertNotNull(result);
    }

    @Test
    void testGetDeptCode() {
        String result = CommonUtil.getDeptCode();
        Assertions.assertNotNull(result);
    }

    @Test
    void testInitBaseEntity() {
        BaseEntity baseEntity = new BaseEntity();
        baseEntity.setTenantId("test");
        baseEntity.setDeptCode("test");
        baseEntity.setCreatedDate(new Date());
        baseEntity.setDeleted(false);
        CommonUtil.initBaseEntity(baseEntity);
    }

    @Test
    void testInitUpdateBaseInfo() {
        UpdateBaseInfo result = CommonUtil.initUpdateBaseInfo();
        Assertions.assertNotNull(result);
    }

    @Test
    void testIsValidUUid() {
        boolean result = CommonUtil.isValidUUid("str");
        assertEquals(false, result);
    }

    @Test
    void testIsValidUUid2() {
        boolean result = CommonUtil.isValidUUid("51ddae55-5897-1512-90ae-45ad9e4881b5");
        assertEquals(true, result);
    }

    @Test
    void testReplaceConnectorName() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("test", "test");
        Map<String, Object> json = new HashMap<>();
        json.put("info", jsonObject);
        CommonUtil.replaceConnectorName(json, "newName");
    }

    @Test
    void testSplitName() {
        String result = CommonUtil.splitName("realName", NameSplitInfix.PRIVATE_CONNECTOR);
        Assertions.assertNotNull(result);
    }

    @Test
    void testSpliceName() {
        String result = CommonUtil.spliceName("name", NameSplitInfix.PRIVATE_CONNECTOR);
        Assertions.assertNotNull(result);
    }

    @Test
    void testGetExpireTime() {
        String result = CommonUtil.getExpireTime(0L);
        Assertions.assertNotNull(result);
    }

    @Test
    void testSplitFunctionExpression() {
        String result = CommonUtil.splitFunctionExpression("functionExpression");
        Assertions.assertNotNull(result);
    }

    @Test
    void testValidateTenantIdIsNotEmpty() {
        when(CommonUtil.getTenantId()).thenReturn("system");
        CommonUtil.validateTenantIdIsNotEmpty();
    }

    @Test
    void testValidatePageInfo() {
        PageInfoV2 pageInfoV2 = new PageInfoV2();
        pageInfoV2.setOffset(-1);
        pageInfoV2.setLimit(-1);
        Assertions.assertThrows(Exception.class,
            () -> CommonUtil.validatePageInfo(pageInfoV2));
    }

    @Test
    void testValidatePageInfo2() {
        PageInfoV2 pageInfoV2 = new PageInfoV2();
        pageInfoV2.setOffset(-1);
        pageInfoV2.setLimit(10);
        Assertions.assertThrows(Exception.class,
            () -> CommonUtil.validatePageInfo(pageInfoV2));
    }

    @Test
    void testValidatePageInfo3() {
        Assertions.assertThrows(Exception.class,
            () -> CommonUtil.validatePageInfo(null));
    }

    @Test
    void testValidateProjectId() {
        CommonUtil.validateProjectId("projectId");
    }

    @Test
    void testValidateWorkspaceId() {
        CommonUtil.validateWorkspaceId("workspaceId");
    }

    @Test
    void testValidateProjectAndWorkspaceId() {
        CommonUtil.validateProjectAndWorkspaceId("projectId", "workspaceId");
    }

    @Test
    void testGetAndValidateTenantId() {
        when(CommonUtil.getTenantId()).thenReturn("system");
        String result = CommonUtil.getAndValidateTenantId();
        Assertions.assertNotNull(result);
    }

    @Test
    void testValidateMcpConfigFormat() {
        Assertions.assertThrows(Exception.class,
            () -> CommonUtil.validateMcpConfigFormat("config"));
    }

    @Test
    void testGetUUID() {
        String result = CommonUtil.getUUID();
        Assertions.assertNotNull(result);
    }

    @Test
    void testSetCommonUpdateProperties() {
        McpServerEntity mcpServerEntity = new McpServerEntity();
        CommonUtil.setCommonUpdateProperties(mcpServerEntity);
    }

    @Test
    void test_setCommonUpdateProperties_throw_exception() {
        PageInfoV2 pageInfoV2 = new PageInfoV2();
        assertThrows(AgentStudioException.class, () -> {
            CommonUtil.setCommonUpdateProperties(pageInfoV2);
        });
    }

    @Test
    void test_setCommonCreateProperties_throw_exception() {
        PageInfoV2 pageInfoV2 = new PageInfoV2();
        assertThrows(AgentStudioException.class, () -> {
            CommonUtil.setCommonCreateProperties(pageInfoV2);
        });
    }

    @Test
    void testSetCommonCreateProperties() {
        McpServerEntity mcpServerEntity = new McpServerEntity();
        CommonUtil.setCommonCreateProperties(mcpServerEntity);
    }

    @Test
    void testIsIntegerAndLessThan10() {
        boolean result = CommonUtil.isIntegerAndLessThan10("9");
        assertEquals(true, result);
    }

    @Test
    void testParseMcpConfigHeaderInfo() {
        CommonUtil.parseMcpConfigHeaderInfo("configInfo", Map.of("headers", "headers"), 0);
    }

    @Test
    void testParseMcpConfigHeaderInfo2() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("x-hw", "tset");
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("headers", jsonObject);
        Map<String, String> result = CommonUtil.parseMcpConfigHeaderInfo(jsonParam.toString());
        Assertions.assertNotNull(result);
    }

    @Test
    void testGenerateHeaders() {
        HttpHeaders result = CommonUtil.generateHeaders();
        Assertions.assertNotNull(result);
    }

    @Test
    void testList2String() {
        String result = CommonUtil.List2String(List.of("list"));
        Assertions.assertNotNull(result);
    }

    @Test
    void testParseUrlFromMcpConfig() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("headers", "headers");
        jsonObject.put("url", "https://localhost/sse");
        String result = CommonUtil.parseUrlFromMcpConfig(jsonObject, 6);
        Assertions.assertNotNull(result);
    }

    @Test
    void testParseUrlFromMcpConfig2() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("headers", "headers");
        jsonObject.put("url", "https://localhost/sse");
        String result = CommonUtil.parseUrlFromMcpConfig(jsonObject, 1);
        Assertions.assertNotNull(result);
    }

    @Test
    public void test_getRawQueryString_null_input() {
        // Given
        String input = null;

        // When
        String result = CommonUtil.getRawQueryString(input);

        // Then
        assertNull(result);
    }

    @Test
    public void test_getRawQueryString_empty_input() {
        // Given
        String input = "   ";

        // When
        String result = CommonUtil.getRawQueryString(input);

        // Then
        assertNull(result);
    }

    @Test
    public void test_getRawQueryString_valid_url_without_query() {
        // Given
        String input = "http://example.com/path";

        // When
        String result = CommonUtil.getRawQueryString(input);

        // Then
        assertNull(result);
    }

    @Test
    public void test_getRawQueryString_valid_url_with_query() {
        // Given
        String input = "http://example.com/path?query=param";

        // When
        String result = CommonUtil.getRawQueryString(input);

        // Then
        assertEquals("query=param", result);
    }

    @Test
    void testGetResourceContentWithNonexistentResource() {
        // 使用不存在的资源文件
        String resourceName = "non_existent_file.txt";

        // 执行测试
        String result = CommonUtil.getResourceContent(resourceName);

        // 验证返回空字符串
        assertEquals("", result);
    }

    @Test
    void testGetResourceContentWithEmptyInput() {
        // 测试空输入
        assertEquals("", CommonUtil.getResourceContent(""));
    }

    @Test
    void testGetResourceContentWithNullInput() {
        // 测试null输入
        assertEquals("", CommonUtil.getResourceContent(null));
    }
}
