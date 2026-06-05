/* * Copyright (c) Huawei Technologies Co., Ltd. 2021-2025. All rights reserved. */

package com.openjiuwen.studio.agent.manager.service.workspace;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.UuidUtils;
import com.openjiuwen.studio.agent.manager.dto.Extension;
import com.openjiuwen.studio.agent.manager.dto.ExternalMappingInfo;
import com.openjiuwen.studio.agent.manager.dto.SecretLevel;
import com.openjiuwen.studio.agent.manager.entity.workspace.WorkspaceMappingEntity;
import com.openjiuwen.studio.agent.manager.mapper.workspace.WorkspaceMappingMapper;
import com.openjiuwen.studio.agent.common.utils.CryptoUtils;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 添加类的描述
 *
 */
@Service
@Slf4j
public class WorkspaceMappingService {

    @Autowired
    WorkspaceMappingMapper workspaceMappingMapper;

    public List<ExternalMappingInfo> queryWorkspaceMappingInfoByWorkspaceId(String workspaceId) {
        List<WorkspaceMappingEntity> workspaceMappingEntityList
            = workspaceMappingMapper.selectWorkspaceMappingEntityByWorkspaceId(workspaceId);

        if (CollectionUtils.isEmpty(workspaceMappingEntityList)) {
            return Collections.emptyList();
        }

        return workspaceMappingEntityList.stream().map(mappingEntity -> {
            ExternalMappingInfo externalMappingInfo = new ExternalMappingInfo();
            BeanUtils.copyProperties(mappingEntity, externalMappingInfo);
            return externalMappingInfo;
        }).collect(Collectors.toList());
    }

    public List<Extension> queryMappingWorkspaceExtensionInfo(String workspaceId, String source) {
        WorkspaceMappingEntity workspaceMappingEntity
            = workspaceMappingMapper.selectWorkspaceMappingEntityByWorkspaceIdAndSource(workspaceId, source);

        if (workspaceMappingEntity == null) {
            return Collections.emptyList();
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(workspaceMappingEntity.getExtensionContent(), new TypeReference<List<Extension>>() {});
        } catch (Exception e) {
            log.error("Failed to deserialize externalExtension {}", e.getMessage());
            throw new AgentStudioException(StudioError.INVALID_EXTENSION_PARAM);
        }
    }

    public void associateWorkspaceRelationWithExternalSystem(String workspaceId,
        ExternalMappingInfo externalMappingInfo) {
        if (externalMappingInfo == null) {
            return;
        }

        if (!workspaceId.equals(externalMappingInfo.getWorkspaceId())) {
            log.error("the workspaceId {} is not same with request workspaceId {}", workspaceId, externalMappingInfo.getWorkspaceId());
            throw new AgentStudioException(StudioError.INVALID_PARAMETER_ERROR);
        }

        WorkspaceMappingEntity existedMappingEntity
            = workspaceMappingMapper.selectWorkspaceMappingEntityByWorkspaceIdAndSource(workspaceId, externalMappingInfo.getSource());

        if (existedMappingEntity == null) {
            WorkspaceMappingEntity workspaceMappingEntity = new WorkspaceMappingEntity().setId(UuidUtils.getUUID())
                .setWorkspaceId(workspaceId)
                .setMappingId(externalMappingInfo.getMappingId())
                .setSource(externalMappingInfo.getSource())
                .setExtensionContent(encryptedExtension2String(externalMappingInfo.getExtensionContent()));

            workspaceMappingMapper.insert(workspaceMappingEntity);
            return;
        }

        existedMappingEntity.setExtensionContent(encryptedExtension2String(externalMappingInfo.getExtensionContent()));
        existedMappingEntity.setMappingId(externalMappingInfo.getMappingId());

        workspaceMappingMapper.updateByPrimaryKey(existedMappingEntity);
    }


    public String encryptedExtension2String(List<Extension> externalExtensionList) {
        try {
            List<Extension> processed = new ArrayList<>();
            for (Extension ext : externalExtensionList) {
                Extension copy = new Extension();
                copy.setKey(ext.getKey());
                copy.setSecretLevel(ext.getSecretLevel());

                if (SecretLevel.ENCRYPTION.toString().equalsIgnoreCase(ext.getSecretLevel().toString())) {
                    copy.setValue(CryptoUtils.encrypt(ext.getValue()));
                } else {
                    copy.setValue(ext.getValue());
                }
                processed.add(copy);
            }

            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(processed);

        } catch (Exception e) {
            log.error("Failed to serialize externalExtension {}", e.getMessage());
            throw new AgentStudioException(StudioError.INVALID_EXTENSION_PARAM);
        }
    }
}
