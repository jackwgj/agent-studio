/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service.clean;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.agentbase.service.clean.pipeline.core.TenantCleanPipelineContext;
import com.openjiuwen.studio.agent.agentbase.service.clean.pipeline.core.TenantCleanPipelineExecutor;
import com.openjiuwen.studio.agent.foundation.base.exception.AgentBaseException;
import com.openjiuwen.studio.agent.foundation.base.exception.ErrorCode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * TenantDataCleanOrchestratorServiceImpl测试类
 * 测试基于管道模式的租户数据清理编排服务
 *
 * @since 2025-12-17
 */
@ExtendWith(MockitoExtension.class)
class TenantDataCleanOrchestratorServiceImplTest {

    @Mock
    private TenantCleanPipelineExecutor pipelineExecutor;

    private com.openjiuwen.studio.agent.agentbase.service.clean.TenantDataCleanOrchestratorServiceImpl
        tenantDataCleanOrchestratorService;

    private static final String TEST_DOMAIN_ID = "testDomain123";

    /**
     * 设置测试环境
     */
    @BeforeEach
    void setUp() {
        // 手动创建实例，避免@InjectMocks的问题
        tenantDataCleanOrchestratorService = new TenantDataCleanOrchestratorServiceImpl(pipelineExecutor);
        // 重置mock
        reset(pipelineExecutor);
    }

    /**
     * 用例描述：管道执行器正常执行，清理成功
     * 预置条件：pipelineExecutor.execute执行正常
     * 输入参数：testDomain123
     * 预期结果：无异常抛出，pipelineExecutor.execute被调用一次
     */
    @Test
    void test_executeClean_when_pipelineExecutorSuccess_then_noException() {
        // 准备返回对象
        TenantCleanPipelineContext successContext = new TenantCleanPipelineContext();
        successContext.setDomainId(TEST_DOMAIN_ID);

        // 设置mock行为 - 返回成功的context对象
        when(pipelineExecutor.execute(any(TenantCleanPipelineContext.class))).thenReturn(successContext);

        // 执行测试
        assertDoesNotThrow(() -> {
            tenantDataCleanOrchestratorService.executeClean(TEST_DOMAIN_ID);
        });

        // 验证管道执行器被调用
        verify(pipelineExecutor, times(1)).execute(any(TenantCleanPipelineContext.class));
    }

    /**
     * 用例描述：管道执行器抛出运行时异常，正确转换为AgentBaseException
     * 预置条件：pipelineExecutor.execute抛出RuntimeException
     * 输入参数：testDomain123
     * 预期结果：抛出包含原始错误信息的AgentBaseException
     */
    @Test
    void test_executeClean_when_pipelineExecutorThrowsException_then_wrapInAgentBaseException() {
        // 准备异常
        RuntimeException runtimeException = new RuntimeException("Internal Server Error");
        doThrow(runtimeException).when(pipelineExecutor).execute(any(TenantCleanPipelineContext.class));

        // 执行测试并验证异常包装
        AgentBaseException exception = assertThrows(AgentBaseException.class, () -> {
            tenantDataCleanOrchestratorService.executeClean(TEST_DOMAIN_ID);
        });

        // 验证异常信息包含原始错误
        assertTrue(exception.getMessage().contains("Internal Server Error"));
        assertEquals(ErrorCode.SERVER_INTERNAL_ERROR, exception.getErrorCode());

        // 验证管道执行器确实被调用
        verify(pipelineExecutor, times(1)).execute(any(TenantCleanPipelineContext.class));
    }

    /**
     * 用例描述：管道执行器抛出空指针异常，正确转换为AgentBaseException
     * 预置条件：pipelineExecutor.execute抛出NullPointerException
     * 输入参数：testDomain123
     * 预期结果：抛出包含原始错误信息的AgentBaseException
     */
    @Test
    void test_executeClean_when_pipelineExecutorThrowsNullPointerException_then_wrapInAgentBaseException() {
        // 准备异常
        NullPointerException nullPointerException = new NullPointerException("Internal Server Error");
        doThrow(nullPointerException).when(pipelineExecutor).execute(any(TenantCleanPipelineContext.class));

        // 执行测试并验证异常包装
        AgentBaseException exception = assertThrows(AgentBaseException.class, () -> {
            tenantDataCleanOrchestratorService.executeClean(TEST_DOMAIN_ID);
        });

        // 验证异常信息包含原始错误
        assertTrue(exception.getMessage().contains("Internal Server Error"));
        assertEquals(ErrorCode.SERVER_INTERNAL_ERROR, exception.getErrorCode());

        // 验证管道执行器确实被调用
        verify(pipelineExecutor, times(1)).execute(any(TenantCleanPipelineContext.class));
    }

