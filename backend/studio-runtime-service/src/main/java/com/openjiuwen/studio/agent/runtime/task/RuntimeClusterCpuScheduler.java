/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.task;

import com.openjiuwen.studio.agent.common.redis.RedisClient;
import com.openjiuwen.studio.agent.common.service.K8sResourceService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
@Slf4j
@EnableScheduling
@EnableAsync
@ConditionalOnProperty(name = "env_type", havingValue = "hcs")
public class RuntimeClusterCpuScheduler {
    /**
     * runtime所在集群vCPU用量
     */
    public static final String RUNTIME_CPU_NUMS = "RUNTIME_CPU_NUMS";

    @Autowired
    private RedisClient redisClient;

    @Autowired
    private K8sResourceService k8SResourceService;

    @Scheduled(cron = "0 0/14 * * * ?")
    @Async
    public void syncLicenseTask() {
        double agentResourceCpu = k8SResourceService.getAgentResourceCpu();
        if (agentResourceCpu == 0) {
            log.error("get RuntimeClusterCpu failed.");
            return;
        }
        redisClient.set(RUNTIME_CPU_NUMS, String.valueOf(agentResourceCpu));
        log.info("sync RuntimeClusterCpu: {}", agentResourceCpu);
    }

}
