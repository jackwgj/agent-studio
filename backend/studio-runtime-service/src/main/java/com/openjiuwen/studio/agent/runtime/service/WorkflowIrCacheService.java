/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.service;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.openjiuwen.studio.agent.common.constant.Constants;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.sensitive.SensitiveEntity;
import com.openjiuwen.studio.agent.runtime.constant.Constant;
import com.openjiuwen.studio.agent.runtime.model.ExecuteParams;
import com.openjiuwen.studio.agent.runtime.model.WorkflowIrMetadata;
import com.openjiuwen.studio.agent.runtime.model.WorkflowIrWrapper;
import com.openjiuwen.studio.agent.runtime.utils.PerformUtils;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

/**
 * workflow缓存Service
 *
 */
@Slf4j
@Component
public class WorkflowIrCacheService {
    public static final String METADATA = "metadata";

    public static final String COMPONENTS = "components";

    private static final long MAX_NUM_SIZE = 10000L;

    private static final String GET_WORKFLOW_FAILED = "Did not get workflow ir by workflow id:%s.";

    @Autowired
    private ObsService obsService;

    private LoadingCache<ComposedIrKey, WorkflowIrWrapper> workflowIrCache;

    @Value("${workflow.ir-obs-path}")
    private String irObsPath;

    @Value("${workflow.ir-cache-hours}")
    private long irCacheInHours;

    @PostConstruct
    public void init() {
        workflowIrCache = Caffeine.newBuilder()
            .maximumSize(MAX_NUM_SIZE)
            .expireAfterAccess(irCacheInHours, TimeUnit.HOURS)
            .build(composedIrKey -> {
            Map<String, String> obsMaps = obsService.getObject(irObsPath + "/" + composedIrKey.workflowId(),
                composedIrKey + ".json");
            String irString = obsMaps.get(Constant.Obs.CONTENT);
            String md5 = obsMaps.get(Constant.Obs.MD5);
            if (StringUtils.isEmpty(irString) || StringUtils.isEmpty(md5)) {
                return null;
            }

            // 设置md5
            WorkflowIrWrapper cache = new WorkflowIrWrapper();
            cache.setMd5(obsMaps.get(Constant.Obs.MD5));

            // 设置metadata
            JSONObject ir = JSONObject.parseObject(irString);
            WorkflowIrMetadata metadata = ir.getObject(METADATA, WorkflowIrMetadata.class);
            cache.setMetadata(metadata);
            cache.setWorkflowName(ir.getString("workflowName"));

            // 设置idNameMap
            JSONArray components = ir.getJSONArray(COMPONENTS);
            Map<String, String> idNameMap = new HashMap<>();
            cache.setIdNameMap(idNameMap);
            if (!components.isEmpty()) {
                for (int i = 0; i < components.size(); i++) {
                    JSONObject component = components.getJSONObject(i);
                    idNameMap.put(component.getString("id"), component.getString("name"));
                }
            }

            JSONObject configs = ir.getJSONObject(Constants.Workflow.CONFIGS);

            // 内容审核
            JSONObject reviewObject = configs.getJSONObject(Constants.Workflow.CONTENT_REVIEW);
            if (reviewObject != null && reviewObject.getBooleanValue(Constants.Workflow.ENABLED, false)) {
                cache.setSensitiveEntity(reviewObject.toJavaObject(SensitiveEntity.class));
            }

            cache.setSafetyBarrier(Boolean.TRUE.equals(configs.getBoolean(Constants.Workflow.SAFETY_BARRIER)));
            return cache;
        });
    }

    /**
     * 获取ir path
     *
     * @param workflowId 工作流id
     * @return ir路径
     */
    public String getIrPath(String workflowId) {
        return String.format("%s/%s/%s.json", irObsPath, workflowId, workflowId);
    }

    /**
     * 获取指定版本ir path
     *
     * @param workflowId 工作流Id
     * @param versionId 版本号
     * @return
     */
    public String getIrPathWithVersion(String workflowId, String versionId) {
        return String.format("%s/%s/%s.json", irObsPath, workflowId, workflowId + "_" + versionId);
    }

