/* Copyright (c) Huawei Technologies Co., Ltd. 2024-2026. All rights reserved. */
package com.openjiuwen.studio.agent.common.utils.simple;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.constant.SimpleConstants;
import com.openjiuwen.studio.agent.common.dto.simple.SimpleUser;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.service.simple.PocConfig;
import com.openjiuwen.studio.agent.common.utils.SpringBeanUtils;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SimpleAuthUtilsTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private PocConfig pocConfig;

    private MockedStatic<SpringBeanUtils> springBeanUtilsMock;

    @BeforeEach
    void setUp() {
        springBeanUtilsMock = mockStatic(SpringBeanUtils.class);
        springBeanUtilsMock.when(() -> SpringBeanUtils.getBean(PocConfig.class)).thenReturn(pocConfig);
    }

    @AfterEach
    void tearDown() {
        springBeanUtilsMock.close();
    }

    @Test
    void testTokenToUser_ValidTokenWithProjectId() {
        when(pocConfig.getDefaultDomain()).thenReturn("defaultDomain");

        SimpleUser user = SimpleAuthUtils.tokenToUser("user1|project1");

        assertNotNull(user);
        assertEquals("user1", user.getUserId());
        assertEquals("user1", user.getUserName());
        assertEquals("user1|project1", user.getToken());
        assertEquals("project1", user.getProjectId());
        assertEquals("defaultDomain", user.getDomainId());
    }

    @Test
    void testTokenToUser_EmptyToken() {
        assertThrows(AgentStudioException.class, () -> SimpleAuthUtils.tokenToUser(""));
    }

    @Test
    void testTokenToUser_NullToken() {
        assertThrows(AgentStudioException.class, () -> SimpleAuthUtils.tokenToUser(null));
    }

    @Test
    void testTokenToUser_EmptyUserId() {
        when(pocConfig.getDefaultDomain()).thenReturn("defaultDomain");
        assertThrows(AgentStudioException.class, () -> SimpleAuthUtils.tokenToUser("|project1"));
    }

    @Test
    void testTokenToUser_MissingProjectId() {
        when(pocConfig.getDefaultDomain()).thenReturn("defaultDomain");
        assertThrows(AgentStudioException.class, () -> SimpleAuthUtils.tokenToUser("user1"));
    }

    @Test
    void testTokenToUser_EmptyProjectId() {
        assertThrows(AgentStudioException.class, () -> SimpleAuthUtils.tokenToUser("user1|"));
    }

    @Test
    void testParamToToken_FromParams() {
        when(pocConfig.getDefaultDomain()).thenReturn("0");
        when(pocConfig.getDefaultProject()).thenReturn("0");

        Map<String, String[]> params = new HashMap<>();
        params.put(SimpleConstants.UserInfo.USER_FIELD, new String[]{"testUser"});
        params.put(SimpleConstants.UserInfo.PROJECT_FIELD, new String[]{"testProject"});
        when(request.getParameterMap()).thenReturn(params);

        String token = SimpleAuthUtils.paramToToken(request, null);

        assertEquals("testUser|testProject", token);
    }

    @Test
    void testParamToToken_FromHeaders() {
        when(pocConfig.getDefaultProject()).thenReturn("0");

        when(request.getParameterMap()).thenReturn(Collections.emptyMap());
        when(request.getHeader(SimpleConstants.UserInfo.USER_FIELD)).thenReturn("headerUser");
        when(request.getHeader(SimpleConstants.UserInfo.PROJECT_FIELD)).thenReturn("headerProject");

        String token = SimpleAuthUtils.paramToToken(request, null);

        assertEquals("headerUser|headerProject", token);
    }

    @Test
    void testParamToToken_NoParamsNoHeadersNoExistToken() {
        when(pocConfig.getDefaultUserId()).thenReturn("defaultUser");
        when(pocConfig.getDefaultProject()).thenReturn("defaultProject");

        when(request.getParameterMap()).thenReturn(Collections.emptyMap());
        when(request.getHeader(SimpleConstants.UserInfo.USER_FIELD)).thenReturn(null);
        when(request.getHeader(SimpleConstants.UserInfo.PROJECT_FIELD)).thenReturn(null);

        String token = SimpleAuthUtils.paramToToken(request, null);

        assertEquals("defaultUser|defaultProject", token);
    }

    @Test
    void testParamToToken_NoParamsNoHeadersWithExistToken() {
        when(request.getParameterMap()).thenReturn(Collections.emptyMap());
        when(request.getHeader(SimpleConstants.UserInfo.USER_FIELD)).thenReturn(null);
        when(request.getHeader(SimpleConstants.UserInfo.PROJECT_FIELD)).thenReturn(null);

        String token = SimpleAuthUtils.paramToToken(request, "existingToken");

        assertEquals("", token);
    }

    @Test
    void testParamToToken_EmptyExistToken() {
        when(pocConfig.getDefaultUserId()).thenReturn("defaultUser");
        when(pocConfig.getDefaultProject()).thenReturn("defaultProject");

        when(request.getParameterMap()).thenReturn(Collections.emptyMap());
        when(request.getHeader(SimpleConstants.UserInfo.USER_FIELD)).thenReturn(null);
        when(request.getHeader(SimpleConstants.UserInfo.PROJECT_FIELD)).thenReturn(null);

        String token = SimpleAuthUtils.paramToToken(request, "");

        assertEquals("defaultUser|defaultProject", token);
    }

    @Test
    void testParamToToken_ParamsOverrideHeaders() {
        when(pocConfig.getDefaultProject()).thenReturn("0");

        Map<String, String[]> params = new HashMap<>();
        params.put(SimpleConstants.UserInfo.USER_FIELD, new String[]{"paramUser"});
        params.put(SimpleConstants.UserInfo.PROJECT_FIELD, new String[]{"paramProject"});
        when(request.getParameterMap()).thenReturn(params);

        String token = SimpleAuthUtils.paramToToken(request, null);

        assertEquals("paramUser|paramProject", token);
    }

    @Test
    void testParamToToken_OnlyUserId() {
        when(pocConfig.getDefaultProject()).thenReturn("defaultProj");

        Map<String, String[]> params = new HashMap<>();
        params.put(SimpleConstants.UserInfo.USER_FIELD, new String[]{"user1"});
        when(request.getParameterMap()).thenReturn(params);
        when(request.getHeader(SimpleConstants.UserInfo.PROJECT_FIELD)).thenReturn(null);

        String token = SimpleAuthUtils.paramToToken(request, null);

        assertEquals("user1|defaultProj", token);
    }

    @Test
    void testGetToken_BothPresent() {
        when(pocConfig.getDefaultProject()).thenReturn("0");

        String token = SimpleAuthUtils.getToken("user1", "project1");

        assertEquals("user1|project1", token);
    }

    @Test
    void testGetToken_EmptyUserId() {
        when(pocConfig.getDefaultProject()).thenReturn("0");

        String token = SimpleAuthUtils.getToken("", "project1");

        assertEquals("|project1", token);
    }

    @Test
    void testGetToken_NullUserId() {
        when(pocConfig.getDefaultProject()).thenReturn("0");

        String token = SimpleAuthUtils.getToken(null, "project1");

        assertEquals("|project1", token);
    }

    @Test
    void testGetToken_EmptyProjectId() {
        when(pocConfig.getDefaultProject()).thenReturn("defaultProj");

        String token = SimpleAuthUtils.getToken("user1", "");

        assertEquals("user1|defaultProj", token);
    }

    @Test
    void testGetToken_NullProjectId() {
        when(pocConfig.getDefaultProject()).thenReturn("defaultProj");

        String token = SimpleAuthUtils.getToken("user1", null);

        assertEquals("user1|defaultProj", token);
    }

    @Test
    void testGetToken_BothEmpty() {
        when(pocConfig.getDefaultProject()).thenReturn("defaultProj");

        String token = SimpleAuthUtils.getToken("", "");

        assertEquals("|defaultProj", token);
    }
}
