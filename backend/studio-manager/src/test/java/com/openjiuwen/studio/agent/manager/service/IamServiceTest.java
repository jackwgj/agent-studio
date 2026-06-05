/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.utils.OkHttpClientUtils;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.dto.iam.GetIamUserResponse;
import com.openjiuwen.studio.agent.manager.dto.iam.IamUserGroupPermissionRsp;
import com.openjiuwen.studio.agent.manager.dto.iam.IamUserGroupRsp;
import com.openjiuwen.studio.agent.manager.utils.IamServiceUtils;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class IamServiceTest {
    @Mock
    private OkHttpClientUtils okHttpClientUtils;

    @Mock()
    private OkHttpClient mockHttpClient;

    @Mock
    private Call call;

    @InjectMocks
    private IamServiceUtils iamServiceUtils;

    @Mock
    private Response mockTokenResponse;

    @Mock
    private Response mockResponse;

    @BeforeEach
    void setUp() throws Exception {
        ReflectionTestUtils.setField(iamServiceUtils, "iamUrl", "https://iam.com");
    }

    @ParameterizedTest
    @CsvSource( {
        "tokenA, domainA, aaa"
    })
    void test_queryDomainToken_should_return_expected(String token, String domainName, String expected)
        throws Exception {
        setMockHttpClient();
        mockTokenResponse = new Response.Builder().request(new Request.Builder().url("https://iam.com").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .header("X-Subject-Token", "aaa")
            .body(ResponseBody.create("", MediaType.get("text/plain")))
            .build();

        when(call.execute()).thenReturn(mockTokenResponse);

        String result = iamServiceUtils.queryDomainToken(token, domainName);

        assertEquals(expected, result);
    }

    @Test
    void test_queryDomainToken_return_empty() throws Exception {
        setMockHttpClient();
        String token = "tokenA";
        String domainName = "domainA";
        mockTokenResponse = new Response.Builder().request(new Request.Builder().url("https://iam.com").build())
            .protocol(Protocol.HTTP_1_1)
            .code(500)
            .message("OK")
            .header("X-Subject-Token", "aaa")
            .body(ResponseBody.create("", MediaType.get("application/json")))
            .build();

        when(call.execute()).thenReturn(mockTokenResponse);

        String result = iamServiceUtils.queryDomainToken(token, domainName);

        assertEquals("", result);
    }

    @Test
    void test_queryDomainTokenFromCache_tokenNotInCache_shouldReturnNewToken() throws Exception {
        setMockHttpClient();
        Call mockCall = mock(Call.class);
        when(mockHttpClient.newCall(any())).thenReturn(mockCall);

        mockTokenResponse = new Response.Builder().request(new Request.Builder().url("https://iam.com").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .header("X-Subject-Token", "newToken")
            .body(ResponseBody.create("", MediaType.get("text/plain")))
            .build();
        when(mockCall.execute()).thenReturn(mockTokenResponse);

        try (MockedStatic<RequestContextUtils> mocked = mockStatic(RequestContextUtils.class)) {
            mocked.when(RequestContextUtils::getRequestAuthToken).thenReturn("authToken");
            mocked.when(RequestContextUtils::getRequestUserDomainName).thenReturn("testDomain");
            mocked.when(RequestContextUtils::getRequestUserDomainId).thenReturn("domain123");
            mocked.when(RequestContextUtils::getRequestUserId).thenReturn("user123");

            String result = iamServiceUtils.queryDomainTokenFromCache("");
            assertEquals("newToken", result);

            String tokenFromCache = iamServiceUtils.queryDomainTokenFromCache("");
            assertEquals("newToken", tokenFromCache);
        }
    }

    @Test
    void test_queryIamUserList_successfulResponse_shouldReturnParsedResponse() throws Exception {
        setMockHttpClient();

        // Set up IAM context mock
        try (MockedStatic<RequestContextUtils> mocked = mockStatic(RequestContextUtils.class)) {
            mocked.when(RequestContextUtils::getRequestAuthToken).thenReturn("validToken");
            mocked.when(RequestContextUtils::getRequestUserDomainName).thenReturn("testDomain");
            mocked.when(RequestContextUtils::getRequestUserDomainId).thenReturn("domain123");
            mocked.when(RequestContextUtils::getRequestUserId).thenReturn("user123");

            GetIamUserResponse emptyResult = iamServiceUtils.queryIamUserList("");
            assertNotNull(emptyResult);
            assertNull(emptyResult.getUsers());

            reset(call);

            when(call.execute()).thenReturn(mockResponse);

            String validJsonResponse = "{\"users\":[{\"id\":\"user1\",\"name\":\"testUser\"}]}";
            when(mockResponse.code()).thenReturn(200);
            when(mockResponse.body()).thenReturn(
                ResponseBody.create(validJsonResponse, MediaType.get("application/json")));

            // Second call with valid token should return users list
            GetIamUserResponse result = iamServiceUtils.queryIamUserList("validToken");

            assertNotNull(result);
            assertNotNull(result.getUsers());
            assertFalse(result.getUsers().isEmpty());
            assertEquals("user1", result.getUsers().get(0).getId());
            assertEquals("testUser", result.getUsers().get(0).getName());
        }
    }

    @Test
    void test_queryIamUserGroupList_responseBodyNull_shouldReturnEmptyResponse() throws Exception {
        try (MockedStatic<RequestContextUtils> mocked = mockStatic(RequestContextUtils.class)) {
            setMockHttpClient();
            mocked.when(RequestContextUtils::getRequestUserId).thenReturn("userId123");

            when(mockHttpClient.newCall(any(Request.class)).execute()).thenReturn(mockResponse);
            when(mockResponse.body()).thenReturn(null);

            IamUserGroupRsp result = iamServiceUtils.queryIamUserGroupList("validToken");

            assertNotNull(result);
            assertNull(result.getGroups());
        }
    }

    @Test
    void test_queryIamUserGroupList_responseBodyBlank_shouldReturnEmptyResponse() throws Exception {
        setMockHttpClient();
        try (MockedStatic<RequestContextUtils> mocked = mockStatic(RequestContextUtils.class)) {
            mocked.when(RequestContextUtils::getRequestUserId).thenReturn("userId123");

            when(mockHttpClient.newCall(any(Request.class)).execute()).thenReturn(mockResponse);
            when(mockResponse.body()).thenReturn(ResponseBody.create("", MediaType.get("text/plain")));

            IamUserGroupRsp result = iamServiceUtils.queryIamUserGroupList("validToken");

            assertNotNull(result);
            assertNull(result.getGroups());
        }
    }

    @Test
    void test_queryIamUserGroupPermissionList_success() throws Exception {
        try (MockedStatic<RequestContextUtils> mocked = mockStatic(RequestContextUtils.class)) {
            setMockHttpClient();
            IamUserGroupPermissionRsp response = iamServiceUtils.queryIamUserGroupPermissionList("", "groupId123");

            assertNull(response.getRoles());

            mocked.when(RequestContextUtils::getRequestUserId).thenReturn("userId123");
            String expectedBody = "{\n" + "  \"roles\": [\n"
                + "    {\"id\": \"role1\", \"name\": \"admin\", \"description\": \"Administrator role\", \"flag\": \"active\", \"catalog\": \"system\", \"display_name\": \"Admin Role\", \"type\": \"admin\"},\n"
                + "    {\"id\": \"role2\", \"name\": \"user\", \"description\": \"Standard user role\", \"flag\": \"active\", \"catalog\": \"system\", \"display_name\": \"User Role\", \"type\": \"user\"}\n"
                + "  ],\n" + "  \"links\": {\n" + "    \"self\": \"https://iam.example.com/self\",\n"
                + "    \"next\": \"https://iam.example.com/related\"\n" + "  }\n" + "}";

            when(mockHttpClient.newCall(any(Request.class)).execute()).thenReturn(mockResponse);
            when(mockResponse.body()).thenReturn(ResponseBody.create(expectedBody, MediaType.get("application/json")));

            response = iamServiceUtils.queryIamUserGroupPermissionList("userToken", "groupId123");

            assertNotNull(response);
            assertNotNull(response.getRoles());
            assertEquals(2, response.getRoles().size());

            assertNotNull(response.getLinks());
            assertEquals("https://iam.example.com/self", response.getLinks().getSelf());
            assertEquals("https://iam.example.com/related", response.getLinks().getNext());
        }
    }

    void setMockHttpClient() {
        when(okHttpClientUtils.getHttpClient()).thenReturn(mockHttpClient);
        when(mockHttpClient.newCall(any())).thenReturn(call);
    }

    @Test
    void test_queryIamUserList_invalidStatusCode_shouldReturnEmptyResponse() throws Exception {
        setMockHttpClient();
        try (MockedStatic<RequestContextUtils> mocked = mockStatic(RequestContextUtils.class)) {
            mocked.when(RequestContextUtils::getRequestAuthToken).thenReturn("validToken");
            mocked.when(RequestContextUtils::getRequestUserDomainName).thenReturn("testDomain");
            mocked.when(RequestContextUtils::getRequestUserDomainId).thenReturn("domain123");
            mocked.when(RequestContextUtils::getRequestUserId).thenReturn("user123");

            when(call.execute()).thenReturn(mockResponse);
            when(mockResponse.code()).thenReturn(500); // Invalid status code
            String errorBody = "{\"error\":\"Unauthorized\",\"message\":\"Invalid token\"}";
            when(mockResponse.body()).thenReturn(ResponseBody.create(errorBody, MediaType.get("application/json")));

            GetIamUserResponse result = iamServiceUtils.queryIamUserList("validToken");

            assertNotNull(result);
            assertNull(result.getUsers());
        }
    }

    @Test
    void test_queryIamUserList_responseBodyNull_shouldReturnEmptyResponse() throws Exception {
        setMockHttpClient();
        try (MockedStatic<RequestContextUtils> mocked = mockStatic(RequestContextUtils.class)) {
            mocked.when(RequestContextUtils::getRequestAuthToken).thenReturn("validToken");
            mocked.when(RequestContextUtils::getRequestUserDomainName).thenReturn("testDomain");
            mocked.when(RequestContextUtils::getRequestUserDomainId).thenReturn("domain123");
            mocked.when(RequestContextUtils::getRequestUserId).thenReturn("user123");

            when(call.execute()).thenReturn(mockResponse);
            when(mockResponse.code()).thenReturn(200); // Valid status code
            when(mockResponse.body()).thenReturn(null); // Null body

            GetIamUserResponse result = iamServiceUtils.queryIamUserList("validToken");

            assertNotNull(result);
            assertNull(result.getUsers());
        }
    }

    @Test
    void test_queryIamUserList_responseBodyBlank_shouldReturnEmptyResponse() throws Exception {
        setMockHttpClient();
        try (MockedStatic<RequestContextUtils> mocked = mockStatic(RequestContextUtils.class)) {
            mocked.when(RequestContextUtils::getRequestAuthToken).thenReturn("validToken");
            mocked.when(RequestContextUtils::getRequestUserDomainName).thenReturn("testDomain");
            mocked.when(RequestContextUtils::getRequestUserDomainId).thenReturn("domain123");
            mocked.when(RequestContextUtils::getRequestUserId).thenReturn("user123");

            when(call.execute()).thenReturn(mockResponse);
            when(mockResponse.code()).thenReturn(200); // Valid status code
            when(mockResponse.body()).thenReturn(ResponseBody.create("", MediaType.get("text/plain"))); // Empty body

            GetIamUserResponse result = iamServiceUtils.queryIamUserList("validToken");

            assertNotNull(result);
            assertNull(result.getUsers());
        }
    }
}
