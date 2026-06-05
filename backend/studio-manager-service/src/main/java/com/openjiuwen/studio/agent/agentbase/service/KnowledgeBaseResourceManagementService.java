/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.agentbase.service;

import static com.openjiuwen.studio.common.service.constant.CommonConstant.TenantType.TENANT_TYPE_2B;
import static com.openjiuwen.studio.common.service.constant.CommonConstant.TenantType.TENANT_TYPE_2C;

import com.openjiuwen.studio.agent.agentbase.entity.KnowledgeBaseEntity;
import com.openjiuwen.studio.agent.agentbase.enums.ResourceType;
import com.openjiuwen.studio.agent.agentbase.mapper.KnowledgeBaseMapper;
import com.openjiuwen.studio.agent.agentbase.model.QueryResourceUsageResponse;
import com.openjiuwen.studio.agent.agentbase.service.resource.ResourceUsageFactory;
import com.openjiuwen.studio.agent.agentbase.service.validate.ResourceValidateService;
import com.openjiuwen.studio.agent.agentbase.utils.TenantTypeUtil;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.foundation.base.exception.AgentBaseException;
import com.openjiuwen.studio.agent.foundation.base.exception.ErrorCode;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
public class KnowledgeBaseResourceManagementService {

    private final ResourceUsageFactory resourceUsageFactory;

    private final ResourceValidateService resourceValidateService;

    private final KnowledgeBaseMapper knowledgeBaseMapper;

    @Value("${enterprise_knowledge_base_max_storage:-1}")
    private Long enterpriseKnowledgeBaseLimit;

    @Value("${env.type}")
    private String envType;

    public KnowledgeBaseResourceManagementService(ResourceUsageFactory resourceUsageFactory,
        ResourceValidateService resourceValidateService, KnowledgeBaseMapper knowledgeBaseMapper) {
        this.resourceUsageFactory = resourceUsageFactory;
        this.resourceValidateService = resourceValidateService;
        this.knowledgeBaseMapper = knowledgeBaseMapper;
    }

    public QueryResourceUsageResponse queryResourceUsage(String projectId, String resourceType,
        String knowledgeBaseId) {
        ResourceType type = ResourceType.fromValue(resourceType);
        String domainId = RequestContextUtils.getRequestUserDomainId();
        Long maxValue = ResourceType.ENV_TYPE_QUERY_RESOURCE_TYPE_MAP.get(envType).contains(type) ? queryMaxValue(
            projectId, type, knowledgeBaseId) : -1L;
        Long useValue = isInKnowledgeBase(projectId, knowledgeBaseId, type)
            ? resourceUsageFactory.chooseResourceUsageQueryServiceMap(type)
            .queryResourceUsageCountInKnowledgeBase(domainId, knowledgeBaseId)
            : resourceUsageFactory.chooseResourceUsageQueryServiceMap(type).queryResourceUsageCount(domainId);
        return new QueryResourceUsageResponse().setMaxValue(maxValue).setUsedValue(useValue);
    }

    private boolean isInKnowledgeBase(String projectId, String knowledgeBaseId, ResourceType type) {
        if (!type.isInKnowledgeBase()) {
            return false;
        }
        // 当资源为知识库内时,knowledgeBaseId不能为空
        validateKnowledgeBaseId(projectId, type, knowledgeBaseId);
        return true;
    }

    private Long queryMaxValue(String projectId, ResourceType type, String knowledgeBaseId) {
        switch (type) {
            case KNOWLEDGE_BASE_DATESET_STORAGE: {
                final KnowledgeBaseEntity knowledgeBaseEntity = validateKnowledgeBaseId(projectId, type,
                    knowledgeBaseId);
                String tenantType = TenantTypeUtil.getTenantType();
                if (TENANT_TYPE_2C.equals(tenantType) || (TENANT_TYPE_2B.equals(tenantType)
                    && knowledgeBaseEntity.getKnowledgeBaseTenantType().equals(TENANT_TYPE_2C))) {
                    // ToC租户或者ToB租户在公共的KooSearch上开通的知识库中上传的容量由license/数据库控制
                    return resourceValidateService.queryQuotaLimit(type);
                } else {
                    // 企业版租户在新购买的KooSearch上开通的知识库中上传的容量不限制
                    return enterpriseKnowledgeBaseLimit;
                }
            }
            case KNOWLEDGE_BASE: {
                return TENANT_TYPE_2B.equals(TenantTypeUtil.getTenantType())
                    ? enterpriseKnowledgeBaseLimit
                    : resourceValidateService.queryQuotaLimit(type);
            }
            default:
                return resourceValidateService.queryQuotaLimit(type);
        }
    }

    private KnowledgeBaseEntity validateKnowledgeBaseId(String projectId, ResourceType type, String knowledgeBaseId) {
        if (StringUtils.isBlank(knowledgeBaseId)) {
            log.error("The knowledgeBaseId cannot be empty when the resourceType is {}", type);
            throw new AgentBaseException(ErrorCode.INVALID_PARAMETER, "knowledge_base_id");
        }
        final KnowledgeBaseEntity knowledgeBaseEntity = knowledgeBaseMapper.selectById(projectId, knowledgeBaseId);
        if (Objects.isNull(knowledgeBaseEntity)) {
            log.error("knowledgeBase {} can not be empty", knowledgeBaseId);
            throw new AgentBaseException(ErrorCode.KNOWLEDGE_BASE_NOT_EXIST, knowledgeBaseId);
        }
        return knowledgeBaseEntity;
    }
}
