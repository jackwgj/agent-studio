/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.service;

import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.prompt.engineering.entity.ShareResourcePromptEntity;
import com.openjiuwen.studio.prompt.engineering.mapper.ShareResourcePromptMapper;
import com.openjiuwen.studio.prompt.engineering.mapper.ShareScopePromptMapper;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ShareInnerPromptService {
    @Autowired
    private ShareScopePromptMapper shareScopePromptMapper;

    @Autowired
    private ShareResourcePromptMapper shareResourcePromptMapper;

    public boolean cancelPromptShared(String templateId, String projectId, String workspaceId) {
        ShareResourcePromptEntity shareResource = shareResourcePromptMapper.selectShareResourceEntityById(templateId, projectId, workspaceId);
        if (ObjectUtils.isNotEmpty(shareResource)) {
            log.error("The Prompt {} has a shared record and cannot be deleted directly.", templateId);
            throw new AgentStudioException(StudioError.SHARE_RESOURCE_CANNOT_BE_DELETE_DIRECTLY);
        }
        return true;
    }
}
