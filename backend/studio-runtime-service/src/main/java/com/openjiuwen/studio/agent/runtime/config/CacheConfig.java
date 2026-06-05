/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.config;

import com.github.benmanes.caffeine.cache.Caffeine;

import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * 本地缓存配置
 *
 */
@Configuration
public class CacheConfig {
    @Bean(name = "caffeineCache")
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
            // 初始容量
            .initialCapacity(50)
            // 最大个数
            .maximumSize(100)
            // 写入缓存或最后一次访问该缓存对象后，多长时间内没访问就过期
            .expireAfterWrite(1, TimeUnit.HOURS));
        return cacheManager;
    }
}
