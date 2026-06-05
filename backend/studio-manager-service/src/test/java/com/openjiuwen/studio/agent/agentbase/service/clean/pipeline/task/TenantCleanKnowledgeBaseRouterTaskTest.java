/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service.clean.pipeline.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.openjiuwen.studio.agent.agentbase.mapper.CommonCleanMapper;
import com.openjiuwen.studio.agent.agentbase.service.clean.pipeline.core.TenantCleanPipelineContext;
import com.openjiuwen.studio.agent.foundation.base.exception.AgentBaseException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * TenantCleanKnowledgeBaseRouterTask测试类
 *
 * @since 2025-12-22
 */
@ExtendWith(MockitoExtension.class)
class TenantCleanKnowledgeBaseRouterTaskTest {

    @InjectMocks
    private com.openjiuwen.studio.agent.agentbase.service.clean.pipeline.task.TenantCleanKnowledgeBaseRouterTask task;

    @Mock
    private CommonCleanMapper commonCleanMapper;

    @BeforeEach
    void setUp() {
        // 初始化被测对象
    }

    /**
     * 用例描述：正常执行清理知识库路由表数据
     * 预置条件：commonCleanMapper.deleteDataByDomainId返回正常值
     * 输入参数：context.setDomainId("testDomainId")
     * 预期结果：无返回值，但预期日志记录成功，且deleteDataByDomainId被调用一次
     */
    @Test
    void test_execute_when_normal_then_success() {
        // Arrange
        TenantCleanPipelineContext context = new TenantCleanPipelineContext();
        context.setDomainId("testDomainId");

        doReturn(5).when(commonCleanMapper).deleteDataByDomainId("t_kb_connection_router", "testDomainId");

        // Act
        task.execute(context);

        // Assert
        verify(commonCleanMapper, times(1)).deleteDataByDomainId("t_kb_connection_router", "testDomainId");
    }

    /**
     * 用例描述：清理知识库路由表数据时发生异常
     * 预置条件：commonCleanMapper.deleteDataByDomainId抛出异常
     * 输入参数：context.setDomainId("testDomainId")
     * 预期结果：预期抛出AgentBaseException，且日志记录失败信息
     */
    @Test
    void test_execute_when_exception_then_throw() {
        // Arrange
        TenantCleanPipelineContext context = new TenantCleanPipelineContext();
        context.setDomainId("testDomainId");

        doThrow(new RuntimeException("Mocked Exception")).when(commonCleanMapper)
            .deleteDataByDomainId("t_kb_connection_router", "testDomainId");

        // Act & Assert
        try {
            task.execute(context);
        } catch (AgentBaseException e) {
            // 预期抛出AgentBaseException
            assert e.getMessage().contains("Internal Server Error");
        }

        verify(commonCleanMapper, times(1)).deleteDataByDomainId("t_kb_connection_router", "testDomainId");
    }

    /**
     * 验证getOrder方法返回的顺序值是否为30
     *
     * @since 2025-12-22
     */
    @Test
    void test_getOrder_when_noParam_then_return30() {
        // 调用被测方法
        int result = task.getOrder();

        // 验证结果
        assertEquals(30, result);
    }

    /**
     * 验证getName方法返回的字符串是否符合预期
     * <p>
     * 预置条件：无
     * 输入参数：无
     * 预期结果：返回"KnowledgeBaseRouterCleanup"
     */
    @Test
    void test_getName_when_noInput_then_returnExpectedString() {
        // 调用被测方法
        String result = task.getName();

        // 验证结果
        assertEquals("KnowledgeBaseRouterCleanup", result);
    }
}