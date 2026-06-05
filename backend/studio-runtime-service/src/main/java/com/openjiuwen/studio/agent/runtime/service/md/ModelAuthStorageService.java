/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service.md;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.openjiuwen.studio.agent.common.dto.md.NotAuthInfo;
import com.openjiuwen.studio.agent.common.dto.md.ProviderAuth;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.redis.RedisClient;
import com.openjiuwen.studio.agent.common.utils.JsonUtils;
import com.openjiuwen.studio.agent.runtime.dto.md.ApiKeyAuthInfo;
import com.openjiuwen.studio.agent.common.dto.md.CustomApiKeyAuthInfo;
import com.openjiuwen.studio.agent.runtime.entity.md.ProviderAuthData;
import com.openjiuwen.studio.agent.runtime.service.ObsService;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ModelAuthStorageService {
    /**
     * /model-auth/auth/projectId/providerId/认证配置ID.json
     */
    private static final String AUTH_INFO_PATH = "model-auth/auth/%s/%s/%s.json";

    private static final String AUTH_INFO_LIST_PATH = "model-auth/auth/%s/%s/";

    @Autowired
    private ObsService obsService;

    @Autowired
    private RedisClient redisClient;

    private final Cache<String, ProviderAuth> localCache = CacheBuilder.newBuilder()
        .expireAfterWrite(1, TimeUnit.MINUTES)
        .maximumSize(500)
        .build();

    /**
     * 获取模型认证配置
     *
     * @param projectId 项目ID，必须参数
     * @param workspaceId 空间ID，可选参数
     * @param providerId 供应商ID，必须参数
     * @param authId 认证配置ID，新版本必须的参数
     * @param refresh 是否从OBS刷新
     * @return 认证配置
     */
    public ProviderAuth queryProviderAuth(String projectId, String workspaceId, String providerId, String authId,
        boolean refresh, String modelWorkspace) {
        if (StringUtils.isEmpty(authId)) {
            log.info("Use auth id query auth. authId:{}", authId);
            return queryProviderAuthV1(projectId, workspaceId, providerId, refresh, modelWorkspace);
        }
        return queryProviderAuthV2(projectId, providerId, authId, refresh);
    }

    private String getLocalCacheKey(String projectId, String workspaceId, String providerId) {
        return projectId + "_" + workspaceId + "_" + providerId;
    }

    private String getRedisKey(String authId) {
        return "MODEL_AUTH_" + authId;
    }

    private ProviderAuth queryProviderAuthV1(String projectId, String workspaceId, String providerId, boolean refresh,
        String modelWorkspace) {
        log.warn("Query provider auth from old data.");
        if (refresh) {
            return getProviderAuthFromObsV1(projectId, providerId, workspaceId, modelWorkspace);
        }
        try {
            return localCache.get(getLocalCacheKey(workspaceId, projectId, providerId),
                () -> getProviderAuthFromObsV1(projectId, providerId, workspaceId, modelWorkspace));
        } catch (Exception e) {
            log.error("Fail get model auth from local cache. Old data. {}", e.getMessage());
            return getProviderAuthFromObsV1(projectId, providerId, workspaceId, modelWorkspace);
        }
    }

    private ProviderAuth getProviderAuthFromObsV1(String projectId, String providerId, String workspaceId,
        String modelWorkspace) {
        String path = String.format(Locale.ROOT, AUTH_INFO_LIST_PATH, projectId, providerId);
        List<String> objects = obsService.listObjectKeys(path);
        if (objects.isEmpty()) {
            log.error("Not found model auth config. path:{}", path);
            return null;
        }

        ProviderAuthData authData = null;
        if (StringUtils.isEmpty(workspaceId)) {
            String content = obsService.getObject(objects.get(0));
            log.warn("WorkspaceId is empty. select first auth. {}", objects.get(0));
            authData = JsonUtils.decode(content, ProviderAuthData.class);
        } else {
            ProviderAuthData data = null;
            for (String objKey : objects) {
                if (!objKey.endsWith(".json")) {
                    continue;
                }
                String content = obsService.getObject(objKey);
                data = JsonUtils.decode(content, ProviderAuthData.class);
                if (data.getWorkspaceId().equals(workspaceId) || data.getWorkspaceId().equals(modelWorkspace)) {
                    authData = data;
                    break;
                }
            }
        }
        if (authData == null) {
            log.error("Not found auth path.");
            return null;
        }

        ProviderAuth auth = getProviderAuth(authData.getId(), authData.getAuthType(), authData.getAuthInfo());
        if (auth != null) {
            localCache.put(getLocalCacheKey(workspaceId, projectId, providerId), auth);
        }
        return auth;
    }

    private ProviderAuth queryProviderAuthV2(String projectId, String providerId, String authId, boolean refresh) {
        if (refresh) {
            return getProviderAuthFromObs(projectId, providerId, authId);
        }
        try {
            return localCache.get(authId, () -> getProviderAuthFromObsOrRedis(projectId, providerId, authId));
        } catch (Exception e) {
            log.error("Fail get model auth from local cache. V2 . {}", e.getMessage());
            return getProviderAuthFromObsOrRedis(projectId, providerId, authId);
        }
    }

    private ProviderAuth getProviderAuthFromObsOrRedis(String projectId, String providerId, String authId) {
        String content = redisClient.get(getRedisKey(authId));
        if (StringUtils.isEmpty(content)) {
            return getProviderAuthFromObs(projectId, providerId, authId);
        }
        ProviderAuthData authData = JsonUtils.decode(content, ProviderAuthData.class);
        return getProviderAuth(authData.getId(), authData.getAuthType(), authData.getAuthInfo());
    }

    private ProviderAuth getProviderAuthFromObs(String projectId, String providerId, String authId) {
        String path = String.format(Locale.ROOT, AUTH_INFO_PATH, projectId, providerId, authId);
        ProviderAuthData authData = readAuthDataFromObs(path);
        ProviderAuth auth = null;
        if (authData != null) {
            redisClient.set(getRedisKey(authId), JsonUtils.encode(authData), Duration.ofMinutes(5));
            auth = getProviderAuth(authData.getId(), authData.getAuthType(), authData.getAuthInfo());
            localCache.put(authId, auth);
        }
        return auth;
    }

    private ProviderAuthData readAuthDataFromObs(String obsPath) {
        try {
            String content = obsService.getObject(obsPath);
            return JsonUtils.decode(content, ProviderAuthData.class);
        } catch (AgentStudioException e) {
            if (e.getErrorCode() == StudioError.OBS_FAILED) {
                return null;
            }
            throw e;
        }
    }

    public ProviderAuth getProviderAuth(String authId, String authType, String authInfo) {
        ProviderAuth auth = switch (authType) {
            case "API_KEY" -> JsonUtils.decode(authInfo, ApiKeyAuthInfo.class);
            case "CUSTOM_APIKEY" -> new CustomApiKeyAuthInfo(authInfo);
            case "NO_AUTH" -> new NotAuthInfo();
            default -> {
                log.error("auth type={} is not support", authType);
                throw new AgentStudioException(StudioError.METHOD_ARGUMENT_NOT_VALID);
            }
        };
        auth.setAuthId(authId);
        return auth;
    }
}

