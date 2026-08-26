/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service;

import com.openjiuwen.studio.agent.manager.dto.GetStudioSkillDetailsResponseBody;
import com.openjiuwen.studio.agent.manager.dto.ListStudioSkillsQo;
import com.openjiuwen.studio.agent.manager.dto.ListStudioSkillsResponseBody;

/**
 * SIE Skill management service.
 */
public interface ISIESkillManagementService {

    /**
     * Query SIE skills.
     *
     * @param projectId project ID
     * @param listStudioSkillsQo query conditions
     * @return skill list
     */
    ListStudioSkillsResponseBody listStudioSkills(String projectId, ListStudioSkillsQo listStudioSkillsQo);

    /**
     * Query SIE skill details.
     *
     * @param skillId skill ID
     * @param workspaceId workspace ID
     * @param projectId project ID
     * @return skill details
     */
    GetStudioSkillDetailsResponseBody showStudioSkillDetail(String skillId, String workspaceId, String projectId);
}