    /**
     * 获取最新工作流IR
     *
     * @param executeParams 工作流执行参数
     * @param releasedVersion 工作流发布版本号
     * @return 返回工作流ir
     */
    public WorkflowIrWrapper refreshWorkflowIr(ExecuteParams executeParams, String releasedVersion) {
        long start = System.currentTimeMillis();
        String workflowIds = executeParams.getWorkflowId();
        ComposedIrKey composedIrKeys = new ComposedIrKey(workflowIds, releasedVersion);
        WorkflowIrWrapper ir = workflowIrCache.getIfPresent(composedIrKeys);
        try {
            // 未发布工作流如果缓存已存在并且缓存内容和obs内容md5不一致，则失效缓存
            if (ir != null && StringUtils.isEmpty(releasedVersion)) {
                String md5 = obsService.getMd5(irObsPath + "/" + workflowIds, workflowIds + ".json");
                if (!Objects.equals(md5, ir.getMd5())) {
                    workflowIrCache.invalidate(composedIrKeys);
                }
            }

            // 从缓存取ir
            ir = workflowIrCache.get(composedIrKeys);
        } catch (Exception e) {
            String error = String.format(GET_WORKFLOW_FAILED, workflowIds);
            log.error(error, e);
            throw new AgentStudioException(StudioError.WORKFLOW_NOT_EXIST);
        } finally {
            PerformUtils.accumulation(executeParams, Constant.Performance.LOAD_IR, System.currentTimeMillis() - start);
        }
        if (ir == null) {
            String error = String.format(GET_WORKFLOW_FAILED, workflowIds);
            log.error(error);
            throw new AgentStudioException(StudioError.WORKFLOW_NOT_EXIST);
        }
        return ir;
    }

    /**
     * 获取工作流IR
     *
     * @param workflowId 工作流id
     * @param releasedVersion 工作流发布版本号
     * @return 返回工作流ir
     */
    public WorkflowIrWrapper getWorkflowIr(String workflowId, String releasedVersion) {
        try {
            return workflowIrCache.get(new ComposedIrKey(workflowId, releasedVersion));
        } catch (Exception e) {
            String error = String.format(GET_WORKFLOW_FAILED, workflowId);
            log.error(error, e);
            throw new AgentStudioException(StudioError.WORKFLOW_NOT_EXIST);
        }
    }

    /**
     * 获取工作流metadata
     *
     * @param workflowId 工作流id
     * @param releasedVersion 工作流发布版本号
     * @return 工作流metadata
     */
    public WorkflowIrMetadata getIrMetadata(String workflowId, String releasedVersion) {
        return this.getWorkflowIr(workflowId, releasedVersion).getMetadata();
    }

    public String getIrWorkspaceFromCache(String workflowId, String releasedVersion) {
        ComposedIrKey key = new ComposedIrKey(workflowId, releasedVersion);
        WorkflowIrWrapper wrapper = workflowIrCache.getIfPresent(key);
        return wrapper == null ? null : wrapper.getMetadata().getWorkspaceId();
    }

    /**
     * 清理指定 workflow 缓存
     *
     * @param workflowId workflow id
     */
    public void clearWorkflowCache(String workflowId) {
        List<ComposedIrKey> keys =
            workflowIrCache.asMap().keySet().stream().filter(key -> key.toString().startsWith(workflowId)).toList();

        log.info("To clear workflow ir cache with ids: {}", keys);
        workflowIrCache.invalidateAll(keys);
    }

    private record ComposedIrKey(String workflowId, String releasedVersion) {
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ComposedIrKey that = (ComposedIrKey) o;
            return Objects.equals(workflowId, that.workflowId) && Objects.equals(releasedVersion, that.releasedVersion);
        }

        @Override
        public String toString() {
            if (StringUtils.isEmpty(releasedVersion)) {
                return workflowId;
            } else {
                return workflowId + "_" + releasedVersion;
            }
        }
    }
}
