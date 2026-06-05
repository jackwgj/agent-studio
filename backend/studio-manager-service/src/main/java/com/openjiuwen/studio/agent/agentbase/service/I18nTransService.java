/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeBaseI18nResourceEntity;
import com.openjiuwen.studio.agent.agentbase.mapper.KnowledgeBaseI18nResourceMapper;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 国际化翻译服务
 * <p>
 * 采用"数据库存储 + 应用启动全量加载 + 内存缓存读取"的策略
 * 1. 存储层 (MySQL): 作为数据的"真理来源"，持久化存储所有配置
 * 2. 缓存层 (Local Memory): 应用启动时，将 MySQL 中的数据全部读取到 JVM 内存（Map）中
 * 3. 应用层 (Service): 所有翻译请求直接从内存 Map 获取，耗时为纳秒级，完全不涉及数据库 IO
 *
 * @author system
 * @since 2025-01-24
 */
@Slf4j
@Service
public class I18nTransService {

    /**
     * 支持的语言集合，线程安全
     */
    private final Set<String> supportedLocales = ConcurrentHashMap.newKeySet();

    /**
     * Caffeine缓存实例
     * 使用缓存结构: <Locale, <Key, Message>>
     * 比如: get("zh-cn").get("connector.LakeSearch.description")
     */
    private final Cache<String, Map<String, String>> i18nCache = Caffeine.newBuilder()
        .maximumSize(100)
        .expireAfterWrite(1, TimeUnit.HOURS)
        .build();

    @Autowired
    private KnowledgeBaseI18nResourceMapper i18nMapper;

    /**
     * 初始化方法，在类注入时预加载缓存
     */
    @PostConstruct
    private void init() {
        log.info("Starting to initialize i18n resource cache...");

        // 全量获取所有语言资源
        List<KnowledgeBaseI18nResourceEntity> allResources = i18nMapper.findAll();

        // 构建临时Map来收集所有语言
        Map<String, Map<String, String>> localeToResourcesMap = new ConcurrentHashMap<>();

        for (KnowledgeBaseI18nResourceEntity resource : allResources) {
            // 收索支持的语言
            supportedLocales.add(resource.getLocale());

            // 构建语言包
            localeToResourcesMap.computeIfAbsent(resource.getLocale(), k -> new ConcurrentHashMap<>())
                .put(resource.getI18nKey(), resource.getMessage());
        }

        // 使用Caffeine原子加载方式将数据放入缓存
        for (Map.Entry<String, Map<String, String>> entry : localeToResourcesMap.entrySet()) {
            String locale = entry.getKey();
            Map<String, String> resources = Collections.unmodifiableMap(entry.getValue());
            i18nCache.put(locale, resources);
        }

        log.info("I18n resource initialization completed, supported locales: {}, total resource keys: {}",
            supportedLocales.size(), allResources.size());
    }

    /**
     * 对外提供的获取方法，使用Caffeine原子加载避免缓存击穿
     * 如果未找到对应语言的翻译，直接返回默认值。
     *
     * @param locale 语言区域 (如：zh-cn, en-us)
     * @param key 国际化Key
     * @param defaultValue 默认值（如果没有找到对应的翻译）
     * @return 翻译后的文案
     */
    public String getMessage(String locale, String key, String defaultValue) {
        if (locale == null || key == null) {
            return defaultValue;
        }

        // 使用Caffeine原子加载方式，避免缓存击穿
        Map<String, String> languagePack = i18nCache.get(locale, loc -> {
            // 当缓存不存在时，从数据库加载整个语言包
            List<KnowledgeBaseI18nResourceEntity> resources = i18nMapper.findByLocale(loc);
            Map<String, String> localeMap = new ConcurrentHashMap<>();

            for (KnowledgeBaseI18nResourceEntity resource : resources) {
                localeMap.put(resource.getI18nKey(), resource.getMessage());
            }

            log.info("Loaded i18n resources for locale: {}, count: {}", loc, localeMap.size());
            return Collections.unmodifiableMap(localeMap);
        });

        // 从语言包中获取指定key的翻译
        if (languagePack != null) {
            String message = languagePack.get(key);
            return message != null ? message : defaultValue;
        }

        return defaultValue;
    }

    /**
     * 检查是否支持指定的语言
     * 通过预加载的supportedLocales集合进行检查
     *
     * @param locale 语言区域
     * @return 是否支持该语言
     */
    public boolean containsLocale(String locale) {
        if (locale == null) {
            return false;
        }

        return supportedLocales.contains(locale);
    }
}
