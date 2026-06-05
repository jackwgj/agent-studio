/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service;

import com.alibaba.fastjson2.JSONObject;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.openjiuwen.studio.agent.common.constant.Constants;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.sensitive.SensitiveEntity;
import com.openjiuwen.studio.agent.runtime.constant.Constant;
import com.openjiuwen.studio.agent.runtime.entity.Extension;
import com.openjiuwen.studio.agent.runtime.entity.HyperParameters;
import com.openjiuwen.studio.agent.runtime.entity.ModelConfig;
import com.openjiuwen.studio.agent.runtime.enums.AgentIrType;
import com.openjiuwen.studio.agent.runtime.model.AgentIrContext;
import com.openjiuwen.studio.agent.runtime.model.AgentIrMetadata;
import com.openjiuwen.studio.agent.runtime.model.AgentIrWrapper;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.PostConstruct;

/**
 * IR缓存Service
 *
 */
@Slf4j
@Component
public class AgentIrCacheService {
    private static final int MAX_CACHE_SIZE = 10000;

    private static final String METADATA = "metadata";

    @Autowired
    private ObsService obsService;

    private LoadingCache<String, AgentIrWrapper> agentIrCache;

    private LoadingCache<String, AgentIrWrapper> publishedAgentIrCache;

    @Value("${agent.ir-obs-path}")
    private String irObsPath;

    @Value("${agent.ir-cache-hours}")
    private long irCacheInHours;

    /**
     * 服务启动时初始化缓存
     */
    @PostConstruct
    public void init() {
        agentIrCache = Caffeine.newBuilder()
            .maximumSize(MAX_CACHE_SIZE)
            .expireAfterAccess(Duration.ofHours(irCacheInHours))
            .build(this::getIrWrapperFromObs);
        publishedAgentIrCache = Caffeine.newBuilder()
            .maximumSize(MAX_CACHE_SIZE)
            .expireAfterAccess(Duration.ofHours(irCacheInHours))
            .build(this::getPublishedIrWrapperFromObs);
    }

    /**
     * 获取最新的调试中的知识型Agent IR
     *
     * @param agentId agent id (对于百宝箱的应用，是 agent_id + _published)
     * @return AgentIrWrapper
     */
    public AgentIrWrapper getLatestAgentIr(String agentId) {
        // 从本地缓存中获取IR，获取不到则从OBS中获取，并更新到本地缓存中
        AgentIrWrapper agentIrs = agentIrCache.get(agentId);
        if (Objects.isNull(agentIrs)) {
            log.error("Fail to get agent ir by agent id: [{}]", agentId);
            throw new AgentStudioException(StudioError.AGENT_NOT_EXIST);
        }

        // 验证本地缓存与OBS一致性（对于调试中的agent，本地缓存可能是旧的IR）
        String md5 = obsService.getMd5(irObsPath + "/" + agentId, agentId + ".json");
        if (Objects.equals(md5, agentIrs.getMd5())) {
            return agentIrs;
        }

        // 如果本地缓存与OBS不一致，则刷新本地缓存
        agentIrCache.invalidate(agentId);
        return agentIrCache.get(agentId);
    }

    /**
     * 获取最新的已发布的知识型Agent IR
     *
     * @param agentId 已发布agent的id
     * @param versionId 版本id（百宝箱是固定值 published）
     * @return AgentIrWrapper
     */
    public AgentIrWrapper getPublishedAgentIr(String agentId, String versionId) {
        log.debug("Entering getPublishedAgentIr with agentId={}, versionId={}", agentId, versionId);

        // 缓存Key，百宝箱是agentId_published，已发布版本是agentId_versionId
        String cacheKeys = agentId + "_" + (StringUtils.isBlank(versionId) ? "published" : versionId);

        // 从本地缓存中获取IR，获取不到则从OBS中获取，并更新到本地缓存中
        AgentIrWrapper agentIr = publishedAgentIrCache.get(cacheKeys);
        if (Objects.isNull(agentIr)) {
            log.error("Fail to get agent ir by agent id [{}], version id [{}]", agentId, versionId);
            throw new AgentStudioException(StudioError.AGENT_NOT_EXIST);
        }
        return agentIr;
    }

    public String getIrWorkspaceFromCache(String agentId, String releasedVersion) {
        AgentIrWrapper wrapper;
        if (StringUtils.isEmpty(releasedVersion)) {
            wrapper = agentIrCache.getIfPresent(agentId);
        } else {
            wrapper = publishedAgentIrCache.getIfPresent(agentId + "_" + releasedVersion);
        }
        return wrapper == null ? null : wrapper.getMetadata().getWorkspaceId();
    }

