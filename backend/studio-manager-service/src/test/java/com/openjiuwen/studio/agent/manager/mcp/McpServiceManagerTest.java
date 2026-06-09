/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.mcp;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.manager.entity.McpServiceEntity;
import com.openjiuwen.studio.agent.manager.service.mcp.McpClientService;
import com.openjiuwen.studio.agent.manager.service.mcp.McpServiceManager;
import com.openjiuwen.studio.agent.manager.service.mcp.auth.IMcpBase;
import com.openjiuwen.studio.agent.manager.service.mcp.local.McpUpdateTask;
import com.openjiuwen.studio.agent.manager.service.mcp.model.dao.McpServerDao;
import com.openjiuwen.studio.agent.manager.service.mcp.model.dao.McpServiceDao;
import com.openjiuwen.studio.agent.manager.utils.CommonUtil;
import com.openjiuwen.studio.agent.manager.utils.McpUtil;
import com.openjiuwen.studio.agent.manager.service.workspace.WorkspaceMappingService;
import com.openjiuwen.studio.common.service.service.EncryptionAdapter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * McpServiceManager 单元测试
 */
@ExtendWith(MockitoExtension.class)
class McpServiceManagerTest {

    @InjectMocks
    private McpServiceManager mcpServiceManager;

    @Mock
    private McpServiceDao serviceDao;

    @Mock
    private McpServerDao serverDao;

    @Mock
    private EncryptionAdapter encryptionAdapter;

    @Mock
    private IMcpBase mcpBase;

    @Mock
    private McpClientService mcpClientService;

    @Mock
    private ThreadPoolTaskExecutor mcpTaskExecutor;

    @Mock
    private ThreadPoolTaskExecutor mcpUpdateTaskExecutor;

    @Mock
    private McpUpdateTask mcpUpdateTask;

    @Mock
    private WorkspaceMappingService
        workspaceMappingService;

    @Mock
    private McpUtil mcpUtil;

    private MockedStatic<RequestContextUtils> requestContextUtilsMock;

    private MockedStatic<CommonUtil> commonUtilMock;

    private static final String TENANT_ID = "test-tenant-id";

    private static final String AUTH_TOKEN = "test-auth-token";

    private static final String SERVER_CONFIG_PLAIN = "{\"url\":\"http://127.0.0.1:8080/sse\"}";

    private static final String SERVER_CONFIG_CIPHER = "E1-encrypted-content";

