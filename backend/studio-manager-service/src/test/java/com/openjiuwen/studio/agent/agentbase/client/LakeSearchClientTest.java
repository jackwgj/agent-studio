/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.client;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.agentbase.common.enums.LakeSearchApi;
import com.openjiuwen.studio.agent.agentbase.model.ResultModel;
import com.openjiuwen.studio.agent.agentbase.service.knowledgerepo.KnowledgeConnectionRouterService;
import com.openjiuwen.studio.agent.agentbase.service.knowledgerepo.connection.LakeSearchConnection;
import com.openjiuwen.studio.agent.agentbase.service.knowledgerepo.knowledgesourceprovider.LakeSearchConnectionProvider;
import com.openjiuwen.studio.agent.agentbase.utils.HttpClientUtils;
import com.openjiuwen.studio.agent.common.dto.knowledge.KerberosAuth;
import com.openjiuwen.studio.agent.foundation.base.exception.AgentBaseException;
import com.openjiuwen.studio.agent.foundation.base.exception.ErrorCode;
import com.openjiuwen.studio.agent.foundation.base.utils.RandomUtil;
import com.openjiuwen.studio.agent.foundation.i18n.I18nUtils;

import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.PrivilegedAction;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

@MockitoSettings(strictness = Strictness.LENIENT)
class LakeSearchClientTest {

    @Mock
    private LakeSearchConnectionProvider lakeSearchConnectionProvider;

    @Mock
    private KnowledgeConnectionRouterService knowledgeConnectionRouterService;

    private com.openjiuwen.studio.agent.agentbase.client.AgentBaseLakeSearchClient agentBaseLakeSearchClient;

    @BeforeEach
    void init() {
        agentBaseLakeSearchClient = new AgentBaseLakeSearchClient(lakeSearchConnectionProvider, knowledgeConnectionRouterService);
        ReflectionTestUtils.setField(agentBaseLakeSearchClient, "authMode", "kerberos");
        Mockito.when(knowledgeConnectionRouterService.findConnectionIdByKerberosAuthMode())
            .thenReturn("testConnectionId");
        Mockito.when(lakeSearchConnectionProvider.getKnowledgeSourceConnection("testConnectionId")).thenReturn(null);
    }

    @Test
    void initTest() throws LoginException, IOException {
        agentBaseLakeSearchClient.init();
    }

    @Test
    void test_sendAction_should_succeed_when_upload_file() throws IOException {
        // set up
        String endpoint = String.format(LakeSearchApi.UPLOAD_FILE.getEndpoint(), "test_knowledge_base_id");
        MultipartFile file = new MockMultipartFile("fake_file_name", "fake_file_name.txt", "text/plain",
            "fake_file_content".getBytes(StandardCharsets.UTF_8));

        LakeSearchConnection lakeSearchConnection = new LakeSearchConnection();
        lakeSearchConnection.setOcrEnable(false);
        lakeSearchConnection.setConnectionId("test_connection_id");
        lakeSearchConnection.setEndpoint("http://localhost:9200");
        KerberosAuth kerberosAuth = new KerberosAuth();
        kerberosAuth.setPort("8008");
        kerberosAuth.setClusterIps("localhost");
        kerberosAuth.setKrb5File("mock_krb");
        kerberosAuth.setHostNames("localhost");
        lakeSearchConnection.setKerberosAuth(kerberosAuth);
        lakeSearchConnection.setAuthMode("kerberos");
        when(lakeSearchConnectionProvider.getKnowledgeSourceConnection("testConnectionId")).thenReturn(
            lakeSearchConnection);
        // run test
        ResultModel resultModel = agentBaseLakeSearchClient.sendAction(endpoint, null, LakeSearchApi.UPLOAD_FILE, file);

    }

