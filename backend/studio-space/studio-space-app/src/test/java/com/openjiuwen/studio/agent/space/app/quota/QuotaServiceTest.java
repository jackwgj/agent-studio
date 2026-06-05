package com.openjiuwen.studio.agent.space.app.quota;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.openjiuwen.studio.agent.space.app.annotation.quota.QuotaService;
import com.openjiuwen.studio.agent.space.app.util.AppCommonUtils;
import com.openjiuwen.studio.agent.space.common.context.AuthorizationContextHolder;
import com.openjiuwen.studio.agent.space.common.exception.AgentSpaceException;
import com.openjiuwen.studio.agent.space.common.utils.redis.RedisClient;
import com.openjiuwen.studio.agent.space.common.utils.redis.RedisLock;
import com.openjiuwen.studio.agent.space.dao.application.AgentBuilderQuotaMapper;
import com.openjiuwen.studio.agent.space.dao.entity.AgentBuilderQuotaEntity;

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
import java.util.List;

@MockitoSettings(strictness = Strictness.LENIENT)
class QuotaServiceTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private List<String> quotaWhiteList;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RedisClient redisClient;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private AgentBuilderQuotaMapper quotaMapper;

    @InjectMocks
    private QuotaService quotaService;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(quotaService, "taskRunCount", 0);
        ReflectionTestUtils.setField(quotaService, "quotaWhiteList", List.of("whist_list_01"));
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_verifyQuota_in_white_list() throws Exception {
        assertDoesNotThrow(() -> {
            try (MockedStatic<AuthorizationContextHolder> mockedStaticAuthorizationContextHolder = mockStatic(
                AuthorizationContextHolder.class, RETURNS_DEEP_STUBS);
                MockedStatic<AppCommonUtils> mockedStaticAppCommonUtils = mockStatic(AppCommonUtils.class,
                    RETURNS_DEEP_STUBS)) {
                // Given
                mockedStaticAuthorizationContextHolder.when(() -> AuthorizationContextHolder.userId())
                    .thenReturn("not_empty");
                mockedStaticAuthorizationContextHolder.when(() -> AuthorizationContextHolder.domainId())
                    .thenReturn("whist_list_01");
                mockedStaticAppCommonUtils.when(() -> AppCommonUtils.getUUID()).thenReturn("not_empty");
                quotaService.verifyQuota("not_empty", 0);
            }
        });
    }

    @Test
    void test_verifyQuota_should_throw_exception() throws Exception {
        try (MockedStatic<AuthorizationContextHolder> mockedStaticAuthorizationContextHolder = mockStatic(
            AuthorizationContextHolder.class, RETURNS_DEEP_STUBS);
            MockedStatic<AppCommonUtils> mockedStaticAppCommonUtils = mockStatic(AppCommonUtils.class,
                RETURNS_DEEP_STUBS)) {
            // Given
            mockedStaticAuthorizationContextHolder.when(() -> AuthorizationContextHolder.userId())
                .thenReturn("not_empty");
            mockedStaticAuthorizationContextHolder.when(() -> AuthorizationContextHolder.domainId())
                .thenReturn("not_empty");

            mockedStaticAppCommonUtils.when(() -> AppCommonUtils.getUUID()).thenReturn("not_empty");

            RedisLock lock = mock(RedisLock.class, Answers.RETURNS_DEEP_STUBS);
            when(lock.tryLock(any(Duration.class))).thenReturn(true);
            when(redisClient.getLock(anyString())).thenReturn(lock);

            when(quotaMapper.insert(any())).thenReturn(10);
            when(quotaMapper.selectOne(any(Wrapper.class))).thenReturn(null);

            // When
            assertThrows(AgentSpaceException.class, () -> quotaService.verifyQuota("task_run_count", 10));
        }
    }

    @Test
    void test_verifyQuota_should_throw_exception1() throws Exception {
        try (MockedStatic<AuthorizationContextHolder> mockedStaticAuthorizationContextHolder = mockStatic(
            AuthorizationContextHolder.class, RETURNS_DEEP_STUBS);
            MockedStatic<AppCommonUtils> mockedStaticAppCommonUtils = mockStatic(AppCommonUtils.class,
                RETURNS_DEEP_STUBS)) {
            // Given
            mockedStaticAuthorizationContextHolder.when(() -> AuthorizationContextHolder.userId())
                .thenReturn("not_empty");
            mockedStaticAuthorizationContextHolder.when(() -> AuthorizationContextHolder.domainId())
                .thenReturn("not_empty");

            mockedStaticAppCommonUtils.when(() -> AppCommonUtils.getUUID()).thenReturn("not_empty");

            RedisLock lock = mock(RedisLock.class, Answers.RETURNS_DEEP_STUBS);
            when(redisClient.getLock(anyString())).thenReturn(lock);
            when(lock.tryLock(any(Duration.class))).thenThrow(new AgentSpaceException("test_locked"));

            when(quotaMapper.insert(any())).thenReturn(0);
            when(quotaMapper.selectOne(any(Wrapper.class))).thenReturn(null);

            when(quotaWhiteList.contains(anyString())).thenReturn(true);

            // When
            assertThrows(AgentSpaceException.class, () -> quotaService.verifyQuota("task_run_count", 10));
        }
    }

    @Test
    void test_verifyQuota_should_not_throw_exception12() throws Exception {
        assertDoesNotThrow(() -> {
            try (MockedStatic<AuthorizationContextHolder> mockedStaticAuthorizationContextHolder = mockStatic(
                AuthorizationContextHolder.class, RETURNS_DEEP_STUBS);
                MockedStatic<AgentSpaceException> mockedStaticAgentSpaceException = mockStatic(
                    AgentSpaceException.class, RETURNS_DEEP_STUBS);
                MockedStatic<AppCommonUtils> mockedStaticAppCommonUtils = mockStatic(AppCommonUtils.class,
                    RETURNS_DEEP_STUBS)) {
                // Given
                mockedStaticAuthorizationContextHolder.when(() -> AuthorizationContextHolder.userId())
                    .thenReturn("not_empty");
                mockedStaticAuthorizationContextHolder.when(() -> AuthorizationContextHolder.domainId())
                    .thenReturn("not_empty");

                mockedStaticAppCommonUtils.when(() -> AppCommonUtils.getUUID()).thenReturn("not_empty");

                RedisLock lock = mock(RedisLock.class, Answers.RETURNS_DEEP_STUBS);
                doThrow(NullPointerException.class).when(lock).tryLock(any(Duration.class));
                when(redisClient.getLock(anyString())).thenReturn(lock);

                when(quotaMapper.insert(any())).thenReturn(0);
                AgentBuilderQuotaEntity quota = new AgentBuilderQuotaEntity();
                when(quotaMapper.selectOne(any(Wrapper.class))).thenReturn(quota);

                when(quotaWhiteList.contains(anyString())).thenReturn(true);

                // When
                quotaService.verifyQuota("not_empty", 0);
            }
        });
    }

    @Test
    void test_verifyQuota_should_not_throw_exception13() throws Exception {
        assertDoesNotThrow(() -> {
            try (MockedStatic<AuthorizationContextHolder> mockedStaticAuthorizationContextHolder = mockStatic(
                AuthorizationContextHolder.class, RETURNS_DEEP_STUBS);
                MockedStatic<AgentSpaceException> mockedStaticAgentSpaceException = mockStatic(
                    AgentSpaceException.class, RETURNS_DEEP_STUBS);
                MockedStatic<AppCommonUtils> mockedStaticAppCommonUtils = mockStatic(AppCommonUtils.class,
                    RETURNS_DEEP_STUBS)) {
                // Given
                mockedStaticAuthorizationContextHolder.when(() -> AuthorizationContextHolder.userId())
                    .thenReturn("not_empty");
                mockedStaticAuthorizationContextHolder.when(() -> AuthorizationContextHolder.domainId())
                    .thenReturn("not_empty");

                mockedStaticAppCommonUtils.when(() -> AppCommonUtils.getUUID()).thenReturn("not_empty");

                RedisLock lock = mock(RedisLock.class, Answers.RETURNS_DEEP_STUBS);
                when(lock.tryLock(any(Duration.class))).thenReturn(true);
                doThrow(NullPointerException.class).when(lock).unlock();
                when(redisClient.getLock(anyString())).thenReturn(lock);

                when(quotaMapper.insert(any())).thenReturn(0);
                AgentBuilderQuotaEntity quota = new AgentBuilderQuotaEntity();
                when(quotaMapper.selectOne(any(Wrapper.class))).thenReturn(quota);

                when(quotaWhiteList.contains(anyString())).thenReturn(true);

                // When
                quotaService.verifyQuota("not_empty", 0);
            }
        });
    }
}