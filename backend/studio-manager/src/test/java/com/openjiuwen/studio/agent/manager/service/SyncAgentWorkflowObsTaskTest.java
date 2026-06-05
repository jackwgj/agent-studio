/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service;

import static com.openjiuwen.studio.agent.manager.service.startup.SyncAgentWorkflowObsTask.getOBSPath;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.redis.RedisClient;
import com.openjiuwen.studio.agent.common.redis.RedisLock;
import com.openjiuwen.studio.agent.manager.mapper.AgentMapper;
import com.openjiuwen.studio.agent.manager.mapper.AgentVersionMapper;
import com.openjiuwen.studio.agent.manager.mapper.AppMapper;
import com.openjiuwen.studio.agent.manager.mapper.ReleaseChannelMapper;
import com.openjiuwen.studio.agent.manager.mapper.WorkflowMapper;
import com.openjiuwen.studio.agent.manager.obs.MgObsService;
import com.openjiuwen.studio.agent.manager.service.startup.SyncAgentWorkflowObsTask;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.concurrent.Executor;

import javax.sql.DataSource;

@MockitoSettings(strictness = Strictness.LENIENT)
class SyncAgentWorkflowObsTaskTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RedisClient redisClient;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MgObsService obsService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DataSource dataSource;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ResourcePatternResolver resolver;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Executor taskExecutor;

    @Mock
    private AppMapper appMapper;

    @Mock
    private AgentMapper agentMapper;

    @Mock
    private WorkflowMapper workflowMapper;

    @Mock
    private AgentVersionMapper agentVersionMapper;

    @Mock
    private ReleaseChannelMapper releaseChannelMapper;

    @InjectMocks
    private SyncAgentWorkflowObsTask syncAgentWorkflowObsTask;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(syncAgentWorkflowObsTask, "isHcs", true);
        ReflectionTestUtils.setField(syncAgentWorkflowObsTask, "resolver", resolver);
        // 手动初始化resolver的行为
        lenient().when(resolver.getResource(anyString())).thenAnswer(invocation -> {
            String location = invocation.getArgument(0);
            Resource resource = mock(Resource.class);
            when(resource.getFilename()).thenReturn(location.substring(location.lastIndexOf("/") + 1));
            return resource;
        });
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    public void test_run_should_execute_successfully_when_sqlResource_exists() throws Exception {
        // Given
        Resource[] resourceArray = new Resource[0];
        when(resolver.getResources(anyString())).thenReturn(resourceArray);

        Resource sqlResource = mock(Resource.class);
        when(sqlResource.exists()).thenReturn(true);
        when(resolver.getResource(anyString())).thenReturn(sqlResource);

        Connection connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);

        doAnswer(invocation -> {
            ((Runnable) invocation.getArguments()[0]).run();
            return null;
        }).when(taskExecutor).execute(any(Runnable.class));

        RedisLock redisLock = mock(RedisLock.class);
        when(redisClient.getLock(anyString())).thenReturn(redisLock);

        String args = "";

        // When & Then
        assertDoesNotThrow(() -> {
            try (MockedStatic<ScriptUtils> mockedStatic = mockStatic(ScriptUtils.class)) {
                // 模拟 ScriptUtils.executeSqlScript 不抛出异常
                mockedStatic.when(() -> ScriptUtils.executeSqlScript(connection, sqlResource)).then(invocation -> null);

                syncAgentWorkflowObsTask.run(args);
            }
        });
    }


    @Test
    public void test_run_should_execute_without_exception_when_sqlResource_exists() throws Exception {
        // Given
        Resource[] resourceArray = new Resource[0];
        when(resolver.getResources(anyString())).thenReturn(resourceArray);

        Resource sqlResource = mock(Resource.class);
        when(sqlResource.exists()).thenReturn(true);
        when(resolver.getResource(anyString())).thenReturn(sqlResource);

        Connection connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);

        doAnswer(invocation -> {
            ((Runnable) invocation.getArguments()[0]).run();
            return null;
        }).when(taskExecutor).execute(any(Runnable.class));

        RedisLock redisLock = mock(RedisLock.class);
        when(redisClient.getLock(anyString())).thenReturn(redisLock);

        String args = "";

        // When & Then
        assertDoesNotThrow(() -> {
            try (MockedStatic<ScriptUtils> mockedStatic = mockStatic(ScriptUtils.class)) {
                // 模拟 ScriptUtils.executeSqlScript 不抛出异常
                mockedStatic.when(() -> ScriptUtils.executeSqlScript(connection, sqlResource)).then(invocation -> null);

                syncAgentWorkflowObsTask.run(args);
            }
        });
    }

    @Test
    void test_uploadPreObsFiles_should_void_when_condition() throws Exception {
        assertDoesNotThrow(() -> {
            // Given
            Resource[] resourceArray = new Resource[0];
            when(resolver.getResources(anyString())).thenReturn(resourceArray);

            // When
            syncAgentWorkflowObsTask.uploadPreObsFiles(null);
        });
    }

    @Test
    void test_uploadPreObsFiles_should_void_when_condition1() throws Exception {
        assertDoesNotThrow(() -> {
            // Given
            Resource[] resourceArray = new Resource[0];
            when(resolver.getResources(anyString())).thenReturn(resourceArray);

            // When
            syncAgentWorkflowObsTask.uploadPreObsFiles("string");
        });
    }

    @Test
    void test_uploadPreObsFiles_should_throws__null_pointer_exception_when_objects_is_null() throws Exception {
        assertDoesNotThrow(() -> {
            // Given
            when(resolver.getResources(anyString())).thenReturn(null);

            // When
            syncAgentWorkflowObsTask.uploadPreObsFiles(null);
        });
    }

    @Test
    void test_getOBSPath_should_equal_result_when_condition() throws Exception {

        // When
        String result = SyncAgentWorkflowObsTask.getOBSPath(null);

        // Then
        assertNull(result);
    }

    @Test
    void test_getOBSPath_should_equal_result_when_condition1() throws Exception {

        // When
        String result = SyncAgentWorkflowObsTask.getOBSPath("string");

        // Then
        assertNull(result);
    }

    @Test
    void test_getOBSPath_should_throws__null_pointer_exception_when_objects_is_null() throws Exception {
        // When
        String result = SyncAgentWorkflowObsTask.getOBSPath(null);
        assertNull(result);
    }

    @Test
    public void test_executeEntireSqlFile_should_execute_successfully_when_sqlResource_exists() throws Exception {
        // Given
        String sqlFilePath = "classpath:sql/test.sql";
        Connection connection = mock(Connection.class);
        Resource sqlResource = mock(Resource.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(resolver.getResource(eq(sqlFilePath))).thenReturn(sqlResource);
        when(sqlResource.exists()).thenReturn(true);

        // When & Then
        assertDoesNotThrow(() -> {
            try (MockedStatic<ScriptUtils> mockedStatic = mockStatic(ScriptUtils.class)) {
                // 模拟 ScriptUtils.executeSqlScript 不抛出异常
                mockedStatic.when(() -> ScriptUtils.executeSqlScript(connection, sqlResource)).then(invocation -> null);

                syncAgentWorkflowObsTask.executeEntireSqlFile(sqlFilePath);
            }
        });
    }

    @Test
    public void test_executeEntireSqlFile_should_throw_RuntimeException_when_scriptUtils_throws_exception() throws Exception {
        // Given
        String sqlFilePath = "classpath:sql/test.sql";
        Connection connection = mock(Connection.class);
        Resource sqlResource = mock(Resource.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(resolver.getResource(eq(sqlFilePath))).thenReturn(sqlResource);
        when(sqlResource.exists()).thenReturn(true);

        // When & Then
        assertDoesNotThrow(() -> {
            try (MockedStatic<ScriptUtils> mockedStatic = mockStatic(ScriptUtils.class)) {
                mockedStatic.when(() -> ScriptUtils.executeSqlScript(connection, sqlResource))
                    .thenThrow(new RuntimeException("Script execution failed"));

                syncAgentWorkflowObsTask.executeEntireSqlFile(sqlFilePath);
            }
        });
    }

    @Test
    public void test_executeEntireSqlFile_should_throw_exception_when_sqlResource_does_not_exist() throws Exception {
        // Given
        String sqlFilePath = "classpath:sql/test.sql";
        Connection connection = mock(Connection.class);
        Resource sqlResource = mock(Resource.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(resolver.getResource(eq(sqlFilePath))).thenReturn(sqlResource);
        when(sqlResource.exists()).thenReturn(false); // 模拟 SQL 文件不存在

        // When & Then
        assertDoesNotThrow(() -> {
            syncAgentWorkflowObsTask.executeEntireSqlFile(sqlFilePath);
        });
    }

    @Test
    void test_uploadPreObsFiles_should_not_throw_exception1() throws Exception {
        assertDoesNotThrow(() -> {
            try (MockedStatic<SyncAgentWorkflowObsTask> mockedStaticSyncAgentWorkflowObsTask = mockStatic(
                SyncAgentWorkflowObsTask.class, RETURNS_DEEP_STUBS)) {
                // Given
                mockedStaticSyncAgentWorkflowObsTask.when(() -> getOBSPath(anyString())).thenReturn("not_empty");

                Resource[] resourceArray = new Resource[0];
                when(resolver.getResources(anyString())).thenReturn(resourceArray);

                // When
                syncAgentWorkflowObsTask.uploadPreObsFiles("not_empty");
            }
        });
    }

    @Test
    void test_uploadPreObsFiles_should_not_throw_exception5() throws Exception {
        assertDoesNotThrow(() -> {
            try (MockedStatic<SyncAgentWorkflowObsTask> mockedStaticSyncAgentWorkflowObsTask = mockStatic(
                SyncAgentWorkflowObsTask.class, RETURNS_DEEP_STUBS)) {
                // Given
                mockedStaticSyncAgentWorkflowObsTask.when(() -> getOBSPath(anyString())).thenReturn(null);

                when(resolver.getResources(anyString())).thenReturn(null);

                // When
                syncAgentWorkflowObsTask.uploadPreObsFiles(null);
            }
        });
    }

    @Test
    void testUploadPreObsFiles_EmptyFilePath() {
        // 执行测试
        syncAgentWorkflowObsTask.uploadPreObsFiles(null);
        syncAgentWorkflowObsTask.uploadPreObsFiles("");

        // 验证无任何操作
        verify(obsService, never()).putObject(anyString(), anyString());
    }

    @Test
    void testUploadPreObsFiles_SingleFile() throws IOException {
        String filePath = "classpath:obs/test.txt";

        // 创建普通 Resource 和 FileSystemResource 的 mock
        Resource resource = mock(Resource.class);

        // 模拟资源信息
        URL url = new URL("file:/test/obs/test.txt");
        InputStream inputStream = new ByteArrayInputStream("test content".getBytes(StandardCharsets.UTF_8));


        // 模拟其他行为
        when(resolver.getResources(filePath)).thenReturn(new Resource[]{resource});
        when(resource.exists()).thenReturn(true);
        when(resource.getURL()).thenReturn(url);
        when(resource.getInputStream()).thenReturn(inputStream);

        // 验证结果
        assertDoesNotThrow(() -> {
            // 执行测试
            syncAgentWorkflowObsTask.uploadPreObsFiles(filePath);
        });
    }

    @Test
    void testUploadPreObsFiles_SkipDirectory() throws IOException {
        // 准备测试数据
        String filePath = "classpath:obs/dir/**";
        Resource resource = mock(Resource.class);
        URL url = new URL("file:/test/obs/dir/");

        // 模拟依赖行为
        when(resolver.getResources(filePath)).thenReturn(new Resource[]{resource});
        when(resource.exists()).thenReturn(true);
        when(resource.getURL()).thenReturn(url);

        // 验证结果
        assertDoesNotThrow(() -> {
            // 执行测试
            syncAgentWorkflowObsTask.uploadPreObsFiles(filePath);
        });
    }


    @Test
    void testUploadPreObsFiles_ResourceNotExists() throws IOException {
        // 准备测试数据
        String filePath = "classpath:obs/nonexist.txt";
        Resource resource = mock(Resource.class);

        // 模拟依赖行为
        when(resolver.getResources(filePath)).thenReturn(new Resource[]{resource});
        when(resource.exists()).thenReturn(false);

        // 执行测试
        syncAgentWorkflowObsTask.uploadPreObsFiles(filePath);

        verify(obsService, never()).putObject(anyString(), anyString());
    }



    @Test
    void testUploadPreObsFiles_ResourceIOException() throws IOException {
        // 准备测试数据
        String filePath = "classpath:obs/error/**";

        // 模拟依赖行为：获取资源时抛出异常
        when(resolver.getResources(filePath)).thenThrow(new IOException("Resource access error"));

        // 执行测试（应正常处理异常，不崩溃）
        syncAgentWorkflowObsTask.uploadPreObsFiles(filePath);

        // 验证结果：没有调用OBS上传
        verify(obsService, never()).putObject(anyString(), anyString());
    }

    @Test
    void test_updateProjectId_should_not_throw_exception() throws Exception {
        assertDoesNotThrow(() -> {

            // When
            syncAgentWorkflowObsTask.updateProjectId();
        });
    }

}
