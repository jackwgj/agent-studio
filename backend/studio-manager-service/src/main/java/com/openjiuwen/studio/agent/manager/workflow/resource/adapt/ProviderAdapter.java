/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.workflow.resource.adapt;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.manager.entity.ProviderExportMetadata;
import com.openjiuwen.studio.agent.manager.entity.md.ModelServiceProvider;
import com.openjiuwen.studio.agent.manager.mapper.md.SysModelServiceProviderMapper;
import com.openjiuwen.studio.agent.manager.mapper.md.UserModelServiceProviderMapper;
import com.openjiuwen.studio.agent.manager.service.md.ProviderMgmtService;
import com.openjiuwen.studio.agent.manager.utils.JsonUtils;
import com.openjiuwen.studio.agent.manager.workflow.resource.model.ImportCheckResult;
import com.openjiuwen.studio.agent.manager.workflow.resource.model.ImportInfo;
import com.openjiuwen.studio.agent.manager.workflow.resource.model.ImportResourceResult;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

@Component("PROVIDER")
@Slf4j
public class ProviderAdapter extends ResourceAdapter {

    @Autowired
    private ProviderMgmtService providerMgmtService;

    @Autowired
    private SysModelServiceProviderMapper sysModelServiceProviderMapper;

    @Autowired
    private UserModelServiceProviderMapper userModelServiceProviderMapper;


    @Override
    public void checkBeforeImport(ImportCheckResult importCheckResult, ImportInfo importInfo) {
        checkMetadata(importInfo);
        ProviderExportMetadata providerMetadata = JsonUtils.objectToClassType(importInfo.getMetadata(),
            ProviderExportMetadata.class);
        importCheckResult.setDescription(providerMetadata.getModelServiceProviderMetadata().getDescription());
        // 预制资源不可导入
        if (isPlatformProvider(providerMetadata.getModelServiceProviderMetadata().getId())) {
            importCheckResult.setProhibited(true);
            importCheckResult.setStatus(true);
        }

        ModelServiceProvider provider = getProviderByTraceId(importInfo.getTargetProjectId(),
            importInfo.getTargetWorkspaceId(), providerMetadata.getModelServiceProviderMetadata().getIdentityId());
        importCheckResult.setStatus(provider != null);
        importCheckResult.setImportDesc(getImportDescNoUpdate(provider));
    }

    private void checkMetadata(ImportInfo importInfo) {
        if (importInfo.getMetadata() == null) {
            throw new AgentStudioException(StudioError.FILE_LACKS_SOURCE_DATA);
        }
    }

    private boolean isPlatformProvider(String id) {
        ModelServiceProvider sysProviders = sysModelServiceProviderMapper.selectById(id);
        return sysProviders != null;
    }

    private ModelServiceProvider getProviderByTraceId(String projectId, String workspaceId, String traceId) {
        List<ModelServiceProvider> workflowEntities = userModelServiceProviderMapper.selectByProjectId(projectId);
        return workflowEntities.stream()
            .filter(v -> Strings.CS.equals(v.getWorkspaceId(), workspaceId) && Strings.CS.equals(v.getIdentityId(),
                traceId))
            .findFirst()
            .orElse(null);
    }

    @Override
    public void parseImport(ImportResourceResult importResourceResult, ImportInfo importInfo) {
        checkMetadata(importInfo);
        ProviderExportMetadata provider = JsonUtils.objectToClassType(importInfo.getMetadata(),
            ProviderExportMetadata.class);
        // 初始化返回结果
        setImportResultDesc(importResourceResult, null, provider.getModelServiceProviderMetadata().getDescription());

        // 预置资源不允许导入
        if (isPlatformProvider(provider.getModelServiceProviderMetadata().getId())) {
            handlePlatformResource(importResourceResult);
        } else {
            // 处理自定义资源
            handleWidgetsProvider(importInfo, provider, importResourceResult);
        }
    }

    private void handleWidgetsProvider(ImportInfo importInfo, ProviderExportMetadata provider,
        ImportResourceResult result) {
        ModelServiceProvider metadata = provider.getModelServiceProviderMetadata();
        setProviderMetadata(provider, importInfo, metadata);

        ModelServiceProvider existingProvider = findExistingProvider(importInfo, metadata);

        if (existingProvider == null) {
            setProviderName(importInfo.getTargetProjectId(), importInfo.getTargetWorkspaceId(), metadata);
            result.setNewName(metadata.getProviderName());
            handleNewProvider(importInfo, provider, result);
        } else {
            if (!Strings.CS.equals(existingProvider.getId(), provider.getModelServiceProviderMetadata().getId())) {
                setProviderIds(provider, existingProvider.getId());
                result.setNewId(existingProvider.getId());
            }
        }
    }

