/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service.resource;

import com.google.common.collect.ImmutableMap;
import com.openjiuwen.studio.agent.agentbase.enums.ResourceType;
import com.openjiuwen.studio.agent.foundation.base.exception.AgentBaseException;
import com.openjiuwen.studio.agent.foundation.base.exception.ErrorCode;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ResourceUsageFactory implements InitializingBean {

    private final List<ResourceUsageQueryService> resourceUsageQueryServices;

    private Map<ResourceType, ResourceUsageQueryService> resourceUsageQueryServiceMap;

    public ResourceUsageFactory(List<ResourceUsageQueryService> resourceUsageQueryServices) {
        this.resourceUsageQueryServices = resourceUsageQueryServices;
    }

    @Override
    public void afterPropertiesSet() {
        if (CollectionUtils.isEmpty(resourceUsageQueryServices)) {
            this.resourceUsageQueryServiceMap = ImmutableMap.of();
        } else {
            this.resourceUsageQueryServiceMap = resourceUsageQueryServices.stream()
                .collect(Collectors.toMap(ResourceUsageQueryService::resourceType, Function.identity()));
        }
    }

    public ResourceUsageQueryService chooseResourceUsageQueryServiceMap(ResourceType type) {
        final ResourceUsageQueryService executor = resourceUsageQueryServiceMap.get(type);
        if (executor == null) {
            log.error("choose resource usage query service error");
            throw new AgentBaseException(ErrorCode.INVALID_PARAMETER, type.name());
        }
        return executor;
    }
}
