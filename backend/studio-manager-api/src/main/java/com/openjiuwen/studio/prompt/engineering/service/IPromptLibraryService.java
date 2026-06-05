/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.service;

import com.openjiuwen.studio.prompt.engineering.dto.PePromptTemplateDto;
import com.openjiuwen.studio.prompt.engineering.dto.PePromptTemplateListVo;
import com.openjiuwen.studio.prompt.engineering.dto.QueryTemplateCondition;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * PromptLibrary service
 */

public interface IPromptLibraryService {

    /**
     * deletePromptTemplate
     *
     * @param projectId projectId
     * @param id id
     * @param workspaceId workspaceId
     */
    String deletePromptTemplate(String projectId, String id, String workspaceId);

    /**
     * downloadPromptSampleTemplate
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     */
    String downloadPromptSampleTemplate(String projectId, String workspaceId);

    /**
     * downloadPromptTemplateV2
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     * @param body body
     */
    String downloadPromptTemplateV2(String projectId, String workspaceId, List<String> body);

    /**
     * importPromptTemplateV2
     *
     * @param workspaceId workspaceId
     * @param projectId projectId
     * @param file file
     */
    List<String> importPromptTemplateV2(String workspaceId, String projectId, MultipartFile file);

    /**
     * listPromptTemplates
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     * @param category category
     * @param body body
     */
    PePromptTemplateListVo listPromptTemplates(String projectId, String workspaceId, String category, QueryTemplateCondition body);

    /**
     * savePromptTemplate
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     * @param body body
     */
    String savePromptTemplate(String projectId, String workspaceId, PePromptTemplateDto body);
}