    private void setProviderMetadata(ProviderExportMetadata provider, ImportInfo importInfo,
        ModelServiceProvider metadata) {
        String projectId = importInfo.getTargetProjectId();
        String workspaceId = importInfo.getTargetWorkspaceId();
        String domainId = importInfo.getTargetDomainId();
        String createBy = importInfo.getCreator();
        Long createdDate = new Date().getTime();

        metadata.setProjectId(projectId);
        metadata.setWorkspaceId(workspaceId);
        metadata.setDomainId(domainId);
        metadata.setCreatedByUser(createBy);
        metadata.setCreatedDate(createdDate);
        metadata.setLastUpdatedByUser(createBy);
        metadata.setLastUpdatedDate(createdDate);

        provider.getProviderAuthData().setProjectId(projectId);
        provider.getProviderAuthData().setWorkspaceId(workspaceId);
        provider.getProviderAuthData().setDomainId(domainId);
        provider.getProviderAuthData().setCreatedByUser(createBy);
        provider.getProviderAuthData().setCreatedDate(createdDate);
        provider.getProviderAuthData().setLastUpdatedDate(createdDate);

        provider.getProviderAuthMetadata().setProjectId(projectId);
        provider.getProviderAuthMetadata().setWorkspaceId(workspaceId);
        provider.getProviderAuthMetadata().setDomainId(domainId);
        provider.getProviderAuthMetadata().setCreatedByUser(createBy);
        provider.getProviderAuthMetadata().setCreatedDate(createdDate);
        provider.getProviderAuthMetadata().setLastUpdatedDate(createdDate);

    }

    private ModelServiceProvider findExistingProvider(ImportInfo importInfo, ModelServiceProvider metadata) {
        List<ModelServiceProvider> existingProviders = userModelServiceProviderMapper.selectByProjectId(
            importInfo.getTargetProjectId());
        return existingProviders.stream()
            .filter(v -> Strings.CS.equals(v.getWorkspaceId(), importInfo.getTargetWorkspaceId()) && Strings.CS.equals(
                v.getIdentityId(), metadata.getIdentityId()))
            .findFirst()
            .orElse(null);
    }

    private void handleNewProvider(ImportInfo importInfo, ProviderExportMetadata provider,
        ImportResourceResult result) {
        try {
            if (!Strings.CS.equals(provider.getProviderAuthMetadata().getAuthType(), "NO_AUTH")) {
                provider.getProviderAuthMetadata().setAuthType("NO_AUTH");
                provider.getProviderAuthData().setAuthType("NO_AUTH");
                result.setConfigDisplay(true);
            }
            // id已存在,需要更换id
            if (userModelServiceProviderMapper.selectById(provider.getModelServiceProviderMetadata().getId()) != null) {
                String newId = UUID.randomUUID().toString();
                setProviderIds(provider, newId);
                result.setNewId(newId);
            }
            providerMgmtService.createProviderFromMetadata(importInfo.getTargetProjectId(),
                importInfo.getTargetWorkspaceId(), provider);
        } catch (AgentStudioException e) {
            handleException(result, e);
        }
    }

    private void setProviderIds(ProviderExportMetadata provider, String id) {
        provider.getModelServiceProviderMetadata().setId(id);
        provider.getProviderAuthMetadata().setProviderId(id);
        provider.getProviderAuthMetadata().setId(UUID.randomUUID().toString());
        provider.getProviderAuthData().setProviderId(id);
        provider.getProviderAuthData().setId(UUID.randomUUID().toString());
        provider.getProviderAuthData().setAuthMetadataId(provider.getProviderAuthMetadata().getId());
    }

    private void setProviderName(String projectId, String workspaceId, ModelServiceProvider metadata) {
        String name = Optional.ofNullable(metadata.getProviderName()).orElse(metadata.getProviderNameEn());
        List<ModelServiceProvider> providers = userModelServiceProviderMapper.selectByProjectId(projectId);
        List<ModelServiceProvider> providersByWorkspaceId = providers.stream()
            .filter(v -> Strings.CS.equals(v.getWorkspaceId(), workspaceId))
            .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(providersByWorkspaceId)) {
            return;
        }
        Set<String> currentNameSet = providersByWorkspaceId.stream()
            .map(ModelServiceProvider::getProviderName)
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(() -> new TreeSet<>(String.CASE_INSENSITIVE_ORDER)));

        List<ModelServiceProvider> sameNameList = providersByWorkspaceId.stream()
            .filter(v -> Strings.CS.equals(v.getProviderName(), name))
            .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(sameNameList)) {
            return;
        }
        // 用户在当前环境中已存在该MCP服务，且name未改变，不会冲突
        if (sameNameList.stream().filter(v -> Strings.CS.equals(v.getIdentityId(), metadata.getIdentityId())).count()
            > 0) {
            return;
        }

        // 设置唯一供应商名称
        metadata.setProviderName(getFormatName(name, currentNameSet));
    }

}
