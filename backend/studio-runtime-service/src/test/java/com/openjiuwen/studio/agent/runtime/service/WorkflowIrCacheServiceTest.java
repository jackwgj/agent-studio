/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.github.benmanes.caffeine.cache.LoadingCache;
import com.openjiuwen.studio.agent.runtime.model.WorkflowIrMetadata;
import com.openjiuwen.studio.agent.runtime.model.WorkflowIrWrapper;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashSet;
import java.util.concurrent.ConcurrentMap;

class WorkflowIrCacheServiceTest {
    @SuppressWarnings("unchecked")
    @Test
    public void getIrWorkspaceFromCacheTest() {
        WorkflowIrCacheService service = new WorkflowIrCacheService();
        LoadingCache<?, Object> workflowIrCache = Mockito.mock(LoadingCache.class);
        ReflectionTestUtils.setField(service, "workflowIrCache", workflowIrCache);

        Mockito.when(workflowIrCache.getIfPresent(Mockito.any())).thenReturn(null);
        String id = service.getIrWorkspaceFromCache("1", "1");
        assertNull(id);

        WorkflowIrMetadata metadata = new WorkflowIrMetadata();
        metadata.setWorkspaceId("1");
        WorkflowIrWrapper wrapper = new WorkflowIrWrapper();
        wrapper.setMetadata(metadata);
        Mockito.when(workflowIrCache.getIfPresent(Mockito.any())).thenReturn(wrapper);
        id = service.getIrWorkspaceFromCache("1", "1");
        assertEquals("1", id);

        // 创建一个mock的Map对象
        ConcurrentMap<?, Object> mockMap = Mockito.mock(ConcurrentMap.class);
        Mockito.doReturn(mockMap).when(workflowIrCache).asMap();
        Mockito.when(mockMap.keySet()).thenReturn(new HashSet<>());
        service.clearWorkflowCache("id");
    }
}
