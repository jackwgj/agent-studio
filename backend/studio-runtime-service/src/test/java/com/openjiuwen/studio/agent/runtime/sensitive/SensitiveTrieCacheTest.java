/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.sensitive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.MockitoAnnotations.openMocks;

import com.openjiuwen.studio.agent.common.bo.AgentMetadata;
import com.openjiuwen.studio.agent.common.sensitive.SensitiveEntity;
import com.openjiuwen.studio.agent.runtime.constant.Constant;
import com.openjiuwen.studio.agent.runtime.model.WorkflowIrMetadata;
import com.openjiuwen.studio.agent.runtime.model.WorkflowIrWrapper;
import com.openjiuwen.studio.agent.runtime.sensitive.SensitiveTrieCache.SensitiveTrieWrapper;
import com.openjiuwen.studio.agent.runtime.service.AgentIrCacheService;
import com.openjiuwen.studio.agent.runtime.service.WorkflowIrCacheService;
import com.openjiuwen.studio.agent.runtime.utils.JsonUtils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;

/**
 * 功能描述
 *
 */
public class SensitiveTrieCacheTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private WorkflowIrCacheService workflowIrService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private AgentIrCacheService agentIrCacheService;

    private SensitiveTrieCache sensitiveTrieCache;

    private AutoCloseable mockitoCloseable;

    @BeforeEach
    public void setUp() throws Exception {
        mockitoCloseable = openMocks(this);
        sensitiveTrieCache = new SensitiveTrieCache(workflowIrService, agentIrCacheService);
    }

    @AfterEach
    public void tearDown() throws Exception {
        mockitoCloseable.close();
    }

    @Test
    public void test_get_sensitive_trie_wrapper_should_return_not_null_when_test_data_combination() throws Exception {
        // setup
        String contentReviewStr =
            "{\"enabled\":true,\"filter\":{\"keywords\":\"keyword1,keyword2\"},\"replace\":[{\"keywords\":\"keyword1,keyword2\",\"replace\":\"替换词\"}],\"reply\":[{\"keywords\":\"keyword1,keyword2\",\"input_enable\":true,\"input_text\":\"输入兜底回复\",\"output_enable\":true,\"output_text\":\"输出兜底回复\"}]}";
        final long updateAt = 10L;
        final long updateChanged = 15L;
        WorkflowIrMetadata irMetadata = new WorkflowIrMetadata();
        irMetadata.setUpdatedAt(updateAt);
        WorkflowIrWrapper irWrapper = new WorkflowIrWrapper();
        irWrapper.setMetadata(irMetadata);
        irWrapper.setSensitiveEntity(JsonUtils.json2ObjQuietly(contentReviewStr, SensitiveEntity.class));
        Mockito.when(workflowIrService.getWorkflowIr(anyString(), anyString())).thenReturn(irWrapper);

        // init cache
        sensitiveTrieCache.init();

        AgentMetadata metadata = AgentMetadata.builder().agentId("workflowId").versionId("1,0").build();
        metadata.setUpdatedAt(updateAt);
        // run the test
        SensitiveTrieWrapper result = sensitiveTrieCache.getSensitiveTrieWrapper(Constant.AppType.WORKFLOW, metadata);

        // verify the results
        assertTrue(result.isEnabled);
        assertEquals(updateAt, result.updateAt);

        metadata.setUpdatedAt(updateChanged);
        // need update cache
        irMetadata.setUpdatedAt(updateChanged);
        result = sensitiveTrieCache.getSensitiveTrieWrapper(Constant.AppType.WORKFLOW, metadata);
        assertTrue(result.isEnabled);
        assertEquals(updateChanged, result.updateAt);

        // read from cache
        irWrapper.setSensitiveEntity(JsonUtils.json2ObjQuietly("{\"enable\":false}", SensitiveEntity.class));
        result = sensitiveTrieCache.getSensitiveTrieWrapper(Constant.AppType.WORKFLOW, metadata);
        assertTrue(result.isEnabled);
    }
}
