/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service.clean.pipeline.task;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeBaseEntity;
import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeRepoEntity;
import com.openjiuwen.studio.agent.agentbase.mapper.KnowledgeBaseMapper;
import com.openjiuwen.studio.agent.agentbase.mapper.KnowledgeRepoMapper;
import com.openjiuwen.studio.agent.agentbase.service.clean.pipeline.core.TenantCleanPipelineContext;
import com.openjiuwen.studio.agent.agentbase.service.clean.pipeline.subtask.TenantCleanKnowledgeTableSubtask;
import com.openjiuwen.studio.agent.agentbase.service.knowledgerepo.KnowledgeRepoContext;
import com.openjiuwen.studio.agent.agentbase.service.knowledgerepo.KnowledgeRepoService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * TenantCleanKnowledgeBaseTask测试类
 *
 * @since 2025-12-20
 */
@ExtendWith(MockitoExtension.class)
public class TenantCleanKnowledgeBaseTaskTest {

    @Mock
    private KnowledgeBaseMapper knowledgeBaseMapper;

    @Mock
    private KnowledgeRepoMapper knowledgeRepoMapper;

    @Mock
    private TenantCleanKnowledgeTableSubtask cleanKnowledgeTableSubtask;

    @Mock
    private KnowledgeRepoContext knowledgeRepoContext;

    @Mock
    private KnowledgeRepoService knowledgeRepoService;

    private TenantCleanKnowledgeBaseTask
        tenantCleanKnowledgeBaseTask;

    @BeforeEach
    public void setUp() throws Exception {
        // 手动构造实例，使用构造器注入
        tenantCleanKnowledgeBaseTask = new TenantCleanKnowledgeBaseTask(knowledgeBaseMapper, knowledgeRepoMapper,
            knowledgeRepoContext);

        // 使用反射设置@Resource注解的字段
        Field field = TenantCleanKnowledgeBaseTask.class.getDeclaredField("cleanKnowledgeTableSubtask");
        field.setAccessible(true);
        field.set(tenantCleanKnowledgeBaseTask, cleanKnowledgeTableSubtask);

        // 设置knowledgeRepoContext.getService的返回值
        //when(knowledgeRepoContext.getService(anyString())).thenReturn(knowledgeRepoService);
    }

    /**
     * 用例描述：无知识库，直接跳过清理
     * 预置条件：knowledgeBaseMapper.selectByDomainId返回空列表
     * 输入参数：domainId为testDomain
     * 预期结果：日志记录"No knowledge bases found for domainId: testDomain, skipping cleanup"
     */
    @Test
    public void test_execute_when_noKnowledgeBases_then_skipCleanup() {
        TenantCleanPipelineContext context = new TenantCleanPipelineContext();
        context.setDomainId("testDomain");

        doReturn(List.of()).when(knowledgeBaseMapper).selectByDomainId("testDomain");

        tenantCleanKnowledgeBaseTask.execute(context);

        verify(knowledgeBaseMapper, times(1)).selectByDomainId("testDomain");
        verify(knowledgeRepoMapper, never()).selectByPrimaryKey(anyString(), anyString());
        verify(knowledgeRepoService, never()).deleteKnowledgeRepo(any());
        verify(cleanKnowledgeTableSubtask, never()).executeCleanup(anyString());
    }

    /**
     * 用例描述：存在知识库，但无内部知识库，跳过平台接口清理
     * 预置条件：knowledgeBaseMapper.selectByDomainId返回非空列表，但所有知识库类型为EXTERNAL
     * 输入参数：domainId为testDomain，知识库列表包含一个EXTERNAL类型知识库
     * 预期结果：日志记录"No internal knowledge bases found, skipping platform interface cleanup"
     */
    @Test
    public void test_execute_when_noInternalKnowledgeBases_then_skipPlatformCleanup() {
        TenantCleanPipelineContext context = new TenantCleanPipelineContext();
        context.setDomainId("testDomain");

        List<KnowledgeBaseEntity> knowledgeBases = new ArrayList<>();
        KnowledgeBaseEntity kb1 = new KnowledgeBaseEntity();
        kb1.setId("kb1");
        kb1.setType("external");
        knowledgeBases.add(kb1);

        doReturn(knowledgeBases).when(knowledgeBaseMapper).selectByDomainId("testDomain");

        tenantCleanKnowledgeBaseTask.execute(context);

        verify(knowledgeBaseMapper, times(1)).selectByDomainId("testDomain");
        verify(knowledgeRepoMapper, never()).selectByPrimaryKey(anyString(), anyString());
        verify(knowledgeRepoService, never()).deleteKnowledgeRepo(any());
        verify(cleanKnowledgeTableSubtask, times(1)).executeCleanup("testDomain");
    }

