/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service.md;

import com.openjiuwen.studio.agent.manager.entity.md.ModelServiceBase;
import com.openjiuwen.studio.agent.manager.entity.md.ModelServiceProvider;
import com.openjiuwen.studio.agent.manager.entity.md.ProviderAuthData;
import com.openjiuwen.studio.agent.manager.entity.md.ProviderAuthMetadata;
import com.openjiuwen.studio.agent.common.entity.RouterStrategyEntity;
import com.openjiuwen.studio.agent.manager.mapper.md.ModelServiceMapper;
import com.openjiuwen.studio.agent.manager.mapper.md.ProviderAuthDataMapper;
import com.openjiuwen.studio.agent.manager.mapper.md.ProviderAuthMetadataMapper;
import com.openjiuwen.studio.agent.manager.mapper.md.RouterStrategyMapper;
import com.openjiuwen.studio.agent.manager.mapper.md.UserModelServiceProviderMapper;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ModelServiceSchedule {
    private static final long HOUR_SECONDS = 3600;

    @Value("${model.soft.delete.enable:false}")
    private boolean modelSoftDelete;

    @Value("${model.soft.delete.cycle:168}")
    private long cycle;

    @Autowired
    private ModelServiceMapper modelServiceMapper;

    @Autowired
    private ProviderAuthMetadataMapper providerAuthMetadataMapper;

    @Autowired
    private ProviderAuthDataMapper providerAuthDataMapper;

    @Autowired
    private RouterStrategyMapper routerStrategyMapper;

    @Autowired
    private UserModelServiceProviderMapper userModelServiceProviderMapper;

    @Scheduled(cron = "0 0 0 */3 * ?")
    @Async
    public void syncModelPhysicsDeleteTask() {
        if (!modelSoftDelete) {
            return;
        }
        log.info("sync physical delete model info task start");
        long canLastDeleteTime = System.currentTimeMillis() - HOUR_SECONDS * cycle;

        try {
            // 物理删除数据
            physicalDeleteModelService(canLastDeleteTime);
            physicalDeleteProviderAuthData(canLastDeleteTime);
            physicalDeleteProviderAuthMetadata(canLastDeleteTime);
            physicalDeleteRouterStrategy(canLastDeleteTime);
            physicalDeleteUserModelServiceProvider(canLastDeleteTime);
        } catch (Exception e){
            log.error("sync physical delete model info task failed");
        }
        log.info("sync physical delete model info task finish");
    }

    /**
     * 物理删除modelService备份信息
     */
    private void physicalDeleteModelService(long canLastDeleteTime) {
        List<ModelServiceBase> modelServiceBases = modelServiceMapper.queryBackupByUpdateTime(canLastDeleteTime);
        if (modelServiceBases == null || modelServiceBases.isEmpty()) {
            return;
        }

        List<String> ids = modelServiceBases.stream()
                .map(ModelServiceBase::getId)
                .toList();
        modelServiceMapper.deleteBackupByIds(ids);
    }

    /**
     * 物理删除providerAuthData备份信息
     */
    private void physicalDeleteProviderAuthData(long canLastDeleteTime) {
        List<ProviderAuthData> providerAuthDataList = providerAuthDataMapper.queryBackupByUpdateTime(canLastDeleteTime);
        if (providerAuthDataList == null || providerAuthDataList.isEmpty()) {
            return;
        }

        List<String> ids = providerAuthDataList.stream()
                .map(ProviderAuthData::getId)
                .toList();
        providerAuthDataMapper.deleteBackupByIds(ids);
    }

    /**
     * 物理删除providerAuthMetadata备份信息
     */
    private void physicalDeleteProviderAuthMetadata(long canLastDeleteTime) {
        List<ProviderAuthMetadata> providerAuthMetadataList =
                providerAuthMetadataMapper.queryBackupByUpdateTime(canLastDeleteTime);
        if (providerAuthMetadataList == null || providerAuthMetadataList.isEmpty()) {
            return;
        }

        List<String> ids = providerAuthMetadataList.stream()
                .map(ProviderAuthMetadata::getId)
                .toList();
        providerAuthMetadataMapper.deleteBackupByIds(ids);
    }

    /**
     * 物理删除routerStrategy备份信息
     */
    private void physicalDeleteRouterStrategy(long canLastDeleteTime) {
        List<RouterStrategyEntity> routerStrategyEntities =
                routerStrategyMapper.queryBackupByUpdateTime(canLastDeleteTime);
        if (routerStrategyEntities == null || routerStrategyEntities.isEmpty()) {
            return;
        }

        List<String> ids = routerStrategyEntities.stream()
                .map(RouterStrategyEntity::getId)
                .toList();
        routerStrategyMapper.deleteBackupByIds(ids);
    }

    /**
     * 物理删除userModelServiceProvider备份信息
     */
    private void physicalDeleteUserModelServiceProvider(long canLastDeleteTime) {
        List<ModelServiceProvider> modelServiceProviders =
                userModelServiceProviderMapper.queryBackupByUpdateTime(canLastDeleteTime);
        if (modelServiceProviders == null || modelServiceProviders.isEmpty()) {
            return;
        }

        List<String> ids = modelServiceProviders.stream()
                .map(ModelServiceProvider::getId)
                .toList();
        userModelServiceProviderMapper.deleteBackupByIds(ids);
    }
}
