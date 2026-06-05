/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.workflow.resource.adapt;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class ResourceAdapterFactory {

    @Autowired
    private Map<String, ResourceAdapter> resourceAdapters;

    public ResourceAdapter getAdapter(String importResourceType) {
        ResourceAdapter resourceAdapter = resourceAdapters.get(StringUtils.upperCase(importResourceType));
        if (resourceAdapter != null) {
            return resourceAdapter;
        }
        log.error("adapt not exists resourceType:{}", importResourceType);
        throw new AgentStudioException(StudioError.SYSTEM_ERROR_ADD_DATASET);
    }

}