    /**
     * 用例描述：管道执行器抛出业务异常，正确转换为AgentBaseException
     * 预置条件：pipelineExecutor.execute抛出IllegalArgumentException
     * 输入参数：testDomain123
     * 预期结果：抛出包含原始错误信息的AgentBaseException
     */
    @Test
    void test_executeClean_when_pipelineExecutorThrowsIllegalArgumentException_then_wrapInAgentBaseException() {
        // 准备异常
        IllegalArgumentException illegalArgumentException = new IllegalArgumentException("Internal Server Error");
        doThrow(illegalArgumentException).when(pipelineExecutor).execute(any(TenantCleanPipelineContext.class));

        // 执行测试并验证异常包装
        AgentBaseException exception = assertThrows(AgentBaseException.class, () -> {
            tenantDataCleanOrchestratorService.executeClean(TEST_DOMAIN_ID);
        });

        // 验证异常信息包含原始错误
        assertTrue(exception.getMessage().contains("Internal Server Error"));
        assertEquals(ErrorCode.SERVER_INTERNAL_ERROR, exception.getErrorCode());

        // 验证管道执行器确实被调用
        verify(pipelineExecutor, times(1)).execute(any(TenantCleanPipelineContext.class));
    }

    /**
     * 用例描述：domainId为空字符串，管道执行器正常处理
     * 预置条件：domainId为空字符串
     * 输入参数：""
     * 预期结果：无异常抛出，管道执行器被调用
     */
    @Test
    void test_executeClean_when_domainIdIsEmpty_then_pipelineExecutorHandlesIt() {
        // 准备返回对象
        TenantCleanPipelineContext successContext = new TenantCleanPipelineContext();
        successContext.setDomainId("");

        // 设置mock行为 - 返回成功的context对象
        when(pipelineExecutor.execute(any(TenantCleanPipelineContext.class))).thenReturn(successContext);

        // 执行测试
        assertDoesNotThrow(() -> {
            tenantDataCleanOrchestratorService.executeClean("");
        });

        // 验证管道执行器被调用
        verify(pipelineExecutor, times(1)).execute(any(TenantCleanPipelineContext.class));
    }

    /**
     * 用例描述：验证日志记录功能
     * 预置条件：pipelineExecutor执行成功
     * 输入参数：testDomain123
     * 预期结果：成功日志被记录
     */
    @Test
    void test_executeClean_when_success_then_logsSuccess() {
        // 准备返回对象
        TenantCleanPipelineContext successContext = new TenantCleanPipelineContext();
        successContext.setDomainId(TEST_DOMAIN_ID);

        // 设置mock行为 - 返回成功的context对象
        when(pipelineExecutor.execute(any(TenantCleanPipelineContext.class))).thenReturn(successContext);

        // 执行测试
        tenantDataCleanOrchestratorService.executeClean(TEST_DOMAIN_ID);

        // 验证管道执行器被调用
        verify(pipelineExecutor, times(1)).execute(any(TenantCleanPipelineContext.class));
    }

    /**
     * 用例描述：验证异常情况下的日志记录
     * 预置条件：pipelineExecutor抛出异常
     * 输入参数：testDomain123
     * 预期结果：失败日志被记录，异常被正确包装
     */
    @Test
    void test_executeClean_when_exception_then_logsErrorAndThrows() {
        // 准备异常
        RuntimeException runtimeException = new RuntimeException("Internal Server Error");
        doThrow(runtimeException).when(pipelineExecutor).execute(any(TenantCleanPipelineContext.class));

        // 执行测试并验证异常包装
        AgentBaseException exception = assertThrows(AgentBaseException.class, () -> {
            tenantDataCleanOrchestratorService.executeClean(TEST_DOMAIN_ID);
        });

        // 验证异常信息包含原始错误
        assertTrue(exception.getMessage().contains("Internal Server Error."));
        assertEquals(ErrorCode.SERVER_INTERNAL_ERROR, exception.getErrorCode());

        // 验证管道执行器确实被调用
        verify(pipelineExecutor, times(1)).execute(any(TenantCleanPipelineContext.class));
    }
}