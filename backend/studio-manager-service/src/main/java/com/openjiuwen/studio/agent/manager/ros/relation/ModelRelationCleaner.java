/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.ros.relation;

import com.openjiuwen.studio.agent.common.redis.RedisClient;
import com.openjiuwen.studio.agent.manager.obs.MgObsService;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("Model_RelateResourceCleaner")
public class ModelRelationCleaner implements IRelateSourceCleaner {

    @Autowired
    MgObsService obsService;

    @Autowired
    private RedisClient redisClient;

    private static final String MODEL_PATH = "model-service/ir/%s";

    private static final String REDIS_PREFIX = "model_service_";

    @Override
    public void clean(List<String> ids, String domainId) {
        if (ids.size() > 0) {
            // 删除obs
            for (String id : ids) {
                if (!StringUtils.isEmpty(id)) {
                    String path = String.format(MODEL_PATH, id + ".json");
                    obsService.deleteObsFile(path);
                    redisClient.delete(REDIS_PREFIX + id);
                }
            }
        }
    }
}
