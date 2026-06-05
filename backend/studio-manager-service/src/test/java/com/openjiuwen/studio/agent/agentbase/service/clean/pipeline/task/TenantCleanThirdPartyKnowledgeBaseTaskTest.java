/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service.clean.pipeline.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
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

import java.lang.reflect.Field;

/**
 * TenantCleanThirdPartyKnowledgeBaseTask测试类
 *
 * @since 2025-12-22
 */
@ExtendWith(MockitoExtension.class)
public class TenantCleanThirdPartyKnowledgeBaseTaskTest {

    @InjectMocks
    private com.openjiuwen.studio.agent.agentbase.service.clean.pipeline.task.TenantCleanThirdPartyKnowledgeBaseTask
        task;

    @Mock
    private CommonCleanMapper commonCleanMapper;

    @BeforeEach
    public void setUp() {
        try {
            task = spy(new TenantCleanThirdPartyKnowledgeBaseTask(commonCleanMapper));
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject mock object", e);
        }
        try {
            Field field = TenantCleanThirdPartyKnowledgeBaseTask.class.getDeclaredField("commonCleanMapper");
            field.setAccessible(true);
            field.set(task, commonCleanMapper);
            field.set(task, commonCleanMapper);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject mock object", e);
        }
    }

    /**
     * 用例描述：正常执行清理第三方知识库连接表数据
     * 预置条件：commonCleanMapper.deleteDataByDomainId返回1
     * 输入参数：context.setDomainId("testDomainId")
     * 预期结果：日志输出成功信息，且commonCleanMapper.deleteDataByDomainId被调用一次
     */
    @Test
    public void test_execute_when_normal_then_success() {
        TenantCleanPipelineContext context = new TenantCleanPipelineContext();
        context.setDomainId("testDomainId");

        doReturn(1).when(commonCleanMapper).deleteDataByDomainId("t_knowledge_base_connection", "testDomainId");

        task.execute(context);

        verify(commonCleanMapper, times(1)).deleteDataByDomainId("t_knowledge_base_connection", "testDomainId");
    }

    /**
     * 用例描述：清理第三方知识库连接表数据时发生异常
     * 预置条件：commonCleanMapper.deleteDataByDomainId抛出异常
     * 输入参数：context.setDomainId("testDomainId")
     * 预期结果：日志输出失败信息，并抛出AgentBaseException
     */
    @Test
    public void test_execute_when_exception_then_throw() {
        TenantCleanPipelineContext context = new TenantCleanPipelineContext();
        context.setDomainId("testDomainId");

        doThrow(new RuntimeException("Mocked exception")).when(commonCleanMapper)
            .deleteDataByDomainId("t_knowledge_base_connection", "testDomainId");

        try {
            task.execute(context);
        } catch (AgentBaseException e) {
            // 预期抛出AgentBaseException
            verify(commonCleanMapper, times(1)).deleteDataByDomainId("t_knowledge_base_connection", "testDomainId");
            return;
        }

        throw new AssertionError("Expected AgentBaseException to be thrown");
    }

    /**
     * 用例描述：验证getOrder方法返回的顺序值是否正确
     * 预置条件：无
     * 输入参数：无
     * 预期结果：返回值为20
     */
    @Test
    public void test_getOrder_when_noInput_then_return20() {
        // 调用被测方法
        int result = task.getOrder();

        // 验证结果
        assertEquals(20, result);
    }
}
