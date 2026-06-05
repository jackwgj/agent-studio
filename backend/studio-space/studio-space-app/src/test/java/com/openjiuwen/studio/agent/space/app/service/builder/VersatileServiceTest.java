package com.openjiuwen.studio.agent.space.app.service.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.space.api.vo.request.AgentBuilderCreateTaskReq;
import com.openjiuwen.studio.agent.space.app.util.AppCommonUtils;
import com.openjiuwen.studio.agent.space.app.util.AgentBuilderTransformUtil;
import com.openjiuwen.studio.agent.space.common.context.AuthorizationContextHolder;
import com.openjiuwen.studio.agent.space.common.exception.AgentSpaceException;
import com.openjiuwen.studio.agent.space.common.utils.redis.RedisClient;
import com.openjiuwen.studio.agent.space.common.utils.redis.RedisLock;
import com.openjiuwen.studio.agent.space.dao.application.AgentBuilderTaskMapper;
import com.openjiuwen.studio.agent.space.dao.entity.AgentBuilderTaskEntity;

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
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

@MockitoSettings(strictness = Strictness.LENIENT)
class AgentBuilderServiceTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private AgentBuilderTaskMapper taskMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RedisClient redisClient;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private AgentBuilderHelperService AgentBuilderHelperService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private AgentBuilderTransformUtil transformUtil;

    @InjectMocks
    private AgentBuilderService AgentBuilderService;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(AgentBuilderService, "normalAgentBucketName", "not_empty");
        ReflectionTestUtils.setField(AgentBuilderService, "memoriesMsgSize", 0);
        ReflectionTestUtils.setField(AgentBuilderService, "memoriesFileSize", 0);
        ReflectionTestUtils.setField(AgentBuilderService, "downloadUrlExpires", 0L);
        ReflectionTestUtils.setField(AgentBuilderService, "requestUrl", "not_empty");
        ReflectionTestUtils.setField(AgentBuilderService, "pinTaskLimit", 0);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_createTask_should_equal_result() throws Exception {
        try (MockedStatic<AuthorizationContextHolder> mockedStaticAuthorizationContextHolder = mockStatic(
            AuthorizationContextHolder.class, RETURNS_DEEP_STUBS);
            MockedStatic<AppCommonUtils> mockedStaticAppCommonUtils = mockStatic(AppCommonUtils.class,
                RETURNS_DEEP_STUBS);
            MockedStatic<AgentSpaceException> mockedStaticAgentSpaceException = mockStatic(AgentSpaceException.class,
                RETURNS_DEEP_STUBS)) {
            // Given
            mockedStaticAuthorizationContextHolder.when(() -> AuthorizationContextHolder.domainId())
                .thenReturn("not_empty");
            mockedStaticAuthorizationContextHolder.when(() -> AuthorizationContextHolder.userId())
                .thenReturn("not_empty");

            RedisLock lock = mock(RedisLock.class, Answers.RETURNS_DEEP_STUBS);
            when(lock.tryLock(any(Duration.class))).thenReturn(true);
            when(redisClient.getLock(anyString())).thenReturn(lock);

            when(taskMapper.insert(any())).thenReturn(0);

            AgentBuilderTaskEntity task = new AgentBuilderTaskEntity();
            task.setId("mock_id");
            when(transformUtil.createTask2TaskEntity(any(AgentBuilderCreateTaskReq.class))).thenReturn(task);

            AgentBuilderCreateTaskReq req = new AgentBuilderCreateTaskReq();

            // When
            String result = AgentBuilderService.createTask(req);

            // Then
            assertEquals("mock_id", result);
        }
    }

    @Test
    void test_createTask_should_throw_exception() throws Exception {
        try (MockedStatic<AuthorizationContextHolder> mockedStaticAuthorizationContextHolder = mockStatic(
            AuthorizationContextHolder.class, RETURNS_DEEP_STUBS)) {
            // Given
            mockedStaticAuthorizationContextHolder.when(() -> AuthorizationContextHolder.domainId())
                .thenReturn("not_empty");
            mockedStaticAuthorizationContextHolder.when(() -> AuthorizationContextHolder.userId())
                .thenReturn("not_empty");

            RedisLock lock = mock(RedisLock.class, Answers.RETURNS_DEEP_STUBS);
            when(redisClient.getLock(anyString())).thenReturn(lock);
            when(lock.tryLock(any(Duration.class))).thenThrow(new InterruptedException("mock_exception"));

            // when
            AgentBuilderCreateTaskReq req = new AgentBuilderCreateTaskReq();

            // Then
            assertThrows(AgentSpaceException.class, () -> AgentBuilderService.createTask(req));
        }
    }

    @Test
    void test_createTask_should_throw_exception1() throws Exception {
        try (MockedStatic<AuthorizationContextHolder> mockedStaticAuthorizationContextHolder = mockStatic(
            AuthorizationContextHolder.class, RETURNS_DEEP_STUBS)) {
            // Given
            mockedStaticAuthorizationContextHolder.when(() -> AuthorizationContextHolder.domainId())
                .thenReturn("not_empty");
            mockedStaticAuthorizationContextHolder.when(() -> AuthorizationContextHolder.userId())
                .thenReturn("not_empty");

            RedisLock lock = mock(RedisLock.class, Answers.RETURNS_DEEP_STUBS);
            when(lock.tryLock(any(Duration.class))).thenReturn(true);
            when(redisClient.getLock(anyString())).thenReturn(lock);

            when(taskMapper.insert(any())).thenThrow(new NullPointerException());

            when(transformUtil.createTask2TaskEntity(any(AgentBuilderCreateTaskReq.class))).thenReturn(null);

            // When
            assertThrows(AgentSpaceException.class, () -> AgentBuilderService.createTask(null));
        }
    }
}