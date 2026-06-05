/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service.clean.pipeline.subtask;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.openjiuwen.studio.agent.agentbase.mapper.CommonCleanMapper;
import com.openjiuwen.studio.agent.foundation.base.exception.AgentBaseException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * TenantCleanKnowledgeTableSubtask测试类
 *
 * @since 2025-12-22
 */
@ExtendWith(MockitoExtension.class)
public class TenantCleanKnowledgeTableSubtaskTest {

    @InjectMocks
    private com.openjiuwen.studio.agent.agentbase.service.clean.pipeline.subtask.TenantCleanKnowledgeTableSubtask
        subtask;

    @Mock
    private CommonCleanMapper commonCleanMapper;

    private static final String DOMAIN_ID_1 = "testDomain123";

    private static final String DOMAIN_ID_2 = "testDomain456";

    private static final String DOMAIN_ID_3 = "testDomain789";

    private static final String DOMAIN_ID_4 = "testDomain000";

    private static final String DOMAIN_ID_5 = "testDomain999";

    private static final String[] TABLES = new String[] {
        "t_knowledge_test_record", "t_knowledge_base_obs_execute_record", "t_knowledge_base_obs_config",
        "t_knowledge_segment_rule", "t_knowledge_repo", "t_knowledge_base"
    };

    /**
     * 用例描述：正常清理知识库表，所有表删除成功
     * 预置条件：commonCleanMapper.deleteDataByDomainId返回1
     * 输入参数：domainId = "testDomain123"
     * 预期结果：所有表删除成功，日志记录正确
     */
    @Test
    public void test_executeCleanup_when_allTablesDeleted_then_success() {
        for (String table : TABLES) {
            doReturn(1).when(commonCleanMapper).deleteDataByDomainId(table, DOMAIN_ID_1);
        }

        subtask.executeCleanup(DOMAIN_ID_1);

        for (String table : TABLES) {
            verify(commonCleanMapper, times(1)).deleteDataByDomainId(table, DOMAIN_ID_1);
        }
    }

    /**
     * 用例描述：部分表删除失败（模拟某张表删除返回0行）
     * 预置条件：t_knowledge_base表删除返回0，其他表返回1
     * 输入参数：domainId = "testDomain456"
     * 预期结果：部分表删除失败，日志记录正确
     */
    @Test
    public void test_executeCleanup_when_partialTableDeleteFailed_then_logError() {
        doReturn(1).when(commonCleanMapper).deleteDataByDomainId("t_knowledge_test_record", DOMAIN_ID_2);
        doReturn(1).when(commonCleanMapper).deleteDataByDomainId("t_knowledge_base_obs_execute_record", DOMAIN_ID_2);
        doReturn(1).when(commonCleanMapper).deleteDataByDomainId("t_knowledge_base_obs_config", DOMAIN_ID_2);
        doReturn(1).when(commonCleanMapper).deleteDataByDomainId("t_knowledge_segment_rule", DOMAIN_ID_2);
        doReturn(1).when(commonCleanMapper).deleteDataByDomainId("t_knowledge_repo", DOMAIN_ID_2);
        doReturn(0).when(commonCleanMapper).deleteDataByDomainId("t_knowledge_base", DOMAIN_ID_2);

        subtask.executeCleanup(DOMAIN_ID_2);

        for (String table : TABLES) {
            verify(commonCleanMapper, times(1)).deleteDataByDomainId(table, DOMAIN_ID_2);
        }
    }

    /**
     * 用例描述：清理过程中抛出运行时异常，触发异常处理逻辑并包装成AgentBaseException
     * 预置条件：commonCleanMapper.deleteDataByDomainId抛出RuntimeException
     * 输入参数：domainId = "testDomain789"
     * 预期结果：抛出AgentBaseException，异常信息包含原始错误内容
     */
    @Test
    public void test_executeCleanup_when_exceptionThrown_then_throwAgentBaseException() {
        // 使用 RuntimeException 来模拟运行时异常，因为 deleteDataByDomainId 方法通常不会声明抛出受检异常
        RuntimeException runtimeException = new RuntimeException("Mocked Exception");
        doThrow(runtimeException).when(commonCleanMapper).deleteDataByDomainId(anyString(), anyString());

        // 验证异常转换：底层 RuntimeException 会被包装成 AgentBaseException
        AgentBaseException exception = assertThrows(AgentBaseException.class, () -> {
            subtask.executeCleanup(DOMAIN_ID_3);
        });

        // 验证异常信息包含原始错误信息
        assertTrue(exception.getMessage().contains("Internal Server Error"));

        // 验证确实调用了删除方法
        verify(commonCleanMapper, times(1)).deleteDataByDomainId(anyString(), anyString());
    }

    /**
     * 用例描述：清理所有表，但每张表删除多行
     * 预置条件：commonCleanMapper.deleteDataByDomainId返回10
     * 输入参数：domainId = "testDomain999"
     * 预期结果：所有表删除成功，日志记录正确
     */
    @Test
    public void test_executeCleanup_when_allTablesDeletedMultipleRows_then_success() {
        for (String table : TABLES) {
            doReturn(10).when(commonCleanMapper).deleteDataByDomainId(table, DOMAIN_ID_5);
        }

        subtask.executeCleanup(DOMAIN_ID_5);

        for (String table : TABLES) {
            verify(commonCleanMapper, times(1)).deleteDataByDomainId(table, DOMAIN_ID_5);
        }
    }

    /**
     * 用例描述：验证parseKnowledgeTables方法返回的表名数组是否符合预期
     * 预置条件：无
     * 输入参数：无
     * 预期结果：返回的表名数组包含预期的表名
     */
    @Test
    public void test_parseKnowledgeTables_when_returnTablesAsExpected_then_success() {
        String[] result = subtask.parseKnowledgeTables();
        assertArrayEquals(TABLES, result);
    }

    /**
     * 用例描述：验证parseKnowledgeTables方法返回的表名顺序是否与代码中定义一致
     * 预置条件：无
     * 输入参数：无
     * 预期结果：返回的表名顺序与代码中定义一致
     */
    @Test
    public void test_parseKnowledgeTables_when_returnTablesInOrder_then_success() {
        String[] result = subtask.parseKnowledgeTables();
        assertArrayEquals(TABLES, result);
    }

    /**
     * 用例描述：验证parseKnowledgeTables方法返回的数组长度是否为6
     * 预置条件：无
     * 输入参数：无
     * 预期结果：返回的数组长度为6
     */
    @Test
    public void test_parseKnowledgeTables_when_returnArrayLengthIs6_then_success() {
        String[] result = subtask.parseKnowledgeTables();
        assertEquals(6, result.length);
    }

    /**
     * 用例描述：验证parseKnowledgeTables方法返回的表名是否不为空或null
     * 预置条件：无
     * 输入参数：无
     * 预期结果：返回的表名数组中所有元素不为空且不为null
     */
    @Test
    public void test_parseKnowledgeTables_when_returnTablesAreNotNullOrEmpty_then_success() {
        String[] result = subtask.parseKnowledgeTables();
        assertNotNull(result);
        for (String table : result) {
            assertNotNull(table);
            assertFalse(table.isEmpty());
        }
    }
}