    /**
     * 用例描述：存在内部知识库，且externalId为空，跳过平台接口清理
     * 预置条件：knowledgeBaseMapper.selectByDomainId返回非空列表，知识库类型为INTERNAL，但externalId为空
     * 输入参数：domainId为testDomain，知识库列表包含一个INTERNAL类型知识库，externalId为空
     * 预期结果：日志记录"KnowledgeBase externalId is null for knowledgeBaseId: kb1, skipping platform delete"
     */
    @Test
    public void test_execute_when_externalIdIsNull_then_skipPlatformDelete() {
        TenantCleanPipelineContext context = new TenantCleanPipelineContext();
        context.setDomainId("testDomain");

        List<KnowledgeBaseEntity> knowledgeBases = new ArrayList<>();
        KnowledgeBaseEntity kb1 = new KnowledgeBaseEntity();
        kb1.setId("kb1");
        kb1.setType("internal");
        kb1.setExternalId(null);
        knowledgeBases.add(kb1);

        doReturn(knowledgeBases).when(knowledgeBaseMapper).selectByDomainId("testDomain");

        tenantCleanKnowledgeBaseTask.execute(context);

        verify(knowledgeBaseMapper, times(1)).selectByDomainId("testDomain");
        verify(knowledgeRepoMapper, never()).selectByPrimaryKey(anyString(), anyString());
        verify(knowledgeRepoService, never()).deleteKnowledgeRepo(any());
        verify(cleanKnowledgeTableSubtask, times(1)).executeCleanup("testDomain");
    }

    /**
     * 用例描述：存在内部知识库，且knowledgeRepoEntity为空，跳过平台接口清理
     * 预置条件：knowledgeBaseMapper.selectByDomainId返回非空列表，知识库类型为INTERNAL，externalId不为空，但knowledgeRepoMapper.selectByPrimaryKey返回null
     * 输入参数：domainId为testDomain，知识库列表包含一个INTERNAL类型知识库，externalId为externalId1，projectId为null
     * 预期结果：日志记录"KnowledgeRepoEntity not found for knowledgeBaseId: kb1, treating as success"
     */
    @Test
    public void test_execute_when_knowledgeRepoEntityIsNull_then_skipPlatformDelete() {
        TenantCleanPipelineContext context = new TenantCleanPipelineContext();
        context.setDomainId("testDomain");

        List<KnowledgeBaseEntity> knowledgeBases = new ArrayList<>();
        KnowledgeBaseEntity kb1 = new KnowledgeBaseEntity();
        kb1.setId("kb1");
        kb1.setType("internal");
        kb1.setExternalId("externalId1");
        kb1.setProjectId(null);
        knowledgeBases.add(kb1);

        doReturn(knowledgeBases).when(knowledgeBaseMapper).selectByDomainId("testDomain");
        doReturn(null).when(knowledgeRepoMapper).selectByPrimaryKey("kb1", null);

        tenantCleanKnowledgeBaseTask.execute(context);

        verify(knowledgeBaseMapper, times(1)).selectByDomainId("testDomain");
        verify(knowledgeRepoMapper, times(1)).selectByPrimaryKey("kb1", null);
        verify(knowledgeRepoService, never()).deleteKnowledgeRepo(any());
        verify(cleanKnowledgeTableSubtask, times(1)).executeCleanup("testDomain");
    }

    /**
     * 用例描述：存在内部知识库，且成功调用平台接口删除
     * 预置条件：knowledgeBaseMapper.selectByDomainId返回非空列表，知识库类型为INTERNAL，externalId不为空，knowledgeRepoMapper.selectByPrimaryKey返回非空KnowledgeRepoEntity
     * 输入参数：domainId为testDomain，知识库列表包含一个INTERNAL类型知识库，externalId为externalId1，projectId为projectId1
     * 预期结果：调用knowledgeRepoService.deleteKnowledgeRepo方法，日志记录"Successfully cleaned internal knowledgeBase: kb1 for domainId: testDomain"
     */
    @Test
    public void test_execute_when_internalKnowledgeBaseExistsAndDeletedSuccessfully() {
        TenantCleanPipelineContext context = new TenantCleanPipelineContext();
        context.setDomainId("testDomain");

        List<KnowledgeBaseEntity> knowledgeBases = new ArrayList<>();
        KnowledgeBaseEntity kb1 = new KnowledgeBaseEntity();
        kb1.setId("kb1");
        kb1.setType("internal");
        kb1.setExternalId("externalId1");
        kb1.setProjectId("projectId1");
        kb1.setDomainId("testDomain");
        knowledgeBases.add(kb1);

        KnowledgeRepoEntity knowledgeRepoEntity = new KnowledgeRepoEntity();
        knowledgeRepoEntity.setMetadata("metadata1");
        knowledgeRepoEntity.setType("type1");
        knowledgeRepoEntity.setSource("source1");

        doReturn(knowledgeBases).when(knowledgeBaseMapper).selectByDomainId("testDomain");
        doReturn(knowledgeRepoEntity).when(knowledgeRepoMapper).selectByPrimaryKey("kb1", "projectId1");

        when(knowledgeRepoContext.getService(anyString())).thenReturn(knowledgeRepoService);
        tenantCleanKnowledgeBaseTask.execute(context);

        verify(knowledgeBaseMapper, times(1)).selectByDomainId("testDomain");
        verify(knowledgeRepoMapper, times(1)).selectByPrimaryKey("kb1", "projectId1");
        verify(knowledgeRepoService, times(1)).deleteKnowledgeRepo(any());
        verify(cleanKnowledgeTableSubtask, times(1)).executeCleanup("testDomain");
    }
}
