/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */


package com.openjiuwen.studio.agent.runtime.controller;

import com.openjiuwen.studio.agent.common.utils.ResponseModel;
import com.openjiuwen.studio.agent.runtime.dto.ClearLongTermMemoriesResponseBody;
import com.openjiuwen.studio.agent.runtime.dto.DeleteLongTermMemoriesRequestBody;
import com.openjiuwen.studio.agent.runtime.dto.DeleteLongTermMemoriesResponseBody;
import com.openjiuwen.studio.agent.runtime.dto.ListLongTermMemoriesQo;
import com.openjiuwen.studio.agent.runtime.dto.ListLongTermMemoriesResponseBody;
import com.openjiuwen.studio.agent.runtime.dto.UpdateLongTermMemoriesRequestBody;
import com.openjiuwen.studio.agent.runtime.dto.UpdateLongTermMemoriesResponseBody;
import com.openjiuwen.studio.agent.runtime.service.IUserMemoryManagementService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;


/**
 * UserMemoryManagement controller
 */
@RestController

public class UserMemoryManagementApiController implements UserMemoryManagementApi {
    private static final Logger log = LoggerFactory.getLogger(UserMemoryManagementApiController.class);

    @Autowired
    private IUserMemoryManagementService userMemoryManagementService;

    @Override
    public ResponseEntity<ClearLongTermMemoriesResponseBody> clearLongTermMemories(String projectId, String memoryRepoId) {
        return ResponseModel.success(userMemoryManagementService.clearLongTermMemories(projectId, memoryRepoId));
    }

    @Override
    public ResponseEntity<DeleteLongTermMemoriesResponseBody> deleteLongTermMemories(String projectId, String memoryRepoId, DeleteLongTermMemoriesRequestBody body) {
        return ResponseModel.success(userMemoryManagementService.deleteLongTermMemories(projectId, memoryRepoId, body));
    }

    @Override
    public ResponseEntity<ListLongTermMemoriesResponseBody> listLongTermMemories(String projectId, ListLongTermMemoriesQo listLongTermMemoriesQo) {
        return ResponseModel.success(userMemoryManagementService.listLongTermMemories(projectId, listLongTermMemoriesQo));
    }

    @Override
    public ResponseEntity<UpdateLongTermMemoriesResponseBody> updateLongTermMemories(String projectId, String memoryRepoId, UpdateLongTermMemoriesRequestBody body) {
        return ResponseModel.success(userMemoryManagementService.updateLongTermMemories(projectId, memoryRepoId, body));
    }
}