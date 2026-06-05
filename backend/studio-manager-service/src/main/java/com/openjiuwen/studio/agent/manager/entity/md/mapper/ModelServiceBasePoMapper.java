/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.entity.md.mapper;

import com.openjiuwen.studio.agent.manager.dto.ModelServiceRsp;
import com.openjiuwen.studio.agent.manager.entity.md.ModelServiceBase;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * ModelServiceBasePoMapper
 *
 */
@Mapper
public interface ModelServiceBasePoMapper {
    /**
     * INSTANCE
     */
    ModelServiceBasePoMapper INSTANCE = Mappers.getMapper(ModelServiceBasePoMapper.class);

    /**
     * toModelServiceRsp
     *
     * @param modelServiceBase modelServiceBase
     * @return ModelServiceRsp
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "providerId", source = "providerId")
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "serviceName", source = "serviceName")
    @Mapping(target = "serviceKey", source = "serviceKey")
    @Mapping(target = "modelName", source = "modelName")
    @Mapping(target = "logo", source = "logo")
    @Mapping(target = "providerName", ignore = true)
    @Mapping(target = "providerNameEn", ignore = true)
    @Mapping(target = "isReasoning", source = "isReasoning")
    @Mapping(target = "isSupportCloseReasoning", source = "isSupportCloseReasoning")
    @Mapping(target = "isNetwork", source = "isNetwork")
    @Mapping(target = "isPublic", source = "public")
    @Mapping(target = "modelType", source = "modelType")
    @Mapping(target = "modelTags", source = "modelTags")
    @Mapping(target = "modelDescription", source = "modelDescription")
    @Mapping(target = "modelDescriptionEn", source = "modelDescriptionEn")
    @Mapping(target = "modelDeployType", source = "modelDeployType")
    @Mapping(target = "domainId", source = "domainId")
    @Mapping(target = "projectId", source = "projectId")
    @Mapping(target = "workspaceId", source = "workspaceId")
    @Mapping(target = "createdByUser", source = "createdByUser")
    @Mapping(target = "createdDate", source = "createdDate")
    @Mapping(target = "lastUpdatedDate", source = "lastUpdatedDate")
    @Mapping(target = "apiUrl", source = "apiUrl")
    @Mapping(target = "isSupportFunction", source = "isSupportFunction")
    @Mapping(target = "interfaceProtocol", source = "interfaceProtocol")
    @Mapping(target = "authMetadataId", source = "authMetadataId")
    @Mapping(target = "authId", ignore = true)
    @Mapping(target = "publishStatus", source = "publishStatus")
    @Mapping(target = "isSupportStream", source = "isSupportStream")
    @Mapping(target = "throttlingPolicy", source = "throttlingPolicy")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "disclaimer", source = "disclaimer")
    @Mapping(target = "isSubscribed", ignore = true)
    @Mapping(target = "contextLength", source = "contextLength")
    @Mapping(target = "modelSeries", ignore = true)
    @Mapping(target = "isInFreeTrial", ignore = true)
    @Mapping(target = "templateId", ignore = true)
    @Mapping(target = "isSupportDeepThinking", ignore = true)
    @Mapping(target = "isPreparingForOffline", ignore = true)
    @Mapping(target = "residentModelId", ignore = true)
    @Mapping(target = "isAuthConfigured", ignore = true)
    ModelServiceRsp toModelServiceRsp(ModelServiceBase modelServiceBase);
}
