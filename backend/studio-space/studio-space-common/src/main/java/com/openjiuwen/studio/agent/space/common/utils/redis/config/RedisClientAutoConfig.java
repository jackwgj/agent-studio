/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.common.utils.redis.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.openjiuwen.studio.agent.space.common.utils.redis.RedisClient;
import com.openjiuwen.studio.agent.space.common.utils.redis.RedisClientWrapper;
import com.openjiuwen.studio.agent.space.common.utils.redis.impl.RedisClientMemory;
import com.openjiuwen.studio.agent.space.common.utils.redis.impl.RedisClientRedisson;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.lang.reflect.Constructor;

@Configuration
@Lazy
@Slf4j
public class RedisClientAutoConfig {
    public static final String REDIS_CLIENT_3RD_CLASS
        = "com.openjiuwen.studio.agent.common.redis.RedisClient3rd";

    @Value("${redis.enable-log}")
    private boolean enableLog;

    /**
     * 加载Redisson实现
     *
     * @param redisClientConfig redis客户端配置
     * @return RedisClient
     */
    @Bean
    @ConditionalOnMissingClass(REDIS_CLIENT_3RD_CLASS)
    @ConditionalOnProperty(name = "redis.client-type", havingValue = "redisson")
    public RedisClient redissonClient(RedisClientConfig redisClientConfig) {
        RedisClientRedisson redisClientRedisson = new RedisClientRedisson(redisClientConfig);
        log.info("load redisson redis client");

        return new RedisClientWrapper(redisClientRedisson, enableLog);
    }

    /**
     * 加载外部SDK实现
     *
     * @param redisClientConfig redis客户端配置
     * @return RedisClient
     */
    @Bean
    @ConditionalOnClass(name = {REDIS_CLIENT_3RD_CLASS})
    public RedisClient thirdPartyClient(RedisClientConfig redisClientConfig) throws Exception {
        Class<?> clazz = Class.forName(REDIS_CLIENT_3RD_CLASS);
        Constructor<?> constructor = clazz.getConstructor(String.class, String.class, int.class);

        RedisClient redisClient = (RedisClient) constructor.newInstance(redisClientConfig.getRedisHost(),
            redisClientConfig.getRedisPassword(), redisClientConfig.getRedisPort());
        log.info("load external redis client {}", clazz.getName());

        return new RedisClientWrapper(redisClient, enableLog);
    }

    /**
     * 加载Redis内存实现（仅用于测试）
     *
     * @return RedisClient
     */
    @Bean
    @ConditionalOnProperty(name = "redis.client-type", havingValue = "memory")
    public RedisClient memoryClient() {
        return new RedisClientMemory();
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        // 创建 ObjectMapper 并配置
        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 其他配置
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(objectMapper.getPolymorphicTypeValidator(),
            ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);

        // 设置序列化器
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }
}
