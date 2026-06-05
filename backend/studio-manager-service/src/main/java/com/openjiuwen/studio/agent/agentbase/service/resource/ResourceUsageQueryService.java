/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service.resource;

import com.openjiuwen.studio.agent.agentbase.enums.ResourceType;

/**
 * 资源使用查询器
 */
public interface ResourceUsageQueryService {

    ResourceType resourceType();

    Long queryResourceUsageCount(String domainId);

    Long queryResourceUsageCountInKnowledgeBase(String domainId, String knowledgeBaseId);

}
