/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.controller;

import com.openjiuwen.studio.agent.manager.dto.ExportStudioSkillResponseBody;
import com.openjiuwen.studio.agent.manager.dto.GetStudioSkillDetailsResponseBody;
import com.openjiuwen.studio.agent.manager.dto.ImportStudioSkillResponseBody;
import com.openjiuwen.studio.agent.manager.dto.ListStudioSkillsQo;
import com.openjiuwen.studio.agent.manager.dto.ListStudioSkillsResponseBody;
import com.openjiuwen.studio.agent.manager.service.ISIESkillManagementService;
import com.openjiuwen.studio.agent.common.utils.ResponseModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * SkillManagement controller
 */
@RestController

public class SIESkillManagementApiController implements SIESkillManagementApi {
    private static final Logger log = LoggerFactory.getLogger(SIESkillManagementApiController.class);

    @Autowired
    private ISIESkillManagementService sieSkillManagementService;
      @Override
    public ResponseEntity<Void> deleteStudioSkill(String skillId, String workspaceId, String projectId) {
        return ResponseModel.success(sieSkillManagementService.deleteStudioSkill(skillId, workspaceId, projectId));
    }

    @Override
    public ResponseEntity<ExportStudioSkillResponseBody> exportStudioSkill(String skillId, String workspaceId,
        String projectId) {
        return ResponseModel.success(sieSkillManagementService.exportStudioSkill(skillId, workspaceId, projectId));
    }

    @Override
    public ResponseEntity<ImportStudioSkillResponseBody> importStudioSkill(String workspaceId, String projectId,
        MultipartFile file) {
        return ResponseModel.success(sieSkillManagementService.importStudioSkill(workspaceId, projectId, file));
    }


    @Override
    public ResponseEntity<ListStudioSkillsResponseBody> listStudioSkills(String projectId,
        ListStudioSkillsQo listStudioSkillsQo) {
        return ResponseModel.success(sieSkillManagementService.listStudioSkills(projectId, listStudioSkillsQo));
    }

    @Override
    public ResponseEntity<GetStudioSkillDetailsResponseBody> showStudioSkillDetail(String skillId, String workspaceId,
        String projectId) {
        return ResponseModel.success(sieSkillManagementService.showStudioSkillDetail(skillId, workspaceId, projectId));
    }
}