    @BeforeEach
    void setUp() {
        requestContextUtilsMock = Mockito.mockStatic(RequestContextUtils.class);
        commonUtilMock = Mockito.mockStatic(CommonUtil.class);

        lenient().when(RequestContextUtils.getRequestUserDomainId()).thenReturn(TENANT_ID);
        lenient().when(RequestContextUtils.getRequestAuthToken()).thenReturn(AUTH_TOKEN);

        // 模拟线程池同步执行
        lenient().doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            return null;
        }).when(mcpTaskExecutor).execute(any(Runnable.class));

        // 默认让maskUrlCredentials直接返回原值，避免NPE
        lenient().when(CommonUtil.maskUrlCredentials(anyString())).thenAnswer(i -> i.getArguments()[0]);

        lenient().when(mcpBase.exchangeTokenByAuth(any(), any(), anyString()))
            .thenAnswer(invocation -> invocation.getArgument(2));
    }

    @AfterEach
    void tearDown() {
        if (requestContextUtilsMock != null) {
            requestContextUtilsMock.close();
        }
        if (commonUtilMock != null) {
            commonUtilMock.close();
        }
    }

    @Test
    void testGetTools_ScoutPreventsCall() {
        int freePort = findAvailablePort();
        String deadUrl = "http://127.0.0.1:" + freePort + "/sse";

        McpServiceEntity entity = createMockServiceEntity("service-test", deadUrl);
        when(encryptionAdapter.decrypt(anyString())).thenReturn("{}");

        commonUtilMock.when(() -> CommonUtil.parseUrlFromMcpConfig(any(), anyInt())).thenReturn(deadUrl);

        mcpServiceManager.getMcpServiceToolList(entity);

    }

    @Test
    void testCollectAllExceptionInfo() {
        Exception rootCause = new IllegalArgumentException("Invalid configuration parameter");
        Exception middleLayer = new RuntimeException("Failed to initialize component", rootCause);
        Exception topLevel = new AgentStudioException(StudioError.UNEXPECTED_ERROR, middleLayer);

        String result = ReflectionTestUtils.invokeMethod(mcpServiceManager, "collectAllExceptionInfo", topLevel);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.contains("IllegalArgumentException"));
        Assertions.assertTrue(result.contains("RuntimeException"));
        Assertions.assertTrue(result.contains("Invalid configuration parameter"));
        Assertions.assertTrue(result.contains("Failed to initialize component"));
    }

    @Test
    void testMatchErrorByMessage() {
        StudioError error404 = ReflectionTestUtils.invokeMethod(mcpServiceManager, "matchErrorByMessage",
            "java.io.IOException: Server returned HTTP response code: 404 for URL: http://...", null);
        Assertions.assertEquals(StudioError.MCP_TARGET_NOT_FOUND, error404);

        StudioError error407 = ReflectionTestUtils.invokeMethod(mcpServiceManager, "matchErrorByMessage",
            "Received HTTP_PROXY_AUTH (407) code", null);
        Assertions.assertEquals(StudioError.MCP_AUTHENTICATION_FAILED, error407);

        StudioError error403 = ReflectionTestUtils.invokeMethod(mcpServiceManager, "matchErrorByMessage",
            "Access Denied: Forbidden 403", null);
        Assertions.assertEquals(StudioError.MCP_AUTHENTICATION_FAILED, error403);

        StudioError errorTimeout = ReflectionTestUtils.invokeMethod(mcpServiceManager, "matchErrorByMessage",
            "java.net.SocketTimeoutException: connect timed out", null);
        Assertions.assertEquals(StudioError.MCP_NETWORK_TIMEOUT, errorTimeout);

        StudioError errorDns = ReflectionTestUtils.invokeMethod(mcpServiceManager, "matchErrorByMessage",
            "java.net.UnknownHostException: mcp-test-host", null);
        Assertions.assertEquals(StudioError.MCP_UNKNOWN_HOST, errorDns);

        StudioError errorDefault = ReflectionTestUtils.invokeMethod(mcpServiceManager, "matchErrorByMessage",
            "Some random internal illegal state error", StudioError.UNEXPECTED_ERROR);
        Assertions.assertEquals(StudioError.UNEXPECTED_ERROR, errorDefault);
    }

    private int findAvailablePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (Exception e) {
            return 8080;
        }
    }

    /**
     * 测试：ProxySelector 抛出异常的覆盖
     */
    @Test
    void testQuickNetworkCheck_ProxySelectorException() {
        int port = findAvailablePort();
        // 这里的 URL 必须是连不通的，以便走到异常处理逻辑
        String url = "http://127.0.0.1:" + port + "/sse";
        McpServiceEntity entity = createMockServiceEntity("service-proxy-ex", url);

        when(encryptionAdapter.decrypt(anyString())).thenReturn("{}");
        commonUtilMock.when(() -> CommonUtil.parseUrlFromMcpConfig(any(), anyInt())).thenReturn(url);

        try (MockedStatic<java.net.ProxySelector> proxySelectorMock = Mockito.mockStatic(
            java.net.ProxySelector.class)) {
            proxySelectorMock.when(java.net.ProxySelector::getDefault).thenThrow(new RuntimeException("Proxy Error"));
            mcpServiceManager.getMcpServiceToolList(entity);
        }
    }

    /**
     * 测试：主流程重试逻辑 + 智能推断逻辑 (407 Conflict)
     */
    @Test
    void testGetTools_SmartInference_ProxyConflict() throws Exception {
        int port = findAvailablePort();
        String url = "http://127.0.0.1:" + port + "/sse";
        McpServiceEntity entity = createMockServiceEntity("service-smart-inference", url);

        when(encryptionAdapter.decrypt(anyString())).thenReturn("{}");
        commonUtilMock.when(() -> CommonUtil.parseUrlFromMcpConfig(any(), anyInt())).thenReturn(url);

        Thread serverThread = new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                try (Socket clientSocket = serverSocket.accept();
                    BufferedReader reader = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
                    OutputStream output = clientSocket.getOutputStream()) {

                    String line = reader.readLine();
                    while (line != null && !line.isEmpty()) {
                        line = reader.readLine();
                    }

                    output.write("HTTP/1.1 200 OK\r\n".getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    output.write("Content-Length: 0\r\n".getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    output.write("\r\n".getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    output.flush();
                }
            } catch (Exception ignored) {
                // Ignore socket exceptions during test shutdown
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();
        Thread.sleep(500); // Ensure server is up

        RuntimeException timeoutException = new RuntimeException("java.net.SocketTimeoutException: connect timed out");

        lenient().when(mcpClientService.getMcpServiceToolList(any(), any(), any(), any())).thenThrow(timeoutException);

        AgentStudioException ex = Assertions.assertThrows(AgentStudioException.class, () -> {
            mcpServiceManager.getMcpServiceToolList(entity);
        });

        Assertions.assertEquals(StudioError.MCP_AUTHENTICATION_FAILED, ex.getErrorCode());
    }

    /**
     * 测试：主流程循环重试逻辑 (Loop & Retry)
     */
    @Test
    void testGetTools_MainLoopRetry_Success() throws Exception {
        int port = findAvailablePort();
        String url = "http://127.0.0.1:" + port + "/sse";
        McpServiceEntity entity = createMockServiceEntity("service-loop-success", url);

        when(encryptionAdapter.decrypt(anyString())).thenReturn("{}");
        commonUtilMock.when(() -> CommonUtil.parseUrlFromMcpConfig(any(), anyInt())).thenReturn(url);

        CountDownLatch serverReady = new CountDownLatch(1);
        Thread serverThread = new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(port, 50, InetAddress.getByName("127.0.0.1"))) {
                serverSocket.setReuseAddress(true);
                serverReady.countDown();

                while (!serverSocket.isClosed()) {
                    try (Socket clientSocket = serverSocket.accept()) {
                        clientSocket.getOutputStream()
                            .write("HTTP/1.1 200 OK\r\n\r\n".getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    } catch (Exception e) {
                        // Client might disconnect early, ignore
                    }
                }
            } catch (Exception ignored) {
                // Server socket closed, exit thread
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();

        Assertions.assertTrue(serverReady.await(2, TimeUnit.SECONDS), "Server start timeout");

        // 构造“完美”的异常 (注意：要避免被 scout 拦截，scout 会先通)
        AgentStudioException retryableException = new AgentStudioException(StudioError.MCP_NETWORK_CONNECTION_ERROR,
            "Something went wrong: internal error occurred");

        when(mcpClientService.getMcpServiceToolList(any(), any(), any(), any())).thenThrow(retryableException) // 第1次
            .thenThrow(retryableException) // 第2次
            .thenReturn(Collections.emptyList()); // 第3次成功

        String result = mcpServiceManager.getMcpServiceToolList(entity);

        Assertions.assertEquals("", result);
        verify(mcpClientService, times(3)).getMcpServiceToolList(any(), any(), any(), any());
    }

    /**
     * 测试：Unknown Host (覆盖 quickNetworkCheck 内部 catch UnknownHostException)
     */
    @Test
    void testQuickNetworkCheck_UnknownHost() {
        String url = "http://hostname.invalid:8080";
        McpServiceEntity entity = createMockServiceEntity("dns-fail", url);

        when(encryptionAdapter.decrypt(anyString())).thenReturn("{}");
        commonUtilMock.when(() -> CommonUtil.parseUrlFromMcpConfig(any(), anyInt())).thenReturn(url);

        mcpServiceManager.getMcpServiceToolList(entity);

    }

    // --- 辅助方法 ---

    private McpServiceEntity createMockServiceEntity(String id) {
        return createMockServiceEntity(id, "http://127.0.0.1:8080/sse");
    }

    private McpServiceEntity createMockServiceEntity(String id, String url) {
        McpServiceEntity entity = new McpServiceEntity();
        entity.setId(id);
        entity.setName("Test-Service");
        entity.setFcInstanceUrl(url);
        entity.setServerConfig("encrypted-config");
        entity.setAuthType("NONE");
        entity.setOrgType("SSE");
        return entity;
    }

    /**
     * 测试：带 Header 的网络探测成功
     * 专注于测试 quickNetworkCheck 是否真的发送了 Header。
     */
    @Test
    void testGetTools_WithHeaders_AuthSuccess() throws Exception {
        int port = findAvailablePort();
        String url = "http://127.0.0.1:" + port + "/sse";
        String testToken = "";

        McpServiceEntity entity = createMockServiceEntity("service-auth-success", url);
        entity.setAuthType(CommonConstant.MCP_AUTH_TYPE.USE_IAM_TOKEN);
        when(encryptionAdapter.decrypt(anyString())).thenReturn("{}");
        commonUtilMock.when(() -> CommonUtil.parseUrlFromMcpConfig(any(), anyInt())).thenReturn(url);
        requestContextUtilsMock.when(RequestContextUtils::getRequestAuthToken).thenReturn(testToken);
        when(mcpClientService.getMcpServiceToolList(any(), any(), any(), any())).thenReturn(Collections.emptyList());

        CountDownLatch serverLatch = new CountDownLatch(1);
        final boolean[] headerReceived = {false};

        Thread serverThread = new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                serverLatch.countDown();
                // [Fix] 使用 try-with-resources 自动关闭流
                try (Socket client = serverSocket.accept();
                    BufferedReader reader = new BufferedReader(
                        new InputStreamReader(client.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
                    OutputStream out = client.getOutputStream()) {

                    String line;
                    while ((line = reader.readLine()) != null && !line.isEmpty()) {
                        if (line.contains("X-Auth-Token: " + testToken)) {
                            headerReceived[0] = true;
                        }
                    }
                    out.write("HTTP/1.1 200 OK\r\n\r\n".getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    out.flush();
                }
            } catch (Exception ignored) {
                // Ignore socket exceptions during test shutdown
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();

        Assertions.assertTrue(serverLatch.await(2, TimeUnit.SECONDS));

        mcpServiceManager.getMcpServiceToolList(entity);
        verify(mcpClientService, times(1)).getMcpServiceToolList(any(), any(), any(), any());
    }

}
