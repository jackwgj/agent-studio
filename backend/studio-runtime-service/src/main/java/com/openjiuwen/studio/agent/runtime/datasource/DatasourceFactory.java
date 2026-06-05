/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.datasource;

import com.alibaba.fastjson2.JSON;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.redis.RedisClient;
import com.openjiuwen.studio.agent.runtime.dto.DatasourceConfig;
import com.openjiuwen.studio.agent.runtime.service.ObsService;
import com.zaxxer.hikari.HikariDataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Locale;
import java.util.Map;

import javax.annotation.PostConstruct;

/**
 * 数据源工厂类
 *
 */
@Component
public class DatasourceFactory {
    private static final String CONNECTION_KEY_TEMPLATE = "%s_%s";

    private static final String CONFIG_KEY_TEMPLATE = "ei:datasource:id:%s";

    private final Map<String, DataSource> dataSourceMap;

    private final RedisClient redisClient;

    private final ObsService obsService;

    private Cache<String, HikariDataSource> datasourceConnectionCache;

    @Value("${datasource.ir-path:}")
    private String datasourceIrPath;

    @Value("${datasource.cache.pool-size}")
    private int datasourceCachePoolSize;

    @Value("${datasource.cache.hours}")
    private int datasourceCacheHours;

    @Autowired
    public DatasourceFactory(Map<String, DataSource> dataSources, RedisClient redisClient, ObsService obsService) {
        // 自动注入所有DataSource实现，以Bean名称为key
        this.dataSourceMap = dataSources;
        this.redisClient = redisClient;
        this.obsService = obsService;
    }

    /**
     * 数据源缓存初始化，LRU策略
     */
    @PostConstruct
    public void init() {
        datasourceConnectionCache = Caffeine.newBuilder()
            .maximumSize(datasourceCachePoolSize)
            .expireAfterAccess(Duration.ofHours(datasourceCacheHours))
            .removalListener((String key, HikariDataSource dataSource, RemovalCause cause) -> {
                // 缓存移除时释放连接池
                if (dataSource != null) {
                    dataSource.close();
                }
            })
            .build();
    }

    /**
     * 根据数据源id获取数据源配置信息
     *
     * @param datasourceId 数据源id
     * @param shouldByCache 是否从缓存中取
     * @return 数据源配置信息
     */
    private DatasourceConfig getDatasourceConfig(String datasourceId, boolean shouldByCache) {
        String configCacheKey = generateConfigCacheKey(datasourceId);
        String configJson;

        // 引擎侧调用，默认从缓存读配置，读不到从obs并刷新
        if (shouldByCache && redisClient.exists(configCacheKey)) {
            configJson = redisClient.get(configCacheKey);
            return JSON.parseObject(configJson, DatasourceConfig.class);
        }
        // 数据源刷新新增修改配置或redis缓存配置失效，从obs最新配置，强制刷新数据源配置缓存
        configJson = obsService.getObject(datasourceIrPath + "/" + datasourceId + ".json");
        redisClient.set(configCacheKey, configJson, Duration.ofHours(datasourceCacheHours));
        return JSON.parseObject(configJson, DatasourceConfig.class);
    }

    /**
     * 根据类型以及版本获取对应数据源连接池并写入缓存
     *
     * @param datasourceId 数据源id
     * @param shouldByCache 数据源配置页面进入必须重新建立连接池，不可从缓存读取
     * @return 数据源连接池
     */
    public HikariDataSource getConnectionPool(String datasourceId, boolean shouldByCache) {
        HikariDataSource connectionPool = null;
        DatasourceConfig datasourceConfig = getDatasourceConfig(datasourceId, shouldByCache);

        // 本地连接池缓存key为{datasource_id}_{updated_on}，config配置为最新可保证连接池为最新状态
        if (shouldByCache) {
            connectionPool = datasourceConnectionCache.getIfPresent(generateConnectionCacheKey(datasourceConfig));
        }
        // 数据源刷新新增修改配置或缓存失效，重新生成并刷新本地cache
        return connectionPool == null ? getConnectionPool(datasourceConfig) : connectionPool;
    }

    private HikariDataSource getConnectionPool(DatasourceConfig datasourceConfig) {
        // 根据数据库类型获取对应数据库实现类
        DataSource dataSource = dataSourceMap.get(datasourceConfig.getType().toLowerCase(Locale.ROOT));
        if (dataSource == null) {
            throw new AgentStudioException(StudioError.UNSUPPORTED_DATA_SOURCE);
        }
        // 获取对应连接池
        HikariDataSource connectionPool = dataSource.getConnectionPool(datasourceConfig.getConnectionInfo());
        datasourceConnectionCache.put(generateConnectionCacheKey(datasourceConfig), connectionPool);
        return connectionPool;
    }

    private String generateConnectionCacheKey(DatasourceConfig datasourceConfig) {
        // 连接池本地缓存键为数据源id+数据源更新时间
        return String.format(CONNECTION_KEY_TEMPLATE, datasourceConfig.getId(), datasourceConfig.getUpdatedOn());
    }

    private String generateConfigCacheKey(String datasourceId) {
        // redis缓存键值为数据源id
        return String.format(CONFIG_KEY_TEMPLATE, datasourceId);
    }
}
