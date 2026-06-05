/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.relation;

import com.openjiuwen.studio.agent.common.redis.RedisClient;
import com.openjiuwen.studio.agent.manager.entity.md.ProviderAuthData;
import com.openjiuwen.studio.agent.manager.mapper.md.ProviderAuthDataMapper;
import com.openjiuwen.studio.agent.manager.obs.MgObsService;
import com.openjiuwen.studio.agent.manager.ros.relation.ProviderRelationCleaner;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

public class ProviderRelationCleanerTest {

    @Test
    public void cleanTest() {
        ProviderRelationCleaner providerRelationCleaner = new ProviderRelationCleaner();
        RedisClient redisClient = Mockito.mock(RedisClient.class);
        MgObsService obsService = Mockito.mock(MgObsService.class);
        ProviderAuthDataMapper providerAuthDataMapper = Mockito.mock(ProviderAuthDataMapper.class);
        ProviderAuthData providerAuthData = new ProviderAuthData();
        providerAuthData.setProviderId("id");
        providerAuthData.setProjectId("id");
        providerAuthData.setId("id");
        List<ProviderAuthData> list = List.of(providerAuthData);
        Mockito.when(providerAuthDataMapper.selectByIds(Mockito.anyList())).thenReturn(list);
        ReflectionTestUtils.setField(providerRelationCleaner, "redisClient", redisClient);
        ReflectionTestUtils.setField(providerRelationCleaner, "obsService", obsService);
        ReflectionTestUtils.setField(providerRelationCleaner, "authMapper", providerAuthDataMapper);
        List<String> ids = List.of("id");
        providerRelationCleaner.clean(ids, "test_domain_id");
    }
}
