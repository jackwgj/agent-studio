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
 * 组件库 Skill 生命周期管理服务接口（SIE）。
 *
 * <p>方法签名与 {@link ISkillManagementService} 一致，独立成接口以便组件库（SIE）链路与常规
 * Skill 链路在类型层面区分：SIESkillManagementApiController 按此接口注入 SIESkillManagementService，
 * 避免与常规 SkillManagementService 在同一 ISkillManagementService 类型上产生注入歧义。</p>
 */
public interface ISIESkillManagementService {

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
