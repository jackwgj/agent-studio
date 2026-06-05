/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service;

import com.openjiuwen.studio.agent.manager.dto.ExportStudioSkillResponseBody;
import com.openjiuwen.studio.agent.manager.dto.GetStudioSkillDetailsResponseBody;
import com.openjiuwen.studio.agent.manager.dto.ImportStudioSkillResponseBody;
import com.openjiuwen.studio.agent.manager.dto.ListStudioSkillsQo;
import com.openjiuwen.studio.agent.manager.dto.ListStudioSkillsResponseBody;

import org.springframework.web.multipart.MultipartFile;

/**
 * SkillManagement service
 */

public interface ISkillManagementService {

    /**
     * deleteStudioSkill
     *
     * @param skillId skillId
     * @param workspaceId workspaceId
     * @param projectId projectId
     */
    Void deleteStudioSkill(String skillId, String workspaceId, String projectId);

    /**
     * exportStudioSkill
     *
     * @param skillId skillId
     * @param workspaceId workspaceId
     * @param projectId projectId
     */
    ExportStudioSkillResponseBody exportStudioSkill(String skillId, String workspaceId, String projectId);

    /**
     * importStudioSkill
     *
     * @param workspaceId workspaceId
     * @param projectId projectId
     * @param file file
     */
    ImportStudioSkillResponseBody importStudioSkill(String workspaceId, String projectId, MultipartFile file);

    /**
     * listStudioSkills
     *
     * @param projectId projectId
     * @param listStudioSkillsQo listStudioSkillsQo
     */
    ListStudioSkillsResponseBody listStudioSkills(String projectId, ListStudioSkillsQo listStudioSkillsQo);

    /**
     * showStudioSkillDetail
     *
     * @param skillId skillId
     * @param workspaceId workspaceId
     * @param projectId projectId
     */
    GetStudioSkillDetailsResponseBody showStudioSkillDetail(String skillId, String workspaceId, String projectId);
}