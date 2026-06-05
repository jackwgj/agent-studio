/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.controller;

import com.openjiuwen.studio.agent.common.utils.ResponseModel;
import com.openjiuwen.studio.prompt.engineering.dto.PePromptTemplateDto;
import com.openjiuwen.studio.prompt.engineering.dto.PePromptTemplateListVo;
import com.openjiuwen.studio.prompt.engineering.dto.QueryTemplateCondition;
import com.openjiuwen.studio.prompt.engineering.service.IPromptLibraryService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * PromptLibrary controller
 */
@RestController

public class PromptLibraryApiController implements PromptLibraryApi {

    private static final Logger log = LoggerFactory.getLogger(PromptLibraryApiController.class);

    @Autowired
    private IPromptLibraryService promptLibraryService;

    @Override
    public ResponseEntity<String> deletePromptTemplate(String projectId, String id, String workspaceId) {
        return ResponseModel.success(promptLibraryService.deletePromptTemplate(projectId, id, workspaceId));
    }

    @Override
    public ResponseEntity<String> downloadPromptSampleTemplate(String projectId, String workspaceId) {
        return ResponseModel.success(promptLibraryService.downloadPromptSampleTemplate(projectId, workspaceId));
    }

    @Override
    public ResponseEntity<String> downloadPromptTemplateV2(String projectId, String workspaceId, List<String> body) {
        return ResponseModel.success(promptLibraryService.downloadPromptTemplateV2(projectId, workspaceId, body));
    }

    @Override
    public ResponseEntity<List<String>> importPromptTemplateV2(String workspaceId, String projectId, MultipartFile file) {
        return ResponseModel.success(promptLibraryService.importPromptTemplateV2(workspaceId, projectId, file));
    }

    @Override
    public ResponseEntity<PePromptTemplateListVo> listPromptTemplates(String projectId, String workspaceId, String category, QueryTemplateCondition body) {
        return ResponseModel.success(promptLibraryService.listPromptTemplates(projectId, workspaceId, category, body));
    }

    @Override
    public ResponseEntity<String> savePromptTemplate(String projectId, String workspaceId, PePromptTemplateDto body) {
        return ResponseModel.success(promptLibraryService.savePromptTemplate(projectId, workspaceId, body));
    }
}
