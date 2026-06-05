/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service.md;

import com.openjiuwen.studio.agent.common.redis.RedisClient;
import com.openjiuwen.studio.agent.common.redis.RedisLock;
import com.openjiuwen.studio.agent.manager.entity.md.ModelServiceBase;
import com.openjiuwen.studio.agent.manager.mapper.md.ModelServiceMapper;
import com.openjiuwen.studio.agent.manager.obs.MgObsService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.Collections;

public class ModelServiceManagerTest {
    @Test
    public void syncUpgradeModelsTest() {
        com.openjiuwen.studio.agent.manager.service.md.ModelServiceManager manager = new ModelServiceManager();

        RedisClient redisClient = Mockito.mock(RedisClient.class);
        Mockito.doNothing().when(redisClient).set(Mockito.anyString(), Mockito.anyString());

        Mockito.when(redisClient.getLock(Mockito.anyString())).thenReturn(new RedisLock() {
            @Override
            public boolean tryLock(Duration maxWait) throws Exception {
                return true;
            }

            @Override
            public void unlock() {

            }
        });

        ModelServiceMapper modelServiceMapper = Mockito.mock(ModelServiceMapper.class);
        Mockito.when(modelServiceMapper.queryNotSyncModels())
            .thenReturn(Collections.singletonList(new ModelServiceBase().setId("id").setModelName("name")));
        Mockito.doNothing().when(modelServiceMapper).updateSyncStatus(Mockito.anyString());

        MgObsService obsService = Mockito.mock(MgObsService.class);
        Mockito.doNothing().when(obsService).putObject(Mockito.anyString(), Mockito.anyString());

        ReflectionTestUtils.setField(manager, "redisClient", redisClient);
        ReflectionTestUtils.setField(manager, "modelServiceMapper", modelServiceMapper);
        ReflectionTestUtils.setField(manager, "obsService", obsService);

        manager.syncUpgradeModels();
    }
}
