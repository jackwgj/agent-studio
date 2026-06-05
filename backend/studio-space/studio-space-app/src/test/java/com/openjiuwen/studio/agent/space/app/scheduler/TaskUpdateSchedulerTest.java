package com.openjiuwen.studio.agent.space.app.scheduler;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.openjiuwen.studio.agent.space.app.task.DRTaskHandler;
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
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@MockitoSettings(strictness = Strictness.LENIENT)
class TaskUpdateSchedulerTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private AgentBuilderTaskMapper AgentBuilderTaskMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RedisClient redisClient;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DRTaskHandler drTaskHandler;

    @InjectMocks
    private TaskUpdateScheduler taskUpdateScheduler;

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
    void test_executeDRTaskUpdate_should_not_throw_exception() throws Exception {
        assertDoesNotThrow(() -> {
            // Given
            List<AgentBuilderTaskEntity> agentBuilderTaskEntities = new ArrayList<>();
            AgentBuilderTaskEntity AgentBuilderTaskEntity = new AgentBuilderTaskEntity();
            agentBuilderTaskEntities.add(AgentBuilderTaskEntity);
            when(AgentBuilderTaskMapper.selectList(any(IPage.class), any(Wrapper.class))).thenReturn(
                agentBuilderTaskEntities);

            when(drTaskHandler.updateTaskStatus(anyString(), anyInt())).thenReturn(true);

            RedisLock lock = mock(RedisLock.class, Answers.RETURNS_DEEP_STUBS);
            when(lock.tryLock(any(Duration.class))).thenReturn(true);
            when(redisClient.getLock(anyString())).thenReturn(lock);

            // When
            taskUpdateScheduler.executeDRTaskUpdate();
        });
    }

    @Test
    void test_executeDRTaskUpdate_should_not_throw_exception4() throws Exception {
        assertDoesNotThrow(() -> {
            // Given
            when(AgentBuilderTaskMapper.selectList(any(IPage.class), any(Wrapper.class))).thenReturn(null);

            when(drTaskHandler.updateTaskStatus(anyString(), anyInt())).thenReturn(true);

            RedisLock lock = mock(RedisLock.class, Answers.RETURNS_DEEP_STUBS);
            when(lock.tryLock(any(Duration.class))).thenReturn(true);
            when(redisClient.getLock(anyString())).thenReturn(lock);

            // When
            taskUpdateScheduler.executeDRTaskUpdate();
        });
    }

    @Test
    void test_executeDRTaskUpdate_should_not_throw_exception6() throws Exception {
        assertDoesNotThrow(() -> {
            // Given
            List<AgentBuilderTaskEntity> agentBuilderTaskEntities = new ArrayList<>();
            AgentBuilderTaskEntity AgentBuilderTaskEntity = new AgentBuilderTaskEntity();
            agentBuilderTaskEntities.add(AgentBuilderTaskEntity);
            when(AgentBuilderTaskMapper.selectList(any(IPage.class), any(Wrapper.class))).thenReturn(
                agentBuilderTaskEntities);

            when(drTaskHandler.updateTaskStatus(anyString(), anyInt())).thenReturn(true);

            RedisLock lock = mock(RedisLock.class, Answers.RETURNS_DEEP_STUBS);
            doThrow(NullPointerException.class).when(lock).tryLock(any(Duration.class));
            when(redisClient.getLock(anyString())).thenReturn(lock);

            // When
            taskUpdateScheduler.executeDRTaskUpdate();
        });
    }

    @Test
    void test_executeDRTaskUpdate_should_not_throw_exception7() throws Exception {
        assertDoesNotThrow(() -> {
            // Given
            List<AgentBuilderTaskEntity> agentBuilderTaskEntities = new ArrayList<>();
            AgentBuilderTaskEntity AgentBuilderTaskEntity = new AgentBuilderTaskEntity();
            agentBuilderTaskEntities.add(AgentBuilderTaskEntity);
            when(AgentBuilderTaskMapper.selectList(any(IPage.class), any(Wrapper.class))).thenReturn(
                agentBuilderTaskEntities);

            when(drTaskHandler.updateTaskStatus(anyString(), anyInt())).thenReturn(true);

            RedisLock lock = mock(RedisLock.class, Answers.RETURNS_DEEP_STUBS);
            doThrow(NullPointerException.class).when(lock).unlock();
            when(lock.tryLock(any(Duration.class))).thenReturn(true);
            when(redisClient.getLock(anyString())).thenReturn(lock);

            // When
            taskUpdateScheduler.executeDRTaskUpdate();
        });
    }
}