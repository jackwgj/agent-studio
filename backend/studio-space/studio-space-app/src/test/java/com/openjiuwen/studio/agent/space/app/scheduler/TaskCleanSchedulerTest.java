package com.openjiuwen.studio.agent.space.app.scheduler;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.openjiuwen.studio.agent.space.app.util.AppCommonUtils;
import com.openjiuwen.studio.agent.space.common.utils.redis.RedisClient;
import com.openjiuwen.studio.agent.space.common.utils.redis.RedisLock;
import com.openjiuwen.studio.agent.space.dao.application.AgentBuilderActionMapper;
import com.openjiuwen.studio.agent.space.dao.application.AgentBuilderFileMapper;
import com.openjiuwen.studio.agent.space.dao.application.AgentBuilderMessageMapper;
import com.openjiuwen.studio.agent.space.dao.application.AgentBuilderStepMapper;
import com.openjiuwen.studio.agent.space.dao.application.AgentBuilderTaskMapper;
import com.openjiuwen.studio.agent.space.dao.entity.AgentBuilderMessageEntity;
import com.openjiuwen.studio.agent.space.dao.entity.AgentBuilderStepEntity;
import com.openjiuwen.studio.agent.space.dao.entity.AgentBuilderTaskEntity;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@MockitoSettings(strictness = Strictness.LENIENT)
class TaskCleanSchedulerTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private AgentBuilderTaskMapper AgentBuilderTaskMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private AgentBuilderMessageMapper AgentBuilderMessageMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private AgentBuilderStepMapper AgentBuilderStepMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private AgentBuilderActionMapper AgentBuilderActionMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private AgentBuilderFileMapper AgentBuilderFileMapper;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RedisClient redisClient;

    @InjectMocks
    private TaskCleanScheduler taskCleanScheduler;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(taskCleanScheduler, "duration", 0);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    void test_executeCleanTask_should_not_throw_exception() throws Exception {
        assertDoesNotThrow(() -> {
            try (MockedStatic<AppCommonUtils> mockedStaticAppCommonUtils = mockStatic(AppCommonUtils.class,
                RETURNS_DEEP_STUBS)) {
                // Given
                mockedStaticAppCommonUtils.when(() -> AppCommonUtils.getDayStartTime(any(LocalDate.class)))
                    .thenReturn(0L);
                mockedStaticAppCommonUtils.when(() -> AppCommonUtils.getStandardDateTime(anyLong()))
                    .thenReturn("not_empty");

                when(AgentBuilderFileMapper.delete(any(Wrapper.class))).thenReturn(0);

                when(AgentBuilderTaskMapper.deleteBatchIds(any(Collection.class))).thenReturn(0);
                Page<AgentBuilderTaskEntity> taskEntityPage = new Page();
                when(AgentBuilderTaskMapper.selectPage(any(IPage.class), any(Wrapper.class))).thenReturn(taskEntityPage);

                when(AgentBuilderMessageMapper.delete(any(Wrapper.class))).thenReturn(0);
                List<AgentBuilderMessageEntity> messages = new ArrayList<>();
                AgentBuilderMessageEntity AgentBuilderMessageEntity = new AgentBuilderMessageEntity();
                messages.add(AgentBuilderMessageEntity);
                when(AgentBuilderMessageMapper.selectList(any(Wrapper.class))).thenReturn(messages);

                when(AgentBuilderStepMapper.delete(any(Wrapper.class))).thenReturn(0);
                List<AgentBuilderStepEntity> steps = new ArrayList<>();
                AgentBuilderStepEntity AgentBuilderStepEntity = new AgentBuilderStepEntity();
                steps.add(AgentBuilderStepEntity);
                when(AgentBuilderStepMapper.selectList(any(Wrapper.class))).thenReturn(steps);

                when(AgentBuilderActionMapper.delete(any(Wrapper.class))).thenReturn(0);

                RedisLock lock = mock(RedisLock.class, Answers.RETURNS_DEEP_STUBS);
                when(lock.tryLock(any(Duration.class))).thenReturn(true);
                when(redisClient.getLock(anyString())).thenReturn(lock);

                // When
                taskCleanScheduler.executeCleanTask();
            }
        });
    }

    @Test
    void test_executeCleanTask_should_not_throw_exception2() throws Exception {
        assertDoesNotThrow(() -> {
            try (MockedStatic<AppCommonUtils> mockedStaticAppCommonUtils = mockStatic(AppCommonUtils.class,
                RETURNS_DEEP_STUBS)) {
                // Given
                mockedStaticAppCommonUtils.when(() -> AppCommonUtils.getDayStartTime(any(LocalDate.class)))
                    .thenReturn(0L);
                mockedStaticAppCommonUtils.when(() -> AppCommonUtils.getStandardDateTime(anyLong()))
                    .thenReturn("not_empty");

                when(AgentBuilderFileMapper.delete(any(Wrapper.class))).thenReturn(180);

                when(AgentBuilderTaskMapper.deleteBatchIds(any(Collection.class))).thenReturn(180);
                Page<AgentBuilderTaskEntity> taskEntityPage = new Page();
                when(AgentBuilderTaskMapper.selectPage(any(IPage.class), any(Wrapper.class))).thenReturn(taskEntityPage);

                when(AgentBuilderMessageMapper.delete(any(Wrapper.class))).thenReturn(180);
                List<AgentBuilderMessageEntity> messages = new ArrayList<>();
                AgentBuilderMessageEntity AgentBuilderMessageEntity = new AgentBuilderMessageEntity();
                messages.add(AgentBuilderMessageEntity);
                when(AgentBuilderMessageMapper.selectList(any(Wrapper.class))).thenReturn(messages);

                when(AgentBuilderStepMapper.delete(any(Wrapper.class))).thenReturn(180);
                List<AgentBuilderStepEntity> steps = new ArrayList<>();
                AgentBuilderStepEntity AgentBuilderStepEntity = new AgentBuilderStepEntity();
                steps.add(AgentBuilderStepEntity);
                when(AgentBuilderStepMapper.selectList(any(Wrapper.class))).thenReturn(steps);

                when(AgentBuilderActionMapper.delete(any(Wrapper.class))).thenReturn(180);

                RedisLock lock = mock(RedisLock.class, Answers.RETURNS_DEEP_STUBS);
                when(lock.tryLock(any(Duration.class))).thenReturn(true);
                when(redisClient.getLock(anyString())).thenReturn(lock);

                // When
                taskCleanScheduler.executeCleanTask();
            }
        });
    }

    @Test
    void test_executeCleanTask_should_not_throw_exception3() throws Exception {
        assertDoesNotThrow(() -> {
            try (MockedStatic<AppCommonUtils> mockedStaticAppCommonUtils = mockStatic(AppCommonUtils.class,
                RETURNS_DEEP_STUBS)) {
                // Given
                mockedStaticAppCommonUtils.when(() -> AppCommonUtils.getDayStartTime(any(LocalDate.class)))
                    .thenReturn(0L);
                mockedStaticAppCommonUtils.when(() -> AppCommonUtils.getStandardDateTime(anyLong()))
                    .thenReturn("not_empty");

                when(AgentBuilderFileMapper.delete(any(Wrapper.class))).thenReturn(60);

                when(AgentBuilderTaskMapper.deleteBatchIds(any(Collection.class))).thenReturn(60);
                Page<AgentBuilderTaskEntity> taskEntityPage = new Page();
                when(AgentBuilderTaskMapper.selectPage(any(IPage.class), any(Wrapper.class))).thenReturn(taskEntityPage);

                when(AgentBuilderMessageMapper.delete(any(Wrapper.class))).thenReturn(60);
                List<AgentBuilderMessageEntity> messages = new ArrayList<>();
                AgentBuilderMessageEntity AgentBuilderMessageEntity = new AgentBuilderMessageEntity();
                messages.add(AgentBuilderMessageEntity);
                when(AgentBuilderMessageMapper.selectList(any(Wrapper.class))).thenReturn(messages);

                when(AgentBuilderStepMapper.delete(any(Wrapper.class))).thenReturn(60);
                List<AgentBuilderStepEntity> steps = new ArrayList<>();
                AgentBuilderStepEntity AgentBuilderStepEntity = new AgentBuilderStepEntity();
                steps.add(AgentBuilderStepEntity);
                when(AgentBuilderStepMapper.selectList(any(Wrapper.class))).thenReturn(steps);

                when(AgentBuilderActionMapper.delete(any(Wrapper.class))).thenReturn(60);

                RedisLock lock = mock(RedisLock.class, Answers.RETURNS_DEEP_STUBS);
                when(lock.tryLock(any(Duration.class))).thenReturn(true);
                when(redisClient.getLock(anyString())).thenReturn(lock);

                // When
                taskCleanScheduler.executeCleanTask();
            }
        });
    }

    @Test
    void test_executeCleanTask_should_not_throw_exception5() throws Exception {
        assertDoesNotThrow(() -> {
            try (MockedStatic<AppCommonUtils> mockedStaticAppCommonUtils = mockStatic(AppCommonUtils.class,
                RETURNS_DEEP_STUBS)) {
                // Given
                mockedStaticAppCommonUtils.when(() -> AppCommonUtils.getDayStartTime(any(LocalDate.class)))
                    .thenReturn(0L);
                mockedStaticAppCommonUtils.when(() -> AppCommonUtils.getStandardDateTime(anyLong())).thenReturn(null);

                when(AgentBuilderFileMapper.delete(any(Wrapper.class))).thenReturn(0);

                when(AgentBuilderTaskMapper.deleteBatchIds(any(Collection.class))).thenReturn(0);
                when(AgentBuilderTaskMapper.selectPage(any(IPage.class), any(Wrapper.class))).thenReturn(null);

                when(AgentBuilderMessageMapper.delete(any(Wrapper.class))).thenReturn(0);
                when(AgentBuilderMessageMapper.selectList(any(Wrapper.class))).thenReturn(null);

                when(AgentBuilderStepMapper.delete(any(Wrapper.class))).thenReturn(0);
                when(AgentBuilderStepMapper.selectList(any(Wrapper.class))).thenReturn(null);

                when(AgentBuilderActionMapper.delete(any(Wrapper.class))).thenReturn(0);

                RedisLock lock = mock(RedisLock.class, Answers.RETURNS_DEEP_STUBS);
                when(lock.tryLock(any(Duration.class))).thenReturn(true);
                when(redisClient.getLock(anyString())).thenReturn(lock);

                // When
                taskCleanScheduler.executeCleanTask();
            }
        });
    }

    @Test
    void test_executeCleanTask_should_not_throw_exception7() throws Exception {
        assertDoesNotThrow(() -> {
            try (MockedStatic<AppCommonUtils> mockedStaticAppCommonUtils = mockStatic(AppCommonUtils.class,
                RETURNS_DEEP_STUBS)) {
                // Given
                mockedStaticAppCommonUtils.when(() -> AppCommonUtils.getDayStartTime(any(LocalDate.class)))
                    .thenReturn(0L);
                mockedStaticAppCommonUtils.when(() -> AppCommonUtils.getStandardDateTime(anyLong()))
                    .thenReturn("not_empty");

                when(AgentBuilderFileMapper.delete(any(Wrapper.class))).thenReturn(0);

                when(AgentBuilderTaskMapper.deleteBatchIds(any(Collection.class))).thenReturn(0);
                Page<AgentBuilderTaskEntity> taskEntityPage = new Page();
                when(AgentBuilderTaskMapper.selectPage(any(IPage.class), any(Wrapper.class))).thenReturn(taskEntityPage);

                when(AgentBuilderMessageMapper.delete(any(Wrapper.class))).thenReturn(0);
                List<AgentBuilderMessageEntity> messages = new ArrayList<>();
                AgentBuilderMessageEntity AgentBuilderMessageEntity = new AgentBuilderMessageEntity();
                messages.add(AgentBuilderMessageEntity);
                when(AgentBuilderMessageMapper.selectList(any(Wrapper.class))).thenReturn(messages);

                when(AgentBuilderStepMapper.delete(any(Wrapper.class))).thenReturn(0);
                List<AgentBuilderStepEntity> steps = new ArrayList<>();
                AgentBuilderStepEntity AgentBuilderStepEntity = new AgentBuilderStepEntity();
                steps.add(AgentBuilderStepEntity);
                when(AgentBuilderStepMapper.selectList(any(Wrapper.class))).thenReturn(steps);

                when(AgentBuilderActionMapper.delete(any(Wrapper.class))).thenReturn(0);

                RedisLock lock = mock(RedisLock.class, Answers.RETURNS_DEEP_STUBS);
                doThrow(NullPointerException.class).when(lock).tryLock(any(Duration.class));
                when(redisClient.getLock(anyString())).thenReturn(lock);

                // When
                taskCleanScheduler.executeCleanTask();
            }
        });
    }

    @Test
    void test_executeCleanTask_should_not_throw_exception8() throws Exception {
        assertDoesNotThrow(() -> {
            try (MockedStatic<AppCommonUtils> mockedStaticAppCommonUtils = mockStatic(AppCommonUtils.class,
                RETURNS_DEEP_STUBS)) {
                // Given
                mockedStaticAppCommonUtils.when(() -> AppCommonUtils.getDayStartTime(any(LocalDate.class)))
                    .thenReturn(0L);
                mockedStaticAppCommonUtils.when(() -> AppCommonUtils.getStandardDateTime(anyLong()))
                    .thenReturn("not_empty");

                when(AgentBuilderFileMapper.delete(any(Wrapper.class))).thenReturn(0);

                when(AgentBuilderTaskMapper.deleteBatchIds(any(Collection.class))).thenReturn(0);
                Page<AgentBuilderTaskEntity> taskEntityPage = new Page();
                when(AgentBuilderTaskMapper.selectPage(any(IPage.class), any(Wrapper.class))).thenReturn(taskEntityPage);

                when(AgentBuilderMessageMapper.delete(any(Wrapper.class))).thenReturn(0);
                List<AgentBuilderMessageEntity> messages = new ArrayList<>();
                AgentBuilderMessageEntity AgentBuilderMessageEntity = new AgentBuilderMessageEntity();
                messages.add(AgentBuilderMessageEntity);
                when(AgentBuilderMessageMapper.selectList(any(Wrapper.class))).thenReturn(messages);

                when(AgentBuilderStepMapper.delete(any(Wrapper.class))).thenReturn(0);
                List<AgentBuilderStepEntity> steps = new ArrayList<>();
                AgentBuilderStepEntity AgentBuilderStepEntity = new AgentBuilderStepEntity();
                steps.add(AgentBuilderStepEntity);
                when(AgentBuilderStepMapper.selectList(any(Wrapper.class))).thenReturn(steps);

                when(AgentBuilderActionMapper.delete(any(Wrapper.class))).thenReturn(0);

                RedisLock lock = mock(RedisLock.class, Answers.RETURNS_DEEP_STUBS);
                when(lock.tryLock(any(Duration.class))).thenReturn(true);
                doThrow(NullPointerException.class).when(lock).unlock();
                when(redisClient.getLock(anyString())).thenReturn(lock);

                // When
                taskCleanScheduler.executeCleanTask();
            }
        });
    }

    // ========== deleteTask方法测试开始 ==========

    @Test
    void test_deleteTask_withNoExpiredTasks() {
        // Given
        long expirationTime = System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 31; // 31天前

        // 创建第一页查询结果，但为空
        Page<AgentBuilderTaskEntity> emptyPage = new Page<>(1, 100);
        emptyPage.setRecords(new ArrayList<>());
        when(AgentBuilderTaskMapper.selectPage(any(), any())).thenReturn(emptyPage);

        // When
        taskCleanScheduler.deleteTask(expirationTime);

        // Then
        // 验证没有执行任何删除操作
        verify(AgentBuilderTaskMapper, never()).deleteBatchIds(any());
        verify(AgentBuilderMessageMapper, never()).delete(any());
        verify(AgentBuilderStepMapper, never()).delete(any());
        verify(AgentBuilderActionMapper, never()).delete(any());
        verify(AgentBuilderFileMapper, never()).delete(any());
    }

    @Test
    void test_deleteTask_withSingleBatch() {
        // Given
        long expirationTime = System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 31; // 31天前

        // 创建过期任务
        List<AgentBuilderTaskEntity> expiredTasks = createExpiredTasks(50); // 50个过期任务
        Page<AgentBuilderTaskEntity> page = new Page<>(1, 100);
        page.setRecords(expiredTasks);
        when(AgentBuilderTaskMapper.selectPage(any(), any())).thenReturn(page);

        // 设置关联数据
        List<AgentBuilderMessageEntity> messages = createMessages(50);
        List<AgentBuilderStepEntity> steps = createSteps(50);

        when(AgentBuilderMessageMapper.selectList(any())).thenReturn(messages);
        when(AgentBuilderStepMapper.selectList(any())).thenReturn(steps);

        // 各个删除操作的返回值
        when(AgentBuilderFileMapper.delete(any())).thenReturn(25); // 假设有25个文件
        when(AgentBuilderTaskMapper.deleteBatchIds(any())).thenReturn(50);
        when(AgentBuilderMessageMapper.delete(any())).thenReturn(50);
        when(AgentBuilderActionMapper.delete(any())).thenReturn(100); // 假设有100个action
        when(AgentBuilderStepMapper.delete(any())).thenReturn(50);

        // Mock AppCommonUtils
        try (MockedStatic<AppCommonUtils> mockedStaticAppCommonUtils = mockStatic(AppCommonUtils.class)) {
            mockedStaticAppCommonUtils.when(() -> AppCommonUtils.getStandardDateTime(anyLong()))
                .thenReturn("2025-01-01 00:00:00");

            // When
            taskCleanScheduler.deleteTask(expirationTime);

            // Then
            // 验证task查询被调用一次
            verify(AgentBuilderTaskMapper, times(1)).selectPage(any(), any());
            // 验证各个删除操作都被调用
            verify(AgentBuilderTaskMapper, times(1)).deleteBatchIds(any());
            verify(AgentBuilderMessageMapper, times(1)).delete(any());
            verify(AgentBuilderStepMapper, times(1)).delete(any());
            verify(AgentBuilderActionMapper, times(1)).delete(any());
            verify(AgentBuilderFileMapper, times(1)).delete(any());
        }
    }

    @Test
    void test_deleteTask_withQueryConditions() {
        // Given
        long expirationTime = System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 31; // 31天前
        Timestamp expirationTimestamp = new Timestamp(expirationTime);

        // 创建过期任务
        List<AgentBuilderTaskEntity> expiredTasks = createExpiredTasks(10);
        Page<AgentBuilderTaskEntity> page = new Page<>(1, 100);
        page.setRecords(expiredTasks);

        // 验证查询条件
        ArgumentCaptor<LambdaQueryWrapper<AgentBuilderTaskEntity>> queryCaptor = ArgumentCaptor.forClass(
            LambdaQueryWrapper.class);
        when(AgentBuilderTaskMapper.selectPage(any(), queryCaptor.capture())).thenReturn(page);

        when(AgentBuilderMessageMapper.selectList(any())).thenReturn(createMessages(10));
        when(AgentBuilderStepMapper.selectList(any())).thenReturn(createSteps(10));

        // 各个删除操作的返回值
        when(AgentBuilderFileMapper.delete(any())).thenReturn(5);
        when(AgentBuilderTaskMapper.deleteBatchIds(any())).thenReturn(10);
        when(AgentBuilderMessageMapper.delete(any())).thenReturn(10);
        when(AgentBuilderActionMapper.delete(any())).thenReturn(20);
        when(AgentBuilderStepMapper.delete(any())).thenReturn(10);

        // When
        taskCleanScheduler.deleteTask(expirationTime);

        // Then
        // 验证查询条件
        LambdaQueryWrapper<AgentBuilderTaskEntity> capturedQuery = queryCaptor.getValue();
        // 这里可以进一步验证查询条件的具体内容

        // 验证删除操作被调用
        verify(AgentBuilderTaskMapper, times(1)).deleteBatchIds(any());
    }

    // ========== 测试工具方法 ==========

    /**
     * 创建指定数量的过期任务
     */
    private List<AgentBuilderTaskEntity> createExpiredTasks(int count) {
        List<AgentBuilderTaskEntity> tasks = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            tasks.add(createExpiredTask("task_" + i));
        }
        return tasks;
    }

    /**
     * 创建单个过期任务
     */
    private AgentBuilderTaskEntity createExpiredTask(String id) {
        AgentBuilderTaskEntity task = new AgentBuilderTaskEntity();
        task.setId(id);
        task.setCreatedDate(new Timestamp(System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 31)); // 31天前
        task.setDisplayInfo("{\"is_pin\":false}"); // 默认未置顶
        return task;
    }

    /**
     * 创建指定数量的消息
     */
    private List<AgentBuilderMessageEntity> createMessages(int count) {
        List<AgentBuilderMessageEntity> messages = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            AgentBuilderMessageEntity message = new AgentBuilderMessageEntity();
            message.setId("message_" + i);
            message.setTaskId("task_" + i);
            messages.add(message);
        }
        return messages;
    }

    /**
     * 创建指定数量的步骤
     */
    private List<AgentBuilderStepEntity> createSteps(int count) {
        List<AgentBuilderStepEntity> steps = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            AgentBuilderStepEntity step = new AgentBuilderStepEntity();
            step.setId("step_" + i);
            step.setMessageId("message_" + i);
            steps.add(step);
        }
        return steps;
    }
}