/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.controller;

import com.openjiuwen.studio.agent.common.utils.ResponseModel;
import com.openjiuwen.studio.prompt.engineering.dto.BaseInfo;
import com.openjiuwen.studio.prompt.engineering.dto.ListEvalDataSetQo;
import com.openjiuwen.studio.prompt.engineering.dto.PromptBaseResp;
import com.openjiuwen.studio.prompt.engineering.dto.PromptEvalDataItem;
import com.openjiuwen.studio.prompt.engineering.service.IPromptEngineerDataSetService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * PromptEngineerDataSet controller
 */
@RestController

public class PromptEngineerDataSetApiController implements PromptEngineerDataSetApi {

    private static final Logger log = LoggerFactory.getLogger(PromptEngineerDataSetApiController.class);

    @Autowired
    private IPromptEngineerDataSetService promptEngineerDataSetService;

    @Override
    public ResponseEntity<BaseInfo> addBatchEvalDataSetItem(String projectId, String taskId, String workspaceId, List<PromptEvalDataItem> body) {
        return ResponseModel.success(promptEngineerDataSetService.addBatchEvalDataSetItem(projectId, taskId, workspaceId, body));
    }

    @Override
    public ResponseEntity<BaseInfo> addSingleEvalDataSetItem(String projectId, String taskId, String workspaceId, PromptEvalDataItem body) {
        return ResponseModel.success(promptEngineerDataSetService.addSingleEvalDataSetItem(projectId, taskId, workspaceId, body));
    }

    @Override
    public ResponseEntity<BaseInfo> deleteEvalDataSetItem(String projectId, String taskId, String itemId, String workspaceId) {
        return ResponseModel.success(promptEngineerDataSetService.deleteEvalDataSetItem(projectId, taskId, itemId, workspaceId));
    }

    @Override
    public ResponseEntity<Resource> downloadEvalDataSetTemplate(String projectId, String workspaceId, String ptType) {
        return ResponseModel.successDownload(promptEngineerDataSetService.downloadEvalDataSetTemplate(projectId, workspaceId, ptType));
    }

    @Override
    public ResponseEntity<BaseInfo> importEvalDataSetItems(String workspaceId, String projectId, String taskId, MultipartFile file) {
        return ResponseModel.success(promptEngineerDataSetService.importEvalDataSetItems(workspaceId, projectId, taskId, file));
    }

    @Override
    public ResponseEntity<PromptBaseResp> listEvalDataSet(String projectId, String taskId, ListEvalDataSetQo listEvalDataSetQo) {
        return ResponseModel.success(promptEngineerDataSetService.listEvalDataSet(projectId, taskId, listEvalDataSetQo));
    }

    @Override
    public ResponseEntity<PromptEvalDataItem> updateEvalDataSetItem(String projectId, String taskId, String itemId, String workspaceId, PromptEvalDataItem body) {
        return ResponseModel.success(promptEngineerDataSetService.updateEvalDataSetItem(projectId, taskId, itemId, workspaceId, body));
    }

    @Override
    public ResponseEntity<PromptBaseResp> uploadPicture(String workspaceId, String projectId, String taskId, MultipartFile file) {
        return ResponseModel.success(promptEngineerDataSetService.uploadPicture(workspaceId, projectId, taskId, file));
    }
}
