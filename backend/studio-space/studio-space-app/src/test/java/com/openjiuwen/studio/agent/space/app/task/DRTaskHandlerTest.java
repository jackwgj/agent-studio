package com.openjiuwen.studio.agent.space.app.task;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.space.app.util.AppCommonUtils;
import com.openjiuwen.studio.agent.space.common.utils.redis.RedisClient;
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

@MockitoSettings(strictness = Strictness.LENIENT)
class DRTaskHandlerTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private AgentBuilderTaskMapper AgentBuilderTaskMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RedisClient redisClient;

    @InjectMocks
    private DRTaskHandler dRTaskHandler;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_updateTaskStatus_should_return_true() throws Exception {
        try (MockedStatic<AppCommonUtils> mockedStaticAppCommonUtils = mockStatic(AppCommonUtils.class,
            RETURNS_DEEP_STUBS)) {
            // Given
            when(AgentBuilderTaskMapper.deletedById(any(AgentBuilderTaskEntity.class))).thenReturn(1);
            when(AgentBuilderTaskMapper.updateStatusById(any(AgentBuilderTaskEntity.class))).thenReturn(1);

            // When
            boolean result = dRTaskHandler.updateTaskStatus("not_empty", 0);

            // Then
            assertTrue(result);
        }
    }

    @Test
    void test_updateTaskStatusWithDelete_should_return_true() throws Exception {
        try (MockedStatic<AppCommonUtils> mockedStaticAppCommonUtils = mockStatic(AppCommonUtils.class,
            RETURNS_DEEP_STUBS)) {
            // Given
            when(AgentBuilderTaskMapper.deletedById(any(AgentBuilderTaskEntity.class))).thenReturn(1);
            when(AgentBuilderTaskMapper.updateStatusById(any(AgentBuilderTaskEntity.class))).thenReturn(1);

            // When
            boolean result = dRTaskHandler.updateTaskStatusWithDelete("not_empty", null);

            // Then
            assertTrue(result);
        }
    }
}