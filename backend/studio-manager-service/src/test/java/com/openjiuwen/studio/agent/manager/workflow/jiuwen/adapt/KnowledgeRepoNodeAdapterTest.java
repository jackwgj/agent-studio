/* Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved. */
package com.openjiuwen.studio.agent.manager.workflow.jiuwen.adapt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.openjiuwen.studio.agent.common.enums.NodeType;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.common.utils.SpringBeanUtils;
import com.openjiuwen.studio.agent.manager.dto.WorkflowNodeVO;
import com.openjiuwen.studio.agent.manager.entity.KnowledgeRepoEntity;
import com.openjiuwen.studio.agent.manager.service.IrAdapterService;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 校验工作流知识库节点 IR 配置映射（jiuwen.knowledgeRetrieval）：
 * connectionId / knowledgeBaseIds / retrievalConfig(camelCase) 透传给 Python 消费端。
 */
class KnowledgeRepoNodeAdapterTest {

    private final KnowledgeRepoNodeAdapter adapter = new KnowledgeRepoNodeAdapter();

    private KnowledgeRepoEntity repo(String repoId, String connId, List<String> tags) {
        KnowledgeRepoEntity e = new KnowledgeRepoEntity();
        e.setKnowledgeRepoId(repoId);
        e.setConnectionId(connId);
        e.setTag(tags);
        return e;
    }

    @Test
    void getNodeType_isKnowledgeRepoIrType() {
        assertEquals(NodeType.KNOWLEDGE_REPO.getIrType(), adapter.getNodeType());
    }

    @Test
    void adaptConfig_buildsConnectionAndRetrievalConfig() {
        WorkflowNodeVO vo = new WorkflowNodeVO();
        Map<String, Object> configs = new HashMap<>();
        configs.put("top_k", "8");
        configs.put("recall_threshold", "0.7");
        configs.put("search_mode", "doc");
        configs.put("retrieve_image", true);
        configs.put("show_source", "true");
        vo.setConfigs(configs);

        IrAdapterService irAdapterService = Mockito.mock(IrAdapterService.class);
        List<KnowledgeRepoEntity> repos = List.of(
            repo("kb-1", "conn-1", List.of("t1")),
            repo("kb-2", "conn-1", List.of("t2"))
        );

        try (MockedStatic<SpringBeanUtils> beans = mockStatic(SpringBeanUtils.class);
             MockedStatic<RequestContextUtils> ctx = mockStatic(RequestContextUtils.class)) {
            beans.when(() -> SpringBeanUtils.getBean(IrAdapterService.class)).thenReturn(irAdapterService);
            ctx.when(RequestContextUtils::getRequestProjectId).thenReturn("proj-1");
            when(irAdapterService.getReposFromWorkflowNode(configs, "proj-1")).thenReturn(repos);

            Map<String, Object> result = adapter.adaptConfig(vo);

            assertEquals("conn-1", result.get("connectionId"));
            assertEquals(List.of("kb-1", "kb-2"), result.get("knowledgeBaseIds"));

            @SuppressWarnings("unchecked")
            Map<String, Object> rc = (Map<String, Object>) result.get("retrievalConfig");
            assertEquals(8, rc.get("topK"));
            // recallThreshold 与 scoreThreshold 均取召回阈值
            assertEquals(0.7f, rc.get("recallThreshold"));
            assertEquals(0.7f, rc.get("scoreThreshold"));
            assertEquals("doc", rc.get("searchMode"));
            assertEquals(true, rc.get("retrieveImage"));
            assertEquals(true, rc.get("showSource"));
            // tags 扁平合并
            assertEquals(List.of("t1", "t2"), rc.get("tags"));
        }
    }

    @Test
    void adaptConfig_usesDefaultsWhenConfigMissing() {
        WorkflowNodeVO vo = new WorkflowNodeVO();
        vo.setConfigs(new HashMap<>());  // 无任何检索配置 -> 走默认

        IrAdapterService irAdapterService = Mockito.mock(IrAdapterService.class);
        List<KnowledgeRepoEntity> repos = List.of(repo("kb-1", "conn-1", null));

        try (MockedStatic<SpringBeanUtils> beans = mockStatic(SpringBeanUtils.class);
             MockedStatic<RequestContextUtils> ctx = mockStatic(RequestContextUtils.class)) {
            beans.when(() -> SpringBeanUtils.getBean(IrAdapterService.class)).thenReturn(irAdapterService);
            ctx.when(RequestContextUtils::getRequestProjectId).thenReturn("proj-1");
            when(irAdapterService.getReposFromWorkflowNode(Mockito.any(), Mockito.any())).thenReturn(repos);

            Map<String, Object> result = adapter.adaptConfig(vo);
            @SuppressWarnings("unchecked")
            Map<String, Object> rc = (Map<String, Object>) result.get("retrievalConfig");

            assertEquals(5, rc.get("topK"));  // DEFAULT_TOP_K
            assertEquals("doc", rc.get("searchMode"));  // 默认 DOC
            assertEquals(false, rc.get("needExtrasFaqSearch"));
            // 无 tag 时不写 tags 键
            assertFalse(rc.containsKey("tags"));
        }
    }

    @Test
    void adaptConfig_emptyReposReturnsEmptyConfig() {
        WorkflowNodeVO vo = new WorkflowNodeVO();
        vo.setConfigs(new HashMap<>());
        IrAdapterService irAdapterService = Mockito.mock(IrAdapterService.class);

        try (MockedStatic<SpringBeanUtils> beans = mockStatic(SpringBeanUtils.class);
             MockedStatic<RequestContextUtils> ctx = mockStatic(RequestContextUtils.class)) {
            beans.when(() -> SpringBeanUtils.getBean(IrAdapterService.class)).thenReturn(irAdapterService);
            ctx.when(RequestContextUtils::getRequestProjectId).thenReturn("proj-1");
            when(irAdapterService.getReposFromWorkflowNode(Mockito.any(), Mockito.any())).thenReturn(List.of());

            Map<String, Object> result = adapter.adaptConfig(vo);
            assertTrue(result.isEmpty());
        }
    }
}
