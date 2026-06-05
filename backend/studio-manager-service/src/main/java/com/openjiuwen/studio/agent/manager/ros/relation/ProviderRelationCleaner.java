/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.ros.relation;

import com.openjiuwen.studio.agent.common.redis.RedisClient;
import com.openjiuwen.studio.agent.manager.entity.md.ProviderAuthData;
import com.openjiuwen.studio.agent.manager.mapper.md.ProviderAuthDataMapper;
import com.openjiuwen.studio.agent.manager.obs.MgObsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("Provider_RelateResourceCleaner")
public class ProviderRelationCleaner implements IRelateSourceCleaner {
    @Autowired
    MgObsService obsService;

    @Autowired
    private ProviderAuthDataMapper authMapper;

    @Autowired
    private RedisClient redisClient;

    private static final String AUTH_INFO_PATH = "model-auth/auth/%s/%s/%s.json";

    private static final String REDIS_PREFIX = "MODEL_AUTH_";


    @Override
    public void clean(List<String> ids, String domainId) {
        if (ids.size() > 0) {
            List<ProviderAuthData> providerAuthDataList = authMapper.selectByIds(ids);
            providerAuthDataList.forEach(data -> {
                String path = String.format(AUTH_INFO_PATH, data.getProjectId(), data.getProviderId(),
                        data.getId());
                obsService.deleteObsFile(path);
                redisClient.delete(REDIS_PREFIX + data.getId());
            });
        }
    }
}
