/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service.md;

import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.openjiuwen.studio.agent.common.dto.md.ProviderAuth;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.redis.RedisClient;
import com.openjiuwen.studio.agent.runtime.dto.ModelProps;
import com.openjiuwen.studio.agent.runtime.dto.md.ModelServiceDetail;
import com.openjiuwen.studio.agent.runtime.dto.md.ModelStrategy;
import com.openjiuwen.studio.agent.runtime.entity.md.ModelServiceBase;
import com.openjiuwen.studio.agent.common.entity.RouterStrategyEntity;
import com.openjiuwen.studio.agent.runtime.enums.ModelStrategyEnum;
import com.openjiuwen.studio.agent.runtime.service.ObsService;
import com.openjiuwen.studio.agent.runtime.thread.ModelApiLogThreadLocal;
import com.openjiuwen.studio.agent.runtime.utils.JsonUtils;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ModelStorageService {
    private static final String MODEL_PATH = "model-service/ir/%s.json";

    private static final String REDIS_PREFIX = "model_service_";

    @Autowired
    private ObsService obsService;

    @Autowired
    private ModelAuthStorageService authService;

    @Autowired
    private RedisClient redisClient;

    private final Cache<String, Optional<JSONObject>> localCache = CacheBuilder.newBuilder()
        .expireAfterWrite(1, TimeUnit.MINUTES)
        .maximumSize(500)
        .build();

    public ModelStrategy queryModelStrategy(String projectId, String workspaceId, String modelId, String authId,
        boolean refresh) {
        JSONObject object = queryModelMetadata(modelId, refresh).orElse(null);
        if (object == null) {
            return null;
        }
        return getModelStrategy(projectId, workspaceId, authId, refresh, object);
    }

    /**
     * Agent Ops数据上报，获取模型的信息
     * @param modelId 模型ID
     * @return 模型属性数据
     */
    public ModelProps queryModelProps(String modelId) {
        ModelServiceBase modelBase;
        JSONObject object = queryModelMetadata(modelId, false).orElse(null);
        if (object == null) {
            modelBase = new ModelServiceBase();
        } else {
            if (ModelStrategyEnum.ROUTER.getType().equals(object.getString("type"))) {
                // 路由策略
                RouterStrategyEntity entity = JSONObject.toJavaObject(object.getJSONObject("data"),
                    RouterStrategyEntity.class);
                modelBase = new ModelServiceBase().setModelName(entity.getStrategyName())
                    .setServiceName(entity.getStrategyName())
                    .setId(entity.getId());
            } else {
                // 普通模型服务
                modelBase = JSONObject.toJavaObject(object.getJSONObject("data"), ModelServiceBase.class);
            }
        }
        return new ModelProps().setModelId(modelBase.getId())
            .setModelProvider(modelBase.getProviderId())
            .setModelName(modelBase.getModelName());
    }

    private ModelStrategy getModelStrategy(String projectId, String workspaceId, String authId, boolean refresh,
        JSONObject modelMetadata) {
        if (ModelStrategyEnum.ROUTER.getType().equals(modelMetadata.getString("type"))) {
            // 路由策略
            return getRouterStrategy(projectId, workspaceId, modelMetadata.getJSONObject("data"), refresh);
        } else {
            // 普通模型服务
            ModelServiceDetail model = getModelServiceDetail(new HashMap<>(), modelMetadata.getJSONObject("data"),
                projectId, workspaceId, authId, refresh);
            return new ModelStrategy().setModels(Collections.singletonList(model))
                .setType(ModelStrategyEnum.MODEL)
                .setName(model.getModel().getModelName());
        }
    }

    private ModelStrategy getRouterStrategy(String projectId, String workspaceId, JSONObject strategy,
        boolean refresh) {
        if (strategy == null) {
            return null;
        }
        RouterStrategyEntity entity = JSONObject.toJavaObject(strategy, RouterStrategyEntity.class);
        String[] modelIds = entity.getServiceIdList().split(",");
        if (modelIds.length == 0) {
            log.error("Router strategy config is invalid.");
            throw new AgentStudioException(StudioError.UNEXPECTED_ERROR);
        }

        String[] authIds = StringUtils.isEmpty(entity.getAuthIdList())
            ? new String[] {""}
            : entity.getAuthIdList().split(",");

        Map<String, Optional<ProviderAuth>> authCache = new HashMap<>();
        List<ModelServiceDetail> models = new ArrayList<>(modelIds.length);

        for (int idx = 0; idx < modelIds.length; ++idx) {
            JSONObject obj = queryModelMetadata(modelIds[idx], refresh).map((o) -> o.getJSONObject("data"))
                .orElse(null);
            if (obj == null) {
                log.warn("Fail get model detail. model router miss model.");
                throw new AgentStudioException(StudioError.MD_MODEL_ROUTER_INVALID);
            }
            ModelServiceDetail detail = getModelServiceDetail(authCache, obj, projectId, workspaceId,
                authIds[Math.min(idx, authIds.length - 1)], refresh);

            models.add(detail);
        }

        return new ModelStrategy().setStrategyTimeout(entity.getStrategyTimeout())
            .setRetryCount(entity.getStrategyRetryCount())
            .setModels(models)
            .setType(ModelStrategyEnum.ROUTER)
            .setName(entity.getStrategyKey());
    }

    private ModelServiceDetail getModelServiceDetail(Map<String, Optional<ProviderAuth>> authCache, JSONObject object,
        String projectId, String workspaceId, String authId, boolean refresh) {
        ModelServiceBase model = JSONObject.toJavaObject(object, ModelServiceBase.class);

        String authProject = "SYSTEM".equals(model.getProjectId()) ? projectId : model.getProjectId();

        ModelServiceDetail detail = new ModelServiceDetail().setModel(model);
        String cacheKey = getAuthCacheKey(authProject, workspaceId, model.getProviderId(), authId);
        Optional<ProviderAuth> authOpt = authCache.get(cacheKey);
        if (authOpt == null || authOpt.isEmpty()) {
            ProviderAuth queriedAuth = authService.queryProviderAuth(authProject, workspaceId, model.getProviderId(), authId, refresh, model.getWorkspaceId());
            authOpt = Optional.ofNullable(queriedAuth);
            authCache.put(cacheKey, authOpt);
        }

        detail.setAuth(authOpt.orElse(null)).setAvailable(ModelApiLogThreadLocal.getApiLog().isFreeModel() || authOpt.isPresent());
        return detail;
    }

    private String getAuthCacheKey(String projectId, String workspaceId, String providerId, String authId) {
        return projectId + "_" + workspaceId + "_" + providerId + "_" + authId;
    }

    private Optional<JSONObject> queryModelMetadata(String modelId, boolean refresh) {
        if (refresh) {
            Optional<JSONObject> optional = queryModelMetadataFromObs(modelId);
            localCache.put(modelId, optional);
            return optional;
        }
        try {
            return localCache.get(modelId, () -> queryModelMetadataFromRedisOrObs(modelId));
        } catch (Exception e) {
            log.warn("Fail query model metadata from cache. {}", e.getMessage());
            return queryModelMetadataFromRedisOrObs(modelId);
        }
    }

    private Optional<JSONObject> queryModelMetadataFromRedisOrObs(String modelId) {
        String key = REDIS_PREFIX + modelId;
        String content = redisClient.get(key);
        if (content != null) {
            return Optional.of(JSONObject.parseObject(content));
        }
        return queryModelMetadataFromObs(modelId);
    }

    private boolean isPlatformModel(JSONObject object) {
        if (ModelStrategyEnum.ROUTER.getType().equals(object.getString("type"))) {
            // 路由策略
            return false;
        } else {
            // 普通模型服务
            return "SYSTEM".equals(object.getJSONObject("data").getString("project_id"));
        }
    }

    private Optional<JSONObject> queryModelMetadataFromObs(String modelId) {
        String objKey = String.format(MODEL_PATH, modelId);
        try {
            String content = obsService.getObject(objKey);
            JSONObject jsonObject = JSONObject.parseObject(content);
            String key = REDIS_PREFIX + modelId;
            if (isPlatformModel(jsonObject)) {
                redisClient.set(key, jsonObject.toJSONString());
            } else {
                redisClient.set(key, jsonObject.toJSONString(), Duration.ofMinutes(5));
            }
            return Optional.of(jsonObject);
        } catch (AgentStudioException e) {
            if (e.getErrorCode() == StudioError.OBS_FAILED) {
                log.error("bos failed {}" , (Object) e.getStackTrace());
                return Optional.empty();
            }
            log.error("Fail query model metadata from obs. ", e);
            throw e;
        }
    }
}
