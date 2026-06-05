/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.service;

import com.openjiuwen.studio.prompt.engineering.dto.BaseInfo;
import com.openjiuwen.studio.prompt.engineering.dto.ListEvalDataSetQo;
import com.openjiuwen.studio.prompt.engineering.dto.PromptBaseResp;
import com.openjiuwen.studio.prompt.engineering.dto.PromptEvalDataItem;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * PromptEngineerDataSet service
 */

public interface IPromptEngineerDataSetService {

    /**
     * addBatchEvalDataSetItem
     *
     * @param projectId projectId
     * @param taskId taskId
     * @param workspaceId workspaceId
     * @param body body
     */
    BaseInfo addBatchEvalDataSetItem(String projectId, String taskId, String workspaceId, List<PromptEvalDataItem> body);

    /**
     * addSingleEvalDataSetItem
     *
     * @param projectId projectId
     * @param taskId taskId
     * @param workspaceId workspaceId
     * @param body body
     */
    BaseInfo addSingleEvalDataSetItem(String projectId, String taskId, String workspaceId, PromptEvalDataItem body);

    /**
     * deleteEvalDataSetItem
     *
     * @param projectId projectId
     * @param taskId taskId
     * @param itemId itemId
     * @param workspaceId workspaceId
     */
    BaseInfo deleteEvalDataSetItem(String projectId, String taskId, String itemId, String workspaceId);

    /**
     * downloadEvalDataSetTemplate
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     * @param ptType ptType
     */
    Resource downloadEvalDataSetTemplate(String projectId, String workspaceId, String ptType);

    /**
     * importEvalDataSetItems
     *
     * @param workspaceId workspaceId
     * @param projectId projectId
     * @param taskId taskId
     * @param file file
     */
    BaseInfo importEvalDataSetItems(String workspaceId, String projectId, String taskId, MultipartFile file);

    /**
     * listEvalDataSet
     *
     * @param projectId projectId
     * @param taskId taskId
     * @param listEvalDataSetQo listEvalDataSetQo
     */
    PromptBaseResp listEvalDataSet(String projectId, String taskId, ListEvalDataSetQo listEvalDataSetQo);

    /**
     * updateEvalDataSetItem
     *
     * @param projectId projectId
     * @param taskId taskId
     * @param itemId itemId
     * @param workspaceId workspaceId
     * @param body body
     */
    PromptEvalDataItem updateEvalDataSetItem(String projectId, String taskId, String itemId, String workspaceId, PromptEvalDataItem body);

    /**
     * uploadPicture
     *
     * @param workspaceId workspaceId
     * @param projectId projectId
     * @param taskId taskId
     * @param file file
     */
    PromptBaseResp uploadPicture(String workspaceId, String projectId, String taskId, MultipartFile file);
}
