/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service.md;

import com.openjiuwen.studio.agent.common.dto.md.ProviderAuth;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.JsonUtils;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.dto.AuthConfigListQo;
import com.openjiuwen.studio.agent.manager.dto.CreateAuthConfigReq;
import com.openjiuwen.studio.agent.manager.dto.ProviderAuthCfgList;
import com.openjiuwen.studio.agent.manager.dto.ProviderAuthConfig;
import com.openjiuwen.studio.agent.manager.entity.md.ProviderAuthData;
import com.openjiuwen.studio.agent.manager.entity.md.ProviderAuthMetadata;
import com.openjiuwen.studio.agent.manager.mapper.md.ProviderAuthDataMapper;
import com.openjiuwen.studio.agent.manager.mapper.md.ProviderAuthMetadataMapper;
import com.openjiuwen.studio.agent.manager.service.IProviderAuthMgmtService;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 模型服务鉴权管理
 */
@Slf4j
@Service
public class ProviderAuthMgmtService implements IProviderAuthMgmtService {
    @Autowired
    private ProviderAuthDataMapper authDataMapper;

    @Autowired
    private ProviderAuthService authService;

    @Autowired
    private ProviderAuthMetadataMapper metadataMapper;

    @Autowired
    private ModelServiceManager serviceManager;

    @Value("${model.soft.delete.enable:false}")
    private boolean modelSoftDelete;

    @Override
    public ProviderAuthCfgList authConfigList(String projectId, AuthConfigListQo param) {
        String workspaceId = param.getWorkspaceId();
        if (StringUtils.isAllEmpty(param.getProviderId(), param.getMetadataId())) {
            log.error("ProviderId and metadataId cannot be all empty.");
            throw new AgentStudioException(StudioError.MD_REQUEST_PARAM_INVALID);
        }
        List<ProviderAuthMetadata> authDatas = authDataMapper.selectAuthDataByProviderOrMetaId(projectId, workspaceId,
            param.getProviderId(), param.getMetadataId());
        List<ProviderAuthConfig> cfgs = new ArrayList<>(1);

        if (!authDatas.isEmpty()) {
            cfgs.add(authService.transToAuthConfig(authDatas.get(0)));
        }

        return new ProviderAuthCfgList().setTotal(cfgs.size()).setData(cfgs);
    }

    @Override
    public Void createAuthConfig(String projectId, String workspaceId, Boolean availableCheck,
        CreateAuthConfigReq body) {
        ProviderAuthMetadata metadata = metadataMapper.selectById(body.getMetadataId());
        if (metadata == null) {
            log.error("Metadata is not exist. id:{}", body.getMetadataId());
            throw new AgentStudioException(StudioError.MD_AUTH_METADATA_MODEL_NOT_EXIST, body.getMetadataId());
        }

        if (!projectId.equals(metadata.getProjectId()) && !"SYSTEM".equals(metadata.getProjectId())) {
            log.error("User not permission operate this auth metadata. user project:{} metadataProject:{} metaId:{}",
                projectId, metadata.getProjectId(), metadata.getId());
            throw new AgentStudioException(StudioError.MD_AUTH_METADATA_PERMISSION_NOT_EXIST);
        }

        if (!workspaceId.equals(metadata.getWorkspaceId()) && !"SYSTEM".equals(metadata.getWorkspaceId())) {
            log.error(
                "User not permission operate this auth metadata. user workspace:{} metadataWorkspace:{} metaId:{}",
                workspaceId, metadata.getWorkspaceId(), metadata.getId());
            throw new AgentStudioException(StudioError.MD_AUTH_METADATA_PERMISSION_NOT_EXIST);
        }

        ProviderAuth authCfg = authService.getProviderAuth(metadata.getAuthType(),
            JsonUtils.encode(body.getAuthInfo()));
        authCfg.validCheck();

        if (Boolean.TRUE.equals(availableCheck)) {
            log.info("Not need update provider auth info. provider:{}", metadata.getProviderId());
            serviceManager.checkProviderAuth(projectId, workspaceId, metadata.getAuthType(), body.getAuthInfo(),
                metadata.getProviderId(), "SYSTEM".equals(metadata.getWorkspaceId()));
        }

        ProviderAuthData auth = authDataMapper.selectByMetaId(projectId, workspaceId, body.getMetadataId());
        if (auth == null) {
            String authId = UUID.randomUUID().toString();
            auth = new ProviderAuthData().setAuthMetadataId(body.getMetadataId())
                .setProviderId(metadata.getProviderId())
                .setAuthInfo(authCfg.convertToEncryptStr())
                .setCreatedByUser(RequestContextUtils.getRequestUserName())
                .setDomainId(RequestContextUtils.getRequestUserDomainId())
                .setProjectId(projectId)
                .setWorkspaceId(workspaceId)
                .setCreatedDate(System.currentTimeMillis())
                .setLastUpdatedDate(System.currentTimeMillis())
                .setAuthType(metadata.getAuthType())
                .setId(authId)
                .setIdentityId(authId);
            authService.createAuthInfo(auth);
        } else {
            ProviderAuthData config = authDataMapper.selectById(auth.getId());
            config.setAuthType(metadata.getAuthType());
            config.setAuthInfo(authCfg.convertToEncryptStr());

            authService.updateAuthInfo(config);
        }

        return null;
    }

