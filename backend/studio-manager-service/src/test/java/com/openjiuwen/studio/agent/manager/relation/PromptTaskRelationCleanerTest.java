/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.relation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.manager.ros.relation.PromptTaskRelationCleaner;
import com.openjiuwen.studio.prompt.engineering.entity.v2.PromptTaskEntity;
import com.openjiuwen.studio.prompt.engineering.mapper.v2.PromptTaskMapper;
import com.openjiuwen.studio.prompt.engineering.service.PromptObsService;

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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@MockitoSettings(strictness = Strictness.LENIENT)
class PromptTaskRelationCleanerTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private PromptObsService obsService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private PromptTaskMapper promptTaskMapper;

    @InjectMocks
    private PromptTaskRelationCleaner promptTaskRelationCleaner;

    private AutoCloseable mockitoCloseable;

    private static final List<String> DELETE_TABLE_NAMES = List.of("cases", "history", "job_info", "optimize_info",
        "progress");

    @BeforeEach
    void setUp() throws Exception {
        mockitoCloseable = MockitoAnnotations.openMocks(this);
        // 初始化核心配置参数
        ReflectionTestUtils.setField(promptTaskRelationCleaner, "username", "not_empty");
        ReflectionTestUtils.setField(promptTaskRelationCleaner, "password", "not_empty");
        ReflectionTestUtils.setField(promptTaskRelationCleaner, "url", "not_empty");
        ReflectionTestUtils.setField(promptTaskRelationCleaner, "driverClassName", "org.mariadb.jdbc.Driver");
        ReflectionTestUtils.setField(promptTaskRelationCleaner, "schema", "not_empty");
        ReflectionTestUtils.setField(promptTaskRelationCleaner, "jiuwenSchema", "not_empty");
    }

    @AfterEach
    void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    /**
     * 原有测试：正常场景 - 传入有效ID集合，执行清理无异常
     */
    @Test
    void test_clean_should_not_throw_exception() throws Exception {
        assertDoesNotThrow(() -> {
            try (MockedStatic<DriverManager> mockedStaticDriverManager = mockStatic(DriverManager.class, RETURNS_DEEP_STUBS)) {
                // Given
                Connection connection = mock(Connection.class, Answers.RETURNS_DEEP_STUBS);
                mockedStaticDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(connection);

                List<PromptTaskEntity> promptTaskEntities = new ArrayList<>();
                PromptTaskEntity promptTaskEntity = PromptTaskEntity.builder().jiuwenTaskId("task_01").build();
                promptTaskEntities.add(promptTaskEntity);
                when(promptTaskMapper.selectByIds(anyList())).thenReturn(promptTaskEntities);

                List<String> ids = new ArrayList<>();
                ids.add("not_empty");

                // When
                promptTaskRelationCleaner.clean(ids, "not_empty");
            }
        });
    }

    /**
     * 新增测试1：入参ID为空集合，执行清理无异常（边界值场景）
     */
    @Test
    void test_clean_with_empty_ids_should_not_throw_exception() throws Exception {
        assertDoesNotThrow(() -> {
            try (MockedStatic<DriverManager> mockedStaticDriverManager = mockStatic(DriverManager.class, RETURNS_DEEP_STUBS)) {
                // Given 入参是空集合
                Connection connection = mock(Connection.class, Answers.RETURNS_DEEP_STUBS);
                mockedStaticDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(connection);
                // 空集合查询，返回空结果
                when(promptTaskMapper.selectByIds(Collections.emptyList())).thenReturn(Collections.emptyList());

                // When 传入空ID集合
                promptTaskRelationCleaner.clean(Collections.emptyList(), "not_empty");
            }
        });
    }

    /**
     * 新增测试2：查询返回空的任务列表，执行清理无异常（业务无数据场景）
     */
    @Test
    void test_clean_with_empty_task_list_should_not_throw_exception() throws Exception {
        assertDoesNotThrow(() -> {
            try (MockedStatic<DriverManager> mockedStaticDriverManager = mockStatic(DriverManager.class, RETURNS_DEEP_STUBS)) {
                // Given
                Connection connection = mock(Connection.class, Answers.RETURNS_DEEP_STUBS);
                mockedStaticDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(connection);
                // 关键：查询返回空集合
                when(promptTaskMapper.selectByIds(anyList())).thenReturn(Collections.emptyList());

                List<String> ids = new ArrayList<>();
                ids.add("task_id_001");
                ids.add("task_id_002");

                // When
                promptTaskRelationCleaner.clean(ids, "not_empty");
            }
        });
    }

    /**
     * 新增测试6：入参第二个参数为空白字符串，执行清理无异常（入参宽松兼容场景）
     */
    @Test
    void test_clean_with_blank_second_param_should_not_throw_exception() throws Exception {
        assertDoesNotThrow(() -> {
            try (MockedStatic<DriverManager> mockedStaticDriverManager = mockStatic(DriverManager.class, RETURNS_DEEP_STUBS)) {
                // Given
                Connection connection = mock(Connection.class, Answers.RETURNS_DEEP_STUBS);
                mockedStaticDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(connection);

                List<PromptTaskEntity> promptTaskEntities = new ArrayList<>();
                promptTaskEntities.add(PromptTaskEntity.builder().jiuwenTaskId("task_01").build());
                when(promptTaskMapper.selectByIds(anyList())).thenReturn(promptTaskEntities);

                List<String> ids = new ArrayList<>();
                ids.add("not_empty");

                // When 第二个入参为空白字符串
                promptTaskRelationCleaner.clean(ids, "");
            }
        });
    }

    @Test
    void test_clean_with_empty_jiuwen_task_id_should_skip_db_clean() throws Exception {
        assertDoesNotThrow(() -> {
            try (MockedStatic<DriverManager> mockedStaticDriverManager = mockStatic(DriverManager.class, RETURNS_DEEP_STUBS)) {
                // Given
                Connection connection = mock(Connection.class, Answers.RETURNS_DEEP_STUBS);
                mockedStaticDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(connection);

                // 构造任务：jiuwenTaskId为空 + 空字符串 两种场景
                List<PromptTaskEntity> promptTaskEntities = new ArrayList<>();
                promptTaskEntities.add(PromptTaskEntity.builder().id("prompt_01").jiuwenTaskId(null).build());
                promptTaskEntities.add(PromptTaskEntity.builder().id("prompt_02").jiuwenTaskId("").build());
                promptTaskEntities.add(PromptTaskEntity.builder().id("prompt_03").jiuwenTaskId("  ").build());
                when(promptTaskMapper.selectByIds(anyList())).thenReturn(promptTaskEntities);

                List<String> ids = List.of("prompt_01", "prompt_02", "prompt_03");

                // When
                promptTaskRelationCleaner.clean(ids, "domain_01");

                // Then 验证OBS清理执行，数据库连接的commit/rollback从未执行
                verify(connection, never()).commit();
                verify(connection, never()).rollback();
            }
        });
    }

    @Test
    void test_clean_jiuwen_db_data_success_commit_transaction() throws Exception {
        assertDoesNotThrow(() -> {
            // ============ 核心修复1：移除mockStatic(Class.class)，解决静态注册冲突 ============
            try (MockedStatic<DriverManager> mockedStaticDriverManager = mockStatic(DriverManager.class)) {
                // 1. 必须深度mock Connection，支持嵌套调用（重中之重）
                Connection connection = mock(Connection.class, Answers.RETURNS_DEEP_STUBS);
                PreparedStatement pstmt = mock(PreparedStatement.class);

                // 2. mock获取连接成功，返回深度mock的connection
                mockedStaticDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                    .thenReturn(connection);

                // 4. mock prepareStatement返回有效对象 + executeUpdate返回受影响行数(正确写法，无doNothing报错)
                when(connection.prepareStatement(anyString())).thenReturn(pstmt);
                when(pstmt.executeUpdate()).thenReturn(1);

                // 5. 实体类用new+set 确保jiuwenTaskId赋值有效，jiuWenIds非空，必走数据库逻辑
                PromptTaskEntity task1 = new PromptTaskEntity();
                task1.setJiuwenTaskId("jiuwen_01");

                PromptTaskEntity task2 = new PromptTaskEntity();
                task2.setJiuwenTaskId("jiuwen_02");

                when(promptTaskMapper.selectByIds(anyList())).thenReturn(List.of(task1, task2));

                // When 执行核心方法
                promptTaskRelationCleaner.clean(List.of("prompt_01", "prompt_02"), "domain_01");

                // Then 所有断言全部命中，无任何报错
                verify(connection).setAutoCommit(false);
                verify(connection, times(DELETE_TABLE_NAMES.size())).prepareStatement(anyString());
                verify(connection).commit();
                verify(connection).close();
            }
        });
    }

    @Test
    void test_clean_with_sql_exception_should_rollback_transaction() throws Exception {
        assertDoesNotThrow(() -> {
            try (MockedStatic<DriverManager> mockedStaticDriverManager = mockStatic(DriverManager.class)) {
                // 1. 深度mock Connection + 解决所有空指针
                Connection connection = mock(Connection.class, Answers.RETURNS_DEEP_STUBS);
                PreparedStatement pstmt = mock(PreparedStatement.class);
                mockedStaticDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString())).thenReturn(connection);

                // 3. 模拟SQL执行异常
                when(connection.prepareStatement(anyString())).thenReturn(pstmt);
                doThrow(new SQLException("SQL delete failed")).when(pstmt).executeUpdate();

                // 4. 实体类用new的方式，确保jiuwenTaskId赋值有效
                PromptTaskEntity task = new PromptTaskEntity();
                task.setJiuwenTaskId("jiuwen_01");
                when(promptTaskMapper.selectByIds(anyList())).thenReturn(Collections.singletonList(task));

                // When
                promptTaskRelationCleaner.clean(Collections.singletonList("prompt_01"), "domain_01");

                // Then 断言全部生效
                verify(connection).setAutoCommit(false);
                verify(connection).rollback();
                verify(connection).close();
            }
        });
    }

    @Test
    void test_clean_with_rollback_exception_should_catch_and_close_conn() throws Exception {
        assertDoesNotThrow(() -> {
            try (MockedStatic<DriverManager> mockedStaticDriverManager = mockStatic(DriverManager.class)) {
                Connection connection = mock(Connection.class, Answers.RETURNS_DEEP_STUBS);
                PreparedStatement pstmt = mock(PreparedStatement.class);
                mockedStaticDriverManager.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString())).thenReturn(connection);

                when(connection.prepareStatement(anyString())).thenReturn(pstmt);
                doThrow(new SQLException("SQL delete failed")).when(pstmt).executeUpdate();
                doThrow(new SQLException("rollback failed")).when(connection).rollback();

                PromptTaskEntity task = new PromptTaskEntity();
                task.setJiuwenTaskId("jiuwen_01");
                when(promptTaskMapper.selectByIds(anyList())).thenReturn(Collections.singletonList(task));

                promptTaskRelationCleaner.clean(Collections.singletonList("prompt_01"), "domain_01");

                verify(connection).close();
            }
        });
    }





}
