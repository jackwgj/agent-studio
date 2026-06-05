/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service.md;

import com.openjiuwen.studio.agent.common.dto.md.NotAuthInfo;
import com.openjiuwen.studio.agent.common.dto.md.ProviderAuth;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.redis.RedisClient;
import com.openjiuwen.studio.agent.common.redis.RedisLock;
import com.openjiuwen.studio.agent.common.utils.JsonUtils;
import com.openjiuwen.studio.agent.manager.dto.ProviderAuthConfig;
import com.openjiuwen.studio.agent.manager.dto.md.ApiKeyAuthInfo;
import com.openjiuwen.studio.agent.common.dto.md.CustomApiKeyAuthInfo;
import com.openjiuwen.studio.agent.manager.entity.md.ProviderAuthData;
import com.openjiuwen.studio.agent.manager.entity.md.ProviderAuthMetadata;
import com.openjiuwen.studio.agent.manager.mapper.md.ProviderAuthDataMapper;
import com.openjiuwen.studio.agent.manager.mapper.md.ProviderAuthMetadataMapper;
import com.openjiuwen.studio.agent.manager.obs.MgObsService;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ProviderAuthService {
    private static final String SYNC_UPGRADE_AUTHDATGA_LOCK = "SYNC_UPGRADE_AUTHDATGA_LOCK";

    /**
     * /model-auth/auth/projectId/providerId/认证配置ID.json
     */
    private static final String AUTH_INFO_PATH = "model-auth/auth/%s/%s/%s.json";

    private static final String REDIS_PREFIX = "MODEL_AUTH_";

    private static final String MODEL_AUTH_UPDATE_TIME_KEY = "MODEL_AUTH_UPDATE_TIME_KEY";

    @Value("${model.upgrade.sync.enable:false}")
    private boolean upgradeSyncEnable;

    @Autowired
    private ProviderAuthMetadataMapper metadataMapper;

    @Autowired
    private ProviderAuthDataMapper authMapper;

    @Autowired
    private MgObsService obsService;

    @Autowired
    private RedisClient redisClient;

    @PostConstruct
    public void postConstruct() {
        CompletableFuture.runAsync(() -> {
            try {
                syncAuthDataToNewPath();
            } catch (Exception e) {
                log.error("Fail sync auth data to new path.", e);
            }
        });

        if (!upgradeSyncEnable) {
            log.info("model.upgrade.sync.enable value is false.");
            return;
        }
        Executors.newSingleThreadScheduledExecutor()
            .scheduleAtFixedRate(this::syncUpgradeAuthDatas, 1, 5, TimeUnit.MINUTES);
    }

    void syncUpgradeAuthDatas() {
        RedisLock lock = null;
        try {
            lock = redisClient.getLock(SYNC_UPGRADE_AUTHDATGA_LOCK);
            if (lock.tryLock(Duration.ofSeconds(30))) {
                List<ProviderAuthData> datas = authMapper.queryNotSyncDatas();
                for (ProviderAuthData data : datas) {
                    String path = String.format(AUTH_INFO_PATH, data.getProjectId(), data.getProviderId(),
                        data.getId());
                    String content = JsonUtils.encode(data);
                    obsService.putObject(path, content);
                    redisClient.set(REDIS_PREFIX + data.getId(), content);
                    authMapper.updateSyncStatus(data.getId());
                }
            } else {
                log.warn("SyncUpgradeAuthData Fail get {} lock.", SYNC_UPGRADE_AUTHDATGA_LOCK);
            }
        } catch (Exception e) {
            log.error("Sync UpgradeAuthData to obs exception.", e);
        } finally {
            if (lock != null) {
                lock.unlock();
            }
        }
    }

    void syncAuthDataToNewPath() {
        log.info("Start to sync auth data to new path.");
        String updateTimeStr = redisClient.get(MODEL_AUTH_UPDATE_TIME_KEY);
        long time = System.currentTimeMillis();
        if (!StringUtils.isEmpty(updateTimeStr)) {
            time = Long.parseLong(updateTimeStr);
        }
        if (System.currentTimeMillis() - time > 24L * 60 * 60 * 1000) {
            log.info("Not need sync auth data. updateTime:{}", time);
            return;
        }
        List<ProviderAuthData> datas = authMapper.selectAllAuthByTime(time);
        for (ProviderAuthData data : datas) {
            String path = String.format(AUTH_INFO_PATH, data.getProjectId(), data.getProviderId(), data.getId());
            String content = JsonUtils.encode(data);
            obsService.putObject(path, content);
            redisClient.set(REDIS_PREFIX + data.getId(), content);
        }
        redisClient.set(MODEL_AUTH_UPDATE_TIME_KEY, Long.toString(time));
        log.info("Success sync {} data to obs.", datas.size());
    }

    public void createAuthInfo(ProviderAuthData authData) {
        authMapper.insert(authData);
        saveAuthInfoToObs(authData);
    }

    public void updateAuthInfo(ProviderAuthData authData) {
        authMapper.updateAuthData(authData.getId(), authData.getAuthType(), authData.getAuthInfo(),
            System.currentTimeMillis());
        saveAuthInfoToObs(authData);
    }

    public void deleteByProvider(String projectId, String workspaceId, String providerId) {
        List<ProviderAuthData> datas = authMapper.selectByProviderId(projectId, workspaceId, providerId);
        authMapper.clearByProviderId(projectId, workspaceId, providerId);
        for (ProviderAuthData data : datas) {
            String path = String.format(AUTH_INFO_PATH, projectId, providerId, data.getId());
            obsService.deleteObject(path);
            redisClient.delete(REDIS_PREFIX + data.getId());
        }
    }

    public void backupByProvider(String projectId, String workspaceId, String providerId) {
        List<ProviderAuthData> datas = authMapper.selectByProviderId(projectId, workspaceId, providerId);
        for (ProviderAuthData data : datas) {
            data.setLastUpdatedDate(System.currentTimeMillis());
            authMapper.insertBackup(data);
        }
    }

    public void deleteById(String projectId, String providerId, String id) {
        authMapper.clearById(id);
        String path = String.format(AUTH_INFO_PATH, projectId, providerId, id);
        obsService.deleteObject(path);
        redisClient.delete(REDIS_PREFIX + id);
    }

    public void backupById(String id) {
        ProviderAuthData providerAuthData = authMapper.selectById(id);
        providerAuthData.setLastUpdatedDate(System.currentTimeMillis());
        authMapper.insertBackup(providerAuthData);
    }

    private void saveAuthInfoToObs(ProviderAuthData authData) {
        String path = String.format(AUTH_INFO_PATH, authData.getProjectId(), authData.getProviderId(),
            authData.getId());
        String content = JsonUtils.encode(authData);
        obsService.putObject(path, content);
        redisClient.set(REDIS_PREFIX + authData.getId(), content);
    }

    public ProviderAuth getProviderAuth(String authType, String authInfo) {
        return switch (authType) {
            case "API_KEY" -> JsonUtils.decode(authInfo, ApiKeyAuthInfo.class);
            case "NO_AUTH" -> new NotAuthInfo();
            case "CUSTOM_APIKEY" -> new CustomApiKeyAuthInfo(authInfo);
            default -> {
                log.error("auth type={} is not support", authInfo);
                throw new AgentStudioException(StudioError.METHOD_ARGUMENT_NOT_VALID);
            }
        };
    }

    public ProviderAuthConfig transToAuthConfig(ProviderAuthMetadata metadata) {
        String authInfo = metadata.getAuthInfo();
        if (!StringUtils.isEmpty(metadata.getAuthId()) && !StringUtils.isEmpty(metadata.getAuthConfig())) {
            ProviderAuth auth = getProviderAuth(metadata.getAuthType(), metadata.getAuthConfig());
            authInfo = auth.convertToMaskStr(true);
        }

        return new ProviderAuthConfig().setAuthId(metadata.getAuthId())
            .setAuthType(metadata.getAuthType())
            .setIsRecord(metadata.getAuthId() != null && metadata.getAuthConfig() != null)
            .setMetadataId(metadata.getId())
            .setModelNames("")
            .setAuthUrl(metadata.getAuthUrl())
            .setProviderId(metadata.getProviderId())
            .setAuthInfo(authInfo);
    }

    public List<ProviderAuthMetadata> selectProviderMetadataAndPermissionCheck(String projectId, String workspaceId,
        String providerId, boolean containPlatform) {
        List<ProviderAuthMetadata> metadatas = metadataMapper.selectByProvider(providerId);
        if (metadatas.isEmpty()) {
            log.error("auth metadata is not exist. providerId:{}", providerId);
            throw new AgentStudioException(StudioError.MD_AUTH_METADATA_NOT_EXIST);
        }
        List<String> projects = containPlatform ? Arrays.asList("SYSTEM", projectId) : List.of(projectId);
        if (!projects.contains(metadatas.get(0).getProjectId())) {
            log.error("User not permission operate this provider. user project:{} metadataProject:{} metaId:{}",
                projectId, metadatas.get(0).getProjectId(), metadatas.get(0).getId());
            throw new AgentStudioException(StudioError.MD_USER_NOT_PERMISSION_OPERATE);
        }

        List<String> workspaces = containPlatform ? Arrays.asList("SYSTEM", workspaceId) : List.of(workspaceId);
        if (!workspaces.contains(metadatas.get(0).getWorkspaceId())) {
            log.error("User not permission operate this provider. user workspace:{} metadataWorkspace:{} metaId:{}",
                workspaceId, metadatas.get(0).getWorkspaceId(), metadatas.get(0).getId());
            throw new AgentStudioException(StudioError.MD_USER_NOT_PERMISSION_OPERATE);
        }
        return metadatas;
    }
}