    @Test
    void test_delete_file() {

        String endpoint = String.format(LakeSearchApi.DELETE_FILE.getEndpoint(), "test_knowledge_base_id",
            "test_file_id");
        LakeSearchConnection lakeSearchConnection = new LakeSearchConnection();
        lakeSearchConnection.setOcrEnable(false);
        lakeSearchConnection.setConnectionId("test_connection_id");
        lakeSearchConnection.setEndpoint("http://localhost:9200");
        KerberosAuth kerberosAuth = new KerberosAuth();
        kerberosAuth.setPort("8008");
        kerberosAuth.setClusterIps("localhost");
        kerberosAuth.setKrb5File("mock_krb");
        kerberosAuth.setHostNames("localhost");
        lakeSearchConnection.setKerberosAuth(kerberosAuth);
        lakeSearchConnection.setAuthMode("kerberos");
        when(lakeSearchConnectionProvider.getKnowledgeSourceConnection("testConnectionId")).thenReturn(
            lakeSearchConnection);
        ResultModel resultModel = agentBaseLakeSearchClient.sendAction(endpoint, null, LakeSearchApi.DELETE_FILE, null);

    }

    @Test
    void test_listKnowledgeRepos() {

        String endpoint = String.format(LakeSearchApi.QUERY_KNOWLEDGE_REPOS.getEndpoint());
        LakeSearchConnection lakeSearchConnection = new LakeSearchConnection();
        lakeSearchConnection.setOcrEnable(false);
        lakeSearchConnection.setConnectionId("test_connection_id");
        lakeSearchConnection.setEndpoint("http://localhost:9200");
        KerberosAuth kerberosAuth = new KerberosAuth();
        kerberosAuth.setPort("8008");
        kerberosAuth.setClusterIps("localhost");
        kerberosAuth.setKrb5File("mock_krb");
        kerberosAuth.setHostNames("localhost");
        lakeSearchConnection.setKerberosAuth(kerberosAuth);
        lakeSearchConnection.setAuthMode("kerberos");
        when(lakeSearchConnectionProvider.getKnowledgeSourceConnection("testConnectionId")).thenReturn(
            lakeSearchConnection);
        try (MockedStatic<HttpClientUtils> mockedStaticHttpClients = mockStatic(HttpClientUtils.class,
            RETURNS_DEEP_STUBS);
            MockedStatic<ResponseEntity> mockedStaticResponseEntity = mockStatic(ResponseEntity.class,
                RETURNS_DEEP_STUBS)) {
            // Arrange
            CloseableHttpClient httpclient = mock(CloseableHttpClient.class, Answers.RETURNS_DEEP_STUBS);
            BasicClassicHttpResponse basicResponse = new BasicClassicHttpResponse(HttpStatus.SC_UNAUTHORIZED,
                "Unauthorized");
            basicResponse.addHeader("WWW-Authenticate", "Negotiate");
            CloseableHttpResponse response = CloseableHttpResponse.adapt(spy(basicResponse));
            mockedStaticHttpClients.when(() -> HttpClientUtils.getHttpClient()).thenReturn(httpclient);
            when(httpclient.execute(any(ClassicHttpRequest.class))).thenReturn(response);
            ResponseEntity<byte[]> responseEntity = new ResponseEntity<>(HttpStatusCode.valueOf(200));
            mockedStaticResponseEntity.when(() -> ResponseEntity.ok(any(Object.class))).thenReturn(responseEntity);

            // Act
            ResultModel resultModel = agentBaseLakeSearchClient.sendAction(endpoint, null, LakeSearchApi.QUERY_KNOWLEDGE_REPOS,
                null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void test_delete_files() throws IOException {
        String endpoint = String.format(LakeSearchApi.DELETE_FILE.getEndpoint(), "test_knowledge_base_id", "file_id");

        LakeSearchConnection lakeSearchConnection = new LakeSearchConnection();
        lakeSearchConnection.setOcrEnable(false);
        lakeSearchConnection.setConnectionId("test_connection_id");
        lakeSearchConnection.setEndpoint("http://localhost:9200");
        KerberosAuth kerberosAuth = new KerberosAuth();
        kerberosAuth.setPort("8008");
        kerberosAuth.setClusterIps("localhost");
        kerberosAuth.setKrb5File("mock_krb");
        kerberosAuth.setHostNames("localhost");
        lakeSearchConnection.setKerberosAuth(kerberosAuth);
        lakeSearchConnection.setAuthMode("kerberos");

        when(lakeSearchConnectionProvider.getKnowledgeSourceConnection("testConnectionId")).thenReturn(
            lakeSearchConnection);

        try (MockedStatic<HttpClients> mockedStaticHttpClients = mockStatic(HttpClients.class, RETURNS_DEEP_STUBS);
            MockedStatic<ResponseEntity> mockedStaticResponseEntity = mockStatic(ResponseEntity.class,
                RETURNS_DEEP_STUBS)) {
            // Arrange
            CloseableHttpClient httpclient = mock(CloseableHttpClient.class, Answers.RETURNS_DEEP_STUBS);
            CloseableHttpResponse response = mock(CloseableHttpResponse.class, Answers.RETURNS_DEEP_STUBS);
            mockedStaticHttpClients.when(() -> HttpClients.createDefault()).thenReturn(httpclient);
            when(httpclient.execute(any(ClassicHttpRequest.class))).thenReturn(response);
            response.setCode(200);

            ResponseEntity<byte[]> responseEntity = new ResponseEntity<>(HttpStatusCode.valueOf(200));
            mockedStaticResponseEntity.when(() -> ResponseEntity.ok(any(Object.class))).thenReturn(responseEntity);

            // Act
            ResultModel resultModel = agentBaseLakeSearchClient.sendAction(endpoint, "test_request", LakeSearchApi.DELETE_FILE,
                null);
        }
    }

    @Test
    void test_sendAction_should_return_not_null() throws Exception {
        try (MockedStatic<Subject> mockedStaticSubject = mockStatic(Subject.class, RETURNS_DEEP_STUBS);
            MockedStatic<RandomUtil> mockedStaticRandomUtil = mockStatic(RandomUtil.class, RETURNS_DEEP_STUBS);
            MockedStatic<ClassicRequestBuilder> mockedStaticClassicRequestBuilder = mockStatic(
                ClassicRequestBuilder.class, RETURNS_DEEP_STUBS);
            MockedStatic<HttpClients> mockedStaticHttpClients = mockStatic(HttpClients.class, RETURNS_DEEP_STUBS);
            MockedStatic<MultipartEntityBuilder> mockedStaticMultipartEntityBuilder = mockStatic(
                MultipartEntityBuilder.class, RETURNS_DEEP_STUBS);
            MockedStatic<I18nUtils> mockedStaticI18nUtils = mockStatic(I18nUtils.class, RETURNS_DEEP_STUBS);
            MockedStatic<ResponseEntity> mockedStaticResponseEntity = mockStatic(ResponseEntity.class,
                RETURNS_DEEP_STUBS);
            MockedStatic<EntityUtils> mockedStaticEntityUtils = mockStatic(EntityUtils.class, RETURNS_DEEP_STUBS)) {
            // Given
            ResultModel resultModel = new ResultModel();
            mockedStaticSubject.when(() -> Subject.doAs(any(Subject.class), any(PrivilegedAction.class)))
                .thenReturn(resultModel);

            mockedStaticRandomUtil.when(() -> RandomUtil.getRandom(anyInt())).thenReturn(0);

            ClassicHttpRequest classicHttpRequest = mock(ClassicHttpRequest.class, Answers.RETURNS_DEEP_STUBS);
            mockedStaticClassicRequestBuilder.when(() -> ClassicRequestBuilder.delete(anyString()).build())
                .thenReturn(classicHttpRequest);
            ClassicHttpRequest classicHttpRequest1 = mock(ClassicHttpRequest.class, Answers.RETURNS_DEEP_STUBS);
            mockedStaticClassicRequestBuilder.when(() -> ClassicRequestBuilder.put(anyString()).build())
                .thenReturn(classicHttpRequest1);
            ClassicHttpRequest classicHttpRequest2 = mock(ClassicHttpRequest.class, Answers.RETURNS_DEEP_STUBS);
            mockedStaticClassicRequestBuilder.when(() -> ClassicRequestBuilder.post(anyString()).build())
                .thenReturn(classicHttpRequest2);
            ClassicHttpRequest classicHttpRequest3 = mock(ClassicHttpRequest.class, Answers.RETURNS_DEEP_STUBS);
            mockedStaticClassicRequestBuilder.when(() -> ClassicRequestBuilder.get(anyString()).build())
                .thenReturn(classicHttpRequest3);

            CloseableHttpClient httpclient = mock(CloseableHttpClient.class, Answers.RETURNS_DEEP_STUBS);
            CloseableHttpResponse response = mock(CloseableHttpResponse.class, Answers.RETURNS_DEEP_STUBS);
            when(httpclient.execute(any(ClassicHttpRequest.class))).thenReturn(response);
            mockedStaticHttpClients.when(() -> HttpClients.createDefault()).thenReturn(httpclient);

            MultipartEntityBuilder builder = mock(MultipartEntityBuilder.class, Answers.RETURNS_DEEP_STUBS);
            HttpEntity multipart = mock(HttpEntity.class, Answers.RETURNS_DEEP_STUBS);
            when(builder.build()).thenReturn(multipart);
            mockedStaticMultipartEntityBuilder.when(() -> MultipartEntityBuilder.create()).thenReturn(builder);

            mockedStaticI18nUtils.when(
                    () -> I18nUtils.getMessage(eq(ErrorCode.FAIL_TO_UPLOAD_KNOWLEDGE_REPO_FILE.getCode())))
                .thenReturn("not_empty");

            ResponseEntity<byte[]> responseEntity = new ResponseEntity(HttpStatusCode.valueOf(200));
            mockedStaticResponseEntity.when(() -> ResponseEntity.ok(any(Object.class))).thenReturn(responseEntity);

            byte[] byteVarArray = new byte[0];
            mockedStaticEntityUtils.when(() -> EntityUtils.toByteArray(any(HttpEntity.class))).thenReturn(byteVarArray);

            when(knowledgeConnectionRouterService.findConnectionIdByKerberosAuthMode()).thenReturn("not_empty");

            LakeSearchConnection lakeSearchConnection = new LakeSearchConnection();
            lakeSearchConnection.setOcrEnable(false);
            lakeSearchConnection.setConnectionId("test_connection_id");
            lakeSearchConnection.setEndpoint("http://localhost:9200");
            KerberosAuth kerberosAuth = new KerberosAuth();
            kerberosAuth.setPort("8008");
            kerberosAuth.setClusterIps("localhost");
            kerberosAuth.setKrb5File("mock_krb");
            kerberosAuth.setHostNames("localhost");
            lakeSearchConnection.setKerberosAuth(kerberosAuth);
            lakeSearchConnection.setAuthMode("kerberos");
            when(knowledgeConnectionRouterService.findConnectionIdByKerberosAuthMode()).thenReturn("testConnectionId");
            when(lakeSearchConnectionProvider.getKnowledgeSourceConnection("testConnectionId")).thenReturn(
                lakeSearchConnection);

            Resource resource = mock(Resource.class, Answers.RETURNS_DEEP_STUBS);
            String endpoint = String.format(LakeSearchApi.DELETE_FILE.getEndpoint(), "test_knowledge_base_id",
                "file_id");
            // When
            agentBaseLakeSearchClient.sendAction(endpoint, "test_request", LakeSearchApi.DELETE_FILE, null);
            ;

        }
    }

    @Test
    void test_setEntityForUpload_throw_exception() {
        Resource resource1 = new InputStreamResource(new ByteArrayInputStream(new byte[0])) {
            @NotNull
            @Override
            public String getFilename() {
                return "";
            }

            @Override
            public long contentLength() {
                return 0;
            }
        };
        assertThrows(AgentBaseException.class, () -> {
            ReflectionTestUtils.invokeMethod(agentBaseLakeSearchClient, "setEntityForUpload", LakeSearchApi.UPLOAD_FILE,
                resource1, ClassicRequestBuilder.post("").build());
        });
    }
}