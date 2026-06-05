/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service;

import com.openjiuwen.studio.agent.agentbase.model.QueryResourceUsageResponse;
import com.openjiuwen.studio.agent.agentbase.service.KnowledgeBaseResourceManagementService;
import com.openjiuwen.studio.agent.common.constant.Constants;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.manager.dto.ResourceUsageDetails;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 工作流、单智能体、多智能体、知识库等资源额度检查
 */
@Slf4j
@Service
public class ResourceManagementService implements IResourceManagementService {
    @Autowired
    private SkuManageService skuManageService;

    @Autowired
    private KnowledgeBaseResourceManagementService knowledgeBaseResourceManagementService;

    @Override
    public ResourceUsageDetails resourceUsageDetails(String projectId, String resourceType) {
        if (StringUtils.isBlank(resourceType)) {
            ResourceUsageDetails resourceUsageDetails = new ResourceUsageDetails();
            resourceUsageDetails.setTotalAmount(0);
            resourceUsageDetails.setUsedLimit(0);
            return resourceUsageDetails;
        }
        return switch (resourceType) {
            case Constants.AppType.WORKFLOW, Constants.AppType.AGENT, Constants.AppType.CONTROLLER,
                 Constants.AppType.AGENT_NEW -> {
                List<Integer> skuUsageDetail = skuManageService.queryAppCountExceededLimit();
                ResourceUsageDetails resourceUsageDetails = new ResourceUsageDetails();
                resourceUsageDetails.setIsExceedLimit(skuUsageDetail.get(1) != -1 && skuUsageDetail.get(0) >= skuUsageDetail.get(1));
                resourceUsageDetails.setUsedLimit(skuUsageDetail.get(0));
                resourceUsageDetails.setTotalAmount(skuUsageDetail.get(1));
                resourceUsageDetails.setType(resourceType);
                yield resourceUsageDetails;
            }
            case Constants.KnowledgeBase.KNOWLEDGE_BASE,Constants.KnowledgeBase.KNOWLEDGE_BASE_DATESET_STORAGE,
                 Constants.KnowledgeBase.THIRD_PARTY_KNOWLEDGE_BASE, Constants.KnowledgeBase.THIRD_PARTY_KNOWLEDGE_BASE_CONNECTION ->
                this.queryResourceUsage(resourceType);
            default -> {
                ResourceUsageDetails resourceUsageDetails = new ResourceUsageDetails();
                resourceUsageDetails.setUsedLimit(0);
                resourceUsageDetails.setTotalAmount(0);
                resourceUsageDetails.setType(resourceType);
                yield resourceUsageDetails;
            }
        };
    }

    /**
     * 查询知识库资源额度使用情况
     *
     * @param resourceType 资源类型
     * @since 2026-01-21
     */
    public ResourceUsageDetails queryResourceUsage(String resourceType) {
        QueryResourceUsageResponse baseResourceUsageResponse
            = knowledgeBaseResourceManagementService.queryResourceUsage(RequestContextUtils.getRequestProjectId(),
            resourceType, null);
        ResourceUsageDetails resourceUsageDetails = new ResourceUsageDetails();
        if (baseResourceUsageResponse == null) {
            return resourceUsageDetails;
        }

        Long usedValue = baseResourceUsageResponse.getUsedValue();
        Long maxValue = baseResourceUsageResponse.getMaxValue();

        // 设置已使用量
        resourceUsageDetails.setUsedLimit(Math.toIntExact(usedValue));

        // 检查是否为无限制资源
        if (CommonConstant.UNLIMITED_RESOURCE_VALUE.equals(maxValue)) {
            // 资源无限制，设置totalAmount为-1，isExceedLimit为false
            resourceUsageDetails.setTotalAmount(-1);
            resourceUsageDetails.setIsExceedLimit(false);
        } else {
            // 资源有限制，按照原有逻辑处理
            resourceUsageDetails.setTotalAmount(Math.toIntExact(maxValue));
            resourceUsageDetails.setIsExceedLimit(resourceUsageDetails.getUsedLimit() >= resourceUsageDetails.getTotalAmount());
        }

        resourceUsageDetails.setType(resourceType);
        return resourceUsageDetails;
    }
}
