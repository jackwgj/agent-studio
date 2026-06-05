/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service.mcp;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.manager.service.mcp.model.dao.McpServiceDao;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Transactional
@MockitoSettings(strictness = Strictness.LENIENT)
@Sql(scripts = {"classpath:sql/mcp_data_init_db.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
public class McpCleanTaskTest {

    @InjectMocks
    private McpCleanTask mcpCleanTask;

    @Mock
    private McpServiceDao mcpServiceDao;

    @BeforeEach
    void setup() throws Exception {
        MockitoAnnotations.openMocks(this);
        setPrivateField(mcpCleanTask, "lastSuccessfulMcpCleanTime", LocalDateTime.of(2024, 1, 1, 0, 0));
        setPrivateField(mcpCleanTask, "lastSuccessfulMcpCleanCount", 5);
    }

    /**
     * 通过反射设置私有字段的值
     */
    private void setPrivateField(Object object, String fieldName, Object value) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }

    @Test
    void testMcpCleanTaskNoMcpDeleted() {
        mcpCleanTask.hardDeleteExpiredMcpServices();
        List<String> mockIds = Arrays.asList("mcp_001", "mcp_002", "mcp_003");
        when(mcpServiceDao.listSoftDeletedMcpServiceBeforeTime(any(LocalDateTime.class)
        )).thenReturn(mockIds);
        mcpCleanTask.hardDeleteExpiredMcpServices();
    }

    @Test
    void testMcpCleanTaskMcpDeleted() {
        LocalDateTime mockNow = LocalDateTime.of(2027, 12, 11, 10, 0, 0);
        try (MockedStatic<LocalDateTime> mockedLocalDateTime = mockStatic(LocalDateTime.class)) {
            mockedLocalDateTime.when(LocalDateTime::now).thenReturn(mockNow);
            mcpCleanTask.hardDeleteExpiredMcpServices();
        }
    }

    @Test
    void testGetTaskStatus() {
        mcpCleanTask.getTaskStatus();
    }

}
