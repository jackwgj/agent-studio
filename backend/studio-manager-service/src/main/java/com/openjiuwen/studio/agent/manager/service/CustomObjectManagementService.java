/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.manager.dto.*;
import com.openjiuwen.studio.agent.manager.entity.CustomObjectEntity;
import com.openjiuwen.studio.agent.manager.mapper.CustomObjectMapper;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class CustomObjectManagementService implements ICustomObjectManagementService {

    @Autowired
    CustomObjectMapper customObjectMapper;

    @Value("${env.type}")
    private String envType;

    /**
     * 创建自定义对象
     * @param projectId projectId
     * @param workspaceId workspaceId
     * @param body body
     * @return ObjectBriefRsp 返回object_id
     */
    @Override
    public ObjectBriefRsp createCustomObject(String projectId, String workspaceId, ObjectInfoReq body) {
        CustomObjectEntity customObjectEntity = new CustomObjectEntity();
        customObjectEntity.setId(Optional.ofNullable(body.getId()).orElse(UUID.randomUUID().toString()));
        customObjectEntity.setProjectId(projectId);
        customObjectEntity.setWorkspaceId(workspaceId);
        customObjectEntity.setName(body.getName());
        customObjectEntity.setDescription(body.getDescription());
        customObjectEntity.setObjectSchema(body.getSchemas());
        customObjectEntity.setCreatorId(CommonConstant.EnvType.SIMPLE.equals(envType)
            ? RequestContextUtils.getRequestUserId()
            : RequestContextUtils.getRequestUserName());
        customObjectEntity.setCreatedOn(new Date());
        customObjectEntity.setUpdatedOn(new Date());

        // 检验对象名称是否重复
        checkObjectNameRepeat(body.getName(), projectId, workspaceId);
        log.info("Create custom object, {}", customObjectEntity);
        customObjectMapper.insert(customObjectEntity);
        return new ObjectBriefRsp().setId(customObjectEntity.getId());
    }

    /**
     * 检测名字是否重复
     * @param name 名字
     * @param projectId 项目id
     * @param workspaceId 工作空间id
     */
    private void checkObjectNameRepeat(String name, String projectId, String workspaceId) {
        if (StringUtils.isEmpty(name)) {
            return;
        }
        if (customObjectMapper.countByNameAndProjectIdAndWorkspaceId(name, projectId, workspaceId) > 0) {
            throw new AgentStudioException(StudioError.RESOURCE_OP_ERROR);
        }
    }

    /**
     * 删除自定义对象
     * @param projectId projectId
     * @param objectId objectId
     * @param workspaceId workspaceId
     * @return ObjectBriefRsp
     */
    @Override
    @Transactional
    public ObjectBriefRsp deleteCustomObject(String projectId, String objectId, String workspaceId) {
        log.info("Delete custom object, projectId: {}, objectId: {}, workspaceId: {}", projectId, objectId, workspaceId);
        customObjectMapper.deleteByIdAndProjectIdAndWorkspaceId(objectId, projectId,workspaceId);
        return new ObjectBriefRsp().setId(objectId);
    }

    /**
     * 自定义对象列表
     * @param projectId projectId
     * @param listCustomObjectQo listCustomObjectQo
     * @return ObjectListRsp 自定义对象列表
     */
    @Override
    public ObjectListRsp listCustomObject(String projectId, ListCustomObjectQo listCustomObjectQo) {
        Page<CustomObjectEntity> customObjectEntityPage;

        int offset = listCustomObjectQo.getOffset();
        int limit = listCustomObjectQo.getLimit();
        Pageable pageable = PageRequest.of(offset / limit, limit);

        if (StringUtils.isNotEmpty(listCustomObjectQo.getName())) {
            List<CustomObjectEntity> customObjectEntityList = customObjectMapper.findByProjectIdAndWorkspaceIdAndNameContaining(
                projectId, listCustomObjectQo.getWorkspaceId(), listCustomObjectQo.getName(), limit, offset);
            customObjectEntityPage = new PageImpl<>(customObjectEntityList, pageable, customObjectMapper.countByProjectIdAndWorkspaceIdAndNameContaining(
                projectId, listCustomObjectQo.getWorkspaceId(), listCustomObjectQo.getName()));
        } else {
            List<CustomObjectEntity> customObjectEntityList = customObjectMapper.findByProjectIdAndWorkspaceId(
                projectId, listCustomObjectQo.getWorkspaceId(), limit, offset);
            customObjectEntityPage = new PageImpl<>(customObjectEntityList, pageable, customObjectMapper.countByProjectIdAndWorkspaceIdAndNameContaining(
                projectId, listCustomObjectQo.getWorkspaceId(), null));
        }
        List<ObjectRsp> objectRspList = new ArrayList<>();
        for (CustomObjectEntity entity : customObjectEntityPage.getContent()) {
            objectRspList.add(new ObjectRsp()
                .setId(entity.getId())
                .setProjectId(entity.getProjectId())
                .setWorkspaceId(entity.getWorkspaceId())
                .setDescription(entity.getDescription())
                .setCreatorId(entity.getCreatorId())
                .setUpdateTime(entity.getUpdatedOn())
                .setSchemas(entity.getObjectSchema())
                .setName(entity.getName()));
        }
        return new ObjectListRsp().setCount(customObjectEntityPage.getTotalElements()).setObjects(objectRspList);
    }

    /**
     * 修改自定义对象
     * @param projectId projectId
     * @param objectId objectId
     * @param workspaceId workspaceId
     * @param body body
     * @return ObjectBriefRsp
     */
    @Override
    public ObjectBriefRsp modifyCustomObject(String projectId, String objectId, String workspaceId,
        ObjectInfoReq body) {
        CustomObjectEntity customObjectEntity = customObjectMapper.findByIdAndProjectIdAndWorkspaceId(objectId, projectId, workspaceId);
        if (customObjectEntity == null) {
            log.error("Custom object does not exist, projectId: {}, objectId: {}, workspaceId: {}", projectId, objectId, workspaceId);
            throw new AgentStudioException(StudioError.STATIC_RESOURCE_NOT_EXIST);
        }
        if (body.getName() != null && !body.getName().equals(customObjectEntity.getName())) {
            // 检验新设置的对象名称是否与已有的重复
            checkObjectNameRepeat(body.getName(), projectId, workspaceId);
            customObjectEntity.setName(body.getName());
        }
        //不更新（保留原值），此时传递null；用户传""，表示更新为空字符串
        if (body.getDescription() != null) {
            customObjectEntity.setDescription(body.getDescription());
        }
        if (body.getSchemas() != null) {
            customObjectEntity.setObjectSchema(body.getSchemas());
        }
        customObjectEntity.setUpdatedOn(new Date());
        log.info("Modify custom object, {}", customObjectEntity);
        customObjectMapper.updateById(customObjectEntity);
        return new ObjectBriefRsp().setId(objectId);
    }

    /**
     * 检索自定义对象
     * @param projectId projectId
     * @param objectId objectId
     * @param workspaceId workspaceId
     * @return ObjectRsp
     */
    @Override
    public ObjectRsp retrieveCustomObject(String projectId, String objectId, String workspaceId) {
        CustomObjectEntity customObjectEntity = customObjectMapper.findByIdAndProjectIdAndWorkspaceId(objectId, projectId, workspaceId);

        // 自定义对象不存在
        if (Objects.isNull(customObjectEntity)) {
            throw new AgentStudioException(StudioError.STATIC_RESOURCE_NOT_EXIST);
        }
        return new ObjectRsp().setId(customObjectEntity.getId())
            .setProjectId(customObjectEntity.getProjectId())
            .setWorkspaceId(customObjectEntity.getWorkspaceId())
            .setSchemas(customObjectEntity.getObjectSchema())
            .setDescription(customObjectEntity.getDescription())
            .setCreatorId(customObjectEntity.getCreatorId())
            .setUpdateTime(customObjectEntity.getUpdatedOn())
            .setName(customObjectEntity.getName());
    }
}