    /**
     * 从OBS获取IR信息
     *
     * @param agentId agent id (对于百宝箱的应用，是 agent_id + _published)
     * @return AgentIrWrapper
     */
    public AgentIrWrapper getIrWrapperFromObs(String agentId) {
        Map<String, String> obsMap = obsService.getObject(irObsPath + "/" + agentId, agentId + ".json");
        return getIrWrapperFromObsMap(obsMap);
    }

    /**
     * 从OBS获取已发布的IR信息
     *
     * @param key agent_id + "_" + version_id
     * @return AgentIrWrapper
     */
    public AgentIrWrapper getPublishedIrWrapperFromObs(String key) {
        String agentId = StringUtils.substringBeforeLast(key, "_");
        Map<String, String> obsMap = obsService.getObject(irObsPath + "/" + agentId, key + ".json");
        return getIrWrapperFromObsMap(obsMap);
    }

    /**
     * 清理指定 agent 缓存
     *
     * @param agentId agent id
     */
    public void clearAgentCache(String agentId) {
        agentIrCache.invalidate(agentId);
        log.info("To clear agent ir cache with id: {}", agentId);

        List<String> keys =
            publishedAgentIrCache.asMap().keySet().stream().filter(key -> key.startsWith(agentId)).toList();

        log.info("To clear published agent ir cache with ids: {}", keys);
        publishedAgentIrCache.invalidateAll(keys);
    }

    private AgentIrWrapper getIrWrapperFromObsMap(Map<String, String> obsMap) {
        String irContents = obsMap.get(Constant.Obs.CONTENT);
        String md5 = obsMap.get(Constant.Obs.MD5);
        if (StringUtils.isEmpty(irContents) || StringUtils.isEmpty(md5)) {
            log.warn("No agent IR can be found");
            return null;
        }
        AgentIrWrapper agentIrWrapper = new AgentIrWrapper();
        agentIrWrapper.setMd5(md5);
        JSONObject ir = JSONObject.parseObject(irContents);
        AgentIrMetadata metadata = ir.getObject(METADATA, AgentIrMetadata.class);
        JSONObject configs = ir.getJSONObject(Constant.CONFIGS);
        JSONObject modelConfigMaps = configs.containsKey(Constant.MODEL_CONFIG)
                ? configs.getJSONObject(Constant.MODEL_CONFIG)
                : configs.getJSONObject(Constant.LLM_CONFIG);
        agentIrWrapper.setMetadata(metadata);
        if (modelConfigMaps != null) {
            agentIrWrapper.setModelConfig(ModelConfig.builder()
                .modelName(modelConfigMaps.getString("modelName"))
                .modelType(modelConfigMaps.getString("modelType"))
                .hyperParameters(modelConfigMaps.getObject("hyperParameters", HyperParameters.class))
                .extension(modelConfigMaps.getObject("extension", Extension.class))
                .build());
        }

        AgentIrContext irContext = ir.getJSONObject(Constant.CONFIGS).getObject(Constant.CONTEXT, AgentIrContext.class);
        agentIrWrapper.setContext(irContext);
        // 内容审核
        JSONObject reviewObjects = ir.getJSONObject(Constants.Workflow.CONFIGS)
            .getJSONObject(Constants.Workflow.CONTENT_REVIEW);
        if (reviewObjects != null && reviewObjects.getBooleanValue(Constants.Workflow.ENABLED, false)) {
            agentIrWrapper.setSensitiveEntity(reviewObjects.toJavaObject(SensitiveEntity.class));
        }
        agentIrWrapper.setSafetyBarrier(Boolean.TRUE.equals(
            ir.getJSONObject(Constants.Workflow.CONFIGS).getBoolean(Constants.Workflow.SAFETY_BARRIER)));
        agentIrWrapper.setIrType(parseIrType(ir));
        return agentIrWrapper;
    }

    private AgentIrType parseIrType(JSONObject ir) {
        if (ir.containsKey("workflowId")) {
            return AgentIrType.WORKFLOW;
        }
        if (ir.containsKey("agentId") && ir.containsKey(Constant.CONFIGS)) {
            JSONObject configs = ir.getJSONObject(Constant.CONFIGS);
            if (!CollectionUtils.isEmpty(configs.getJSONArray("agents"))) {
                return AgentIrType.MULTI_AGENTS;
            }
            if ("PlanExecute".equals(configs.get("mode"))) {
                return AgentIrType.PLAN_EXECUTE;
            }
            if (configs.containsKey("deep_search_execute_mode")) {
                return AgentIrType.DEEP_RESEARCH;
            }
            return AgentIrType.AGENT;
        }
        return AgentIrType.UNKNOWN;
    }
}
