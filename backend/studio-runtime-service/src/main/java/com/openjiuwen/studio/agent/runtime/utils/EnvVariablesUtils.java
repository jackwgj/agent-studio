/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.utils;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.redis.RedisClient;
import com.openjiuwen.studio.agent.common.utils.CryptoUtils;
import com.openjiuwen.studio.agent.runtime.dto.EnvVariablesDto;
import com.openjiuwen.studio.agent.runtime.model.AgentExecuteParams;
import com.openjiuwen.studio.agent.runtime.model.ExecuteParams;
import com.openjiuwen.studio.agent.runtime.service.ObsService;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 环境变量获取辅助类
 *
 */
@Component
@Slf4j
public class EnvVariablesUtils {
    private static final String ENV_VAR_FORMAT = "environment:%s:workspaceId:%s";

    private static final long MAX_ENV_CACHE_SIZE = 2000L;

    @Value("${workflow.ir-obs-path}")
    private String irObsPath;

    private final RedisClient redisClient;

    private final ObsService obsService;

    private LoadingCache<EnvCacheKey, String> workflowEnvCache;

    public EnvVariablesUtils(RedisClient redisClient, ObsService obsService) {
        this.redisClient = redisClient;
        this.obsService = obsService;
    }

    @PostConstruct
    public void init() {
        workflowEnvCache = Caffeine.newBuilder().maximumSize(MAX_ENV_CACHE_SIZE).build(envKey -> {
            String path = irObsPath + "/" + envKey.appId + "/" + envKey + "_env.json";

            String cachedEnv = StringUtils.EMPTY;
            try {
                cachedEnv = obsService.getObject(path);
            } catch (AgentStudioException e) {
                log.warn("Get env file from obs failed.", e);
            }

            return cachedEnv;
        });
    }

    public Map<String, Object> getEnvironmentVariables(ExecuteParams executeParams) {
        log.info("Get environment variables, isDebug: {},environment id: {}, workspace id: {}", executeParams.isDebug(),
            executeParams.getEnvironmentId(), executeParams.getWorkspaceId());
        String variablesStr;
        if (executeParams.isDebug()) {
            String redisKey =
                String.format(ENV_VAR_FORMAT, executeParams.getEnvironmentId(), executeParams.getWorkspaceId());
            variablesStr = redisClient.get(redisKey);
            if (StringUtils.isBlank(variablesStr)) {
                log.warn("read env from redis return invalid value, redis key: {}", redisKey);
                return wrappedMap(new HashMap<>());
            }
        } else {
            EnvCacheKey cacheKey = new EnvCacheKey(executeParams.getWorkflowId(), executeParams.getReleasedVersion(),
                executeParams.getEnvironmentId());
            variablesStr = workflowEnvCache.get(cacheKey);
            if (StringUtils.isBlank(variablesStr)) {
                log.warn("read env from obs return invalid value, obs path: {}", cacheKey);
                return wrappedMap(new HashMap<>());
            }
        }
        List<EnvVariablesDto> variablesDtos = JsonUtils.json2Obj(variablesStr, EnvVariablesDto.TYPE_REFERENCE);
        if (CollectionUtils.isEmpty(variablesDtos)) {
            log.warn("read env with invalid value: {}", variablesStr);
            return wrappedMap(new HashMap<>());
        }
        Map<String, Object> varMap = new HashMap<>();
        List<String> secretKeys = new ArrayList<>();
        for (EnvVariablesDto dto : variablesDtos) {
            if (StringUtils.isBlank(dto.getName()) || dto.getValue() == null || dto.getValue().getContent() == null) {
                continue;
            }
            String content = dto.getValue().getContent();
            if (StringUtils.isBlank(content)) {
                varMap.put(dto.getName(), content);
                continue;
            }
            if (dto.getValue().isSecret()) {
                content = CryptoUtils.decrypt(content);
                secretKeys.add(dto.getName());
            }
            if (Strings.CS.equals(dto.getValue().getType(), "number")) {
                try {
                    varMap.put(dto.getName(), NumberFormat.getInstance().parse(content));
                } catch (ParseException e) {
                    log.warn("Variable {} with value {} parse error", dto.getName(), content);
                    varMap.put(dto.getName(), content);
                }
            } else {
                varMap.put(dto.getName(), content);
            }
        }
        Map<String, Object> result = wrappedMap(varMap);
        result.put("_secretEnvKeys", secretKeys);
        return result;
    }