    @Override
    public Void removeAuthConfig(String projectId, String workspaceId, String id) {
        ProviderAuthData auth = authDataMapper.selectById(id);
        if (auth == null) {
            log.error("auth data is not exist. id:{}", id);
            throw new AgentStudioException(StudioError.MD_AUTH_DATA_NOT_EXIST);
        }
        if (!projectId.equals(auth.getProjectId())) {
            log.error("user no permission delete auth data. id:{} userProject:{} dataProject:{}", id, projectId,
                auth.getProjectId());
            throw new AgentStudioException(StudioError.MD_USER_NO_PERMISSION_DELETE);
        }

        if ("IAM".equals(auth.getAuthType()) || "NO_AUTH".equals(auth.getAuthType())) {
            log.error("IAM or NO_AUTH auth type is not need delete");
            throw new AgentStudioException(StudioError.MD_PROVIDER_AUTH_NOT_SUPPORT_DELETE);
        }

        if (!workspaceId.equals(auth.getWorkspaceId())) {
            log.error("user no permission delete auth data. id:{} userWorkspaceId:{} dataWorkspaceId:{}", id,
                workspaceId, auth.getWorkspaceId());
            throw new AgentStudioException(StudioError.MD_USER_NO_PERMISSION_DELETE);
        }

        if (modelSoftDelete) {
            authService.backupById(id);
        }

        authService.deleteById(projectId, auth.getProviderId(), id);
        return null;
    }

    @Override
    public Void removeProviderAuthCfg(String projectId, String workspaceId, String providerId, String authId) {
        if (StringUtils.isAllEmpty(providerId, authId)) {
            log.error("ProviderId and authId cannot be all empty.");
            throw new AgentStudioException(StudioError.MD_REQUEST_PARAM_AUTH_INVALID);
        }
        if (StringUtils.isEmpty(providerId)) {
            removeAuthConfig(projectId, workspaceId, authId);
        } else {
            ProviderAuthMetadata metadata = authService.selectProviderMetadataAndPermissionCheck(projectId, workspaceId,
                providerId, true).get(0);
            if ("IAM".equals(metadata.getAuthType()) || "NO_AUTH".equals(metadata.getAuthType())) {
                log.error("provider IAM or NO_AUTH auth type is not need delete");
                throw new AgentStudioException(StudioError.MD_PROVIDER_AUTH_NOT_SUPPORT_DELETE);
            }
            authService.deleteByProvider(projectId, workspaceId, providerId);
        }
        return null;
    }
}
