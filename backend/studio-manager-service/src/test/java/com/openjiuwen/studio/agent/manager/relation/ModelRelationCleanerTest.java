/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.relation;

import com.openjiuwen.studio.agent.common.redis.RedisClient;
import com.openjiuwen.studio.agent.manager.obs.MgObsService;
import com.openjiuwen.studio.agent.manager.ros.relation.ModelRelationCleaner;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

public class ModelRelationCleanerTest {

    @Test
    public void cleanTest() {
        ModelRelationCleaner modelServiceMapper = new ModelRelationCleaner();
        RedisClient redisClient = Mockito.mock(RedisClient.class);
        MgObsService obsService = Mockito.mock(MgObsService.class);
        ReflectionTestUtils.setField(modelServiceMapper, "redisClient", redisClient);
        ReflectionTestUtils.setField(modelServiceMapper, "obsService", obsService);
        List<String> ids = List.of("id");
        modelServiceMapper.clean(ids, "test_domain_id");
    }
}
