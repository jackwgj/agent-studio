/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service.mcp;

import static com.openjiuwen.studio.agent.common.dto.auth.AuthInfo.DomainEnum.HEADERS;
import static com.openjiuwen.studio.agent.common.dto.auth.AuthInfo.DomainEnum.QUERY;
import static com.openjiuwen.studio.agent.common.dto.auth.AuthInfo.ScopeEnum.SERVICE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.constant.Constants;
import com.openjiuwen.studio.agent.common.dto.auth.AuthInfo;
import com.openjiuwen.studio.agent.common.dto.auth.AuthKeyInfo;
import com.openjiuwen.studio.agent.manager.dto.McpServerConfig;
import com.openjiuwen.studio.agent.manager.dto.McpServerInfo;
import com.openjiuwen.studio.agent.manager.dto.McpServerTool;
import com.openjiuwen.studio.agent.manager.dto.McpServerTools;
import com.openjiuwen.studio.agent.manager.mapper.McpServerConfigMapper;
import com.openjiuwen.studio.agent.manager.mapper.McpServerMapper;
import com.openjiuwen.studio.agent.manager.mapper.McpServiceMapper;
import com.openjiuwen.studio.agent.manager.service.startup.McpUpdateService;
import com.openjiuwen.studio.agent.common.utils.CryptoUtils;
import com.openjiuwen.studio.common.service.service.EncryptionAdapter;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Transactional
@MockitoSettings(strictness = Strictness.LENIENT)
public class McpUpdateServiceTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private McpServerMapper mcpServerMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private McpServerConfigMapper mcpServerConfigMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private McpServiceMapper mcpServiceMapper;

    @Mock
    private EncryptionAdapter encryptionAdapter;

    @InjectMocks
    private McpUpdateService mcpUpdateService;

    private AutoCloseable mockitoCloseable;

    private MockedStatic<CryptoUtils> mgSccCryptoUtils;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        mgSccCryptoUtils = Mockito.mockStatic(CryptoUtils.class, RETURNS_DEEP_STUBS);

        mgSccCryptoUtils.when(() -> CryptoUtils.encrypt(Mockito.anyString())).thenReturn("pwd");
        mgSccCryptoUtils.when(() -> CryptoUtils.decrypt(Mockito.anyString())).thenReturn("decrypt");

        // 注入 encryptionAdapter
        ReflectionTestUtils.setField(mcpUpdateService, "encryptionAdapter", encryptionAdapter);

        // 配置 Adapter 默认行为
        when(encryptionAdapter.encrypt(anyString(), anyString())).thenAnswer(i -> i.getArgument(0));
        when(encryptionAdapter.decrypt(anyString())).thenAnswer(i -> i.getArgument(0));
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
        mgSccCryptoUtils.close();
    }

    @Test
    void test_updateMcp_should_not_throw_exception() throws Exception {
        try (MockedStatic<RequestContextUtils> mockedRequestContext = Mockito.mockStatic(RequestContextUtils.class)) {
            // 设置静态方法的返回值，绕过真实的上下文检查
            mockedRequestContext.when(RequestContextUtils::getRequestUserDomainId).thenReturn("test_domain_id");
            // 如果代码中还用了 Token 或 ProjectId，建议也 Mock 上，防止遗漏
            mockedRequestContext.when(RequestContextUtils::getRequestAuthToken).thenReturn(Constants.TEST_TOKEN);
            mockedRequestContext.when(RequestContextUtils::getRequestProjectId).thenReturn(Constants.TEST_PROJECT_ID);

            assertDoesNotThrow(() -> {
                // Given
                List<String> curIds = new ArrayList<>();
                curIds.add("not_empty");

                List<McpServerConfig> confOld = new ArrayList<>();
                McpServerConfig mcpServerConfig = new McpServerConfig();
                mcpServerConfig.setId("");
                mcpServerConfig.setServerId("test");
                mcpServerConfig.setProjectId("");
                mcpServerConfig.setAuthKeys(new ArrayList<>());
                mcpServerConfig.setCreatorId("");
                mcpServerConfig.setCreateTime(new Date());
                mcpServerConfig.setUpdateTime(new Date());

                confOld.add(mcpServerConfig);
                when(mcpServerConfigMapper.selectAll()).thenReturn(confOld);

                List<McpServerInfo> mcpServerOld = getMcpServerInfos1();
                mcpServerOld.addAll(getMcpServerInfos2());
                when(mcpServerMapper.selectAll()).thenReturn(mcpServerOld);

                // When
                mcpUpdateService.updateMcp();
            });
        }
    }

    private List<McpServerInfo> getMcpServerInfos2() {
        List<McpServerInfo> mcpServerOld = new ArrayList<>();
        McpServerInfo mcpServerInfo = new McpServerInfo();
        mcpServerInfo.setServerId("test2");
        mcpServerInfo.setProjectId("test");
        mcpServerInfo.setName("test");
        mcpServerInfo.setNameEn("test");
        mcpServerInfo.setDesc("test");
        mcpServerInfo.setIcon("test");
        mcpServerInfo.setIconName("test");
        McpServerTools tools = new McpServerTools();
        tools.setCount(1);
        McpServerTool mcpServerTool = new McpServerTool();
        mcpServerTool.setName("test");
        mcpServerTool.setDesc("test");
        mcpServerTool.setInput("{\n" + "  \"type\" : \"object\",\n" + "  \"properties\" : {\n" + "    \"input\" : {\n"
                + "      \"type\" : \"string\",\n" + "      \"description\" : \"The string to calculate an MD5 hash for\"\n"
                + "    },\n" + "    \"trimWhitespace\" : {\n" + "      \"type\" : \"boolean\",\n"
                + "      \"default\" : false,\n"
                + "      \"description\" : \"Whether to trim whitespace from the input before hashing\"\n" + "    }\n"
                + "  },\n" + "  \"required\" : [ \"input\" ],\n" + "  \"additionalProperties\" : false\n" + "}");
        tools.setTools(new ArrayList<>(Collections.singleton(mcpServerTool)));
        mcpServerInfo.setTools(tools);
        mcpServerInfo.setVisibility("public");
        mcpServerInfo.setUrl("");
        AuthInfo authInfo = new AuthInfo();
        authInfo.setScope(SERVICE);
        authInfo.setDomain(HEADERS);
        List<AuthKeyInfo> authKeyInfos = new ArrayList<>();
        AuthKeyInfo authKeyInfo = new AuthKeyInfo();
        authKeyInfo.setTargetName("key");
        authKeyInfo.setSourceName("key");
        authKeyInfo.setAuthKey("key");
        authKeyInfo.setDesc("");
        authKeyInfos.add(authKeyInfo);
        authInfo.setAuthKeys(authKeyInfos);
        mcpServerInfo.setAuth(authInfo);
        mcpServerInfo.setType("");
        mcpServerInfo.setCategory("");
        mcpServerInfo.setConfigs(new McpServerConfig());
        mcpServerInfo.setConfigStatus("");
        mcpServerInfo.setBinding(false);
        mcpServerInfo.setCreator("");
        mcpServerInfo.setCreatorId("");
        mcpServerInfo.setCreateTime(new Date());
        mcpServerInfo.setUpdateTime(new Date());

        mcpServerOld.add(mcpServerInfo);
        return mcpServerOld;
    }

    private static @NotNull List<McpServerInfo> getMcpServerInfos1() {
        List<McpServerInfo> mcpServerOld = new ArrayList<>();
        McpServerInfo mcpServerInfo = new McpServerInfo();
        mcpServerInfo.setServerId("test");
        mcpServerInfo.setProjectId("test");
        mcpServerInfo.setName("test");
        mcpServerInfo.setNameEn("test");
        mcpServerInfo.setDesc("test");
        mcpServerInfo.setIcon("test");
        mcpServerInfo.setIconName("test");
        McpServerTools tools = new McpServerTools();
        tools.setCount(1);
        McpServerTool mcpServerTool = new McpServerTool();
        mcpServerTool.setName("test");
        mcpServerTool.setDesc("test");
        mcpServerTool.setInput("{\n" + "  \"type\" : \"object\",\n" + "  \"properties\" : {\n" + "    \"input\" : {\n"
                + "      \"type\" : \"string\",\n" + "      \"description\" : \"The string to calculate an MD5 hash for\"\n"
                + "    },\n" + "    \"trimWhitespace\" : {\n" + "      \"type\" : \"boolean\",\n"
                + "      \"default\" : false,\n"
                + "      \"description\" : \"Whether to trim whitespace from the input before hashing\"\n" + "    }\n"
                + "  },\n" + "  \"required\" : [ \"input\" ],\n" + "  \"additionalProperties\" : false\n" + "}");
        tools.setTools(new ArrayList<>(Collections.singleton(mcpServerTool)));
        mcpServerInfo.setTools(tools);
        mcpServerInfo.setVisibility("public");
        mcpServerInfo.setUrl("");
        AuthInfo authInfo = new AuthInfo();
        authInfo.setScope(SERVICE);
        authInfo.setDomain(QUERY);
        List<AuthKeyInfo> authKeyInfos = new ArrayList<>();
        authInfo.setAuthKeys(authKeyInfos);
        mcpServerInfo.setAuth(authInfo);
        mcpServerInfo.setType("");
        mcpServerInfo.setCategory("");
        mcpServerInfo.setConfigs(new McpServerConfig());
        mcpServerInfo.setConfigStatus("");
        mcpServerInfo.setBinding(false);
        mcpServerInfo.setCreator("");
        mcpServerInfo.setCreatorId("");
        mcpServerInfo.setCreateTime(new Date());
        mcpServerInfo.setUpdateTime(new Date());

        mcpServerOld.add(mcpServerInfo);
        return mcpServerOld;
    }
}