    public Map<String, Object> getEnvironmentVariables(AgentExecuteParams executeParams) {
        if (StringUtils.isBlank(executeParams.getEnvironmentId())) {
            return wrappedMap(new HashMap<>());
        }
        log.info("Get environment variables for agent, isDebug: {}, environment id: {}, workspace id: {}",
            executeParams.isDebug(), executeParams.getEnvironmentId(), executeParams.getWorkspaceId());
        String variablesStr;
        if (executeParams.isDebug()) {
            String redisKey =
                String.format(ENV_VAR_FORMAT, executeParams.getEnvironmentId(), executeParams.getWorkspaceId());
            variablesStr = redisClient.get(redisKey);
            if (StringUtils.isBlank(variablesStr)) {
                log.warn("read env from redis return invalid value, redis key: {}", redisKey);
                return wrappedMap(new HashMap<>());
            }
        } else {
            EnvCacheKey cacheKey = new EnvCacheKey(executeParams.getAgentId(),
                executeParams.getVersionId(), executeParams.getEnvironmentId());
            variablesStr = workflowEnvCache.get(cacheKey);
            if (StringUtils.isBlank(variablesStr)) {
                log.warn("read env from obs return invalid value, obs path: {}", cacheKey);
                return wrappedMap(new HashMap<>());
            }
        }
        List<EnvVariablesDto> variablesDtos = JsonUtils.json2Obj(variablesStr, EnvVariablesDto.TYPE_REFERENCE);
        if (CollectionUtils.isEmpty(variablesDtos)) {
            log.warn("read env with invalid value: {}", variablesStr);
            return wrappedMap(new HashMap<>());
        }
        Map<String, Object> varMap = new HashMap<>();
        List<String> secretKeys = new ArrayList<>();
        for (EnvVariablesDto dto : variablesDtos) {
            if (StringUtils.isBlank(dto.getName()) || dto.getValue() == null || dto.getValue().getContent() == null) {
                continue;
            }
            String content = dto.getValue().getContent();
            if (StringUtils.isBlank(content)) {
                varMap.put(dto.getName(), content);
                continue;
            }
            if (dto.getValue().isSecret()) {
                content = CryptoUtils.decrypt(content);
                secretKeys.add(dto.getName());
            }
            if (Strings.CS.equals(dto.getValue().getType(), "number")) {
                try {
                    varMap.put(dto.getName(), NumberFormat.getInstance().parse(content));
                } catch (ParseException e) {
                    log.warn("Variable {} with value {} parse error", dto.getName(), content);
                    varMap.put(dto.getName(), content);
                }
            } else {
                varMap.put(dto.getName(), content);
            }
        }
        Map<String, Object> result = wrappedMap(varMap);
        result.put("_secretEnvKeys", secretKeys);
        return result;
    }

    private Map<String, Object> wrappedMap(Map<String, Object> map) {
        Map<String, Object> newMap = new HashMap<>();
        newMap.put("plugin_url_params", map);
        return newMap;
    }

    /**
     * 环境变量缓存 key
     *
     * @param appId 应用 id
     * @param version 应用版本
     * @param envId 环境 id
     */
    record EnvCacheKey(String appId, String version, String envId) {
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            EnvCacheKey that = (EnvCacheKey) o;
            return Strings.CS.equals(appId, that.appId) && Strings.CS.equals(version, that.version)
                && Strings.CS.equals(envId, that.envId);
        }

        @Override
        public int hashCode() {
            int result = 17;
            result = 31 * result + (appId != null ? appId.hashCode() : 0);
            result = 31 * result + (version != null ? version.hashCode() : 0);
            result = 31 * result + (envId != null ? envId.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder(appId);
            if (StringUtils.isNotEmpty(version)) {
                builder.append("_").append(version);
            }
            if (StringUtils.isNotEmpty(envId)) {
                builder.append("_").append(envId);
            }
            return builder.toString();
        }
    }
}
