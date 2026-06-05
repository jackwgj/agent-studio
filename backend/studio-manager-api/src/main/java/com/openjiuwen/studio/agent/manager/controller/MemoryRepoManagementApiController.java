/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.controller;

import com.openjiuwen.studio.agent.manager.dto.CreateMemoryRepoRequestBody;
import com.openjiuwen.studio.agent.manager.dto.CreateMemoryRepoResponseBody;
import com.openjiuwen.studio.agent.manager.dto.DeleteMemoryRepoResponseBody;
import com.openjiuwen.studio.agent.manager.dto.ListMemoryReposResponseBody;
import com.openjiuwen.studio.agent.manager.dto.ListMemoryRepositoriesQo;
import com.openjiuwen.studio.agent.manager.dto.ModifyMemoryRepoRequestBody;
import com.openjiuwen.studio.agent.manager.dto.ModifyMemoryRepoResponseBody;
import com.openjiuwen.studio.agent.manager.dto.ShowMemoryRepoResponseBody;
import com.openjiuwen.studio.agent.manager.service.IMemoryRepoManagementService;
import com.openjiuwen.studio.agent.common.utils.ResponseModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * MemoryRepoManagement controller
 */
@RestController

public class MemoryRepoManagementApiController implements MemoryRepoManagementApi {
    private static final Logger log = LoggerFactory.getLogger(MemoryRepoManagementApiController.class);

    @Autowired
    private IMemoryRepoManagementService memoryRepoManagementService;

    @Override
    public ResponseEntity<CreateMemoryRepoResponseBody> createMemoryRepo(String projectId, String workspaceId,
        CreateMemoryRepoRequestBody body) {
        return ResponseModel.success(memoryRepoManagementService.createMemoryRepo(projectId, workspaceId, body));
    }

    @Override
    public ResponseEntity<DeleteMemoryRepoResponseBody> deleteMemoryRepo(String projectId, String memoryRepoId,
        String workspaceId) {
        return ResponseModel.success(
            memoryRepoManagementService.deleteMemoryRepo(projectId, memoryRepoId, workspaceId));
    }

    @Override
    public ResponseEntity<ListMemoryReposResponseBody> listMemoryRepositories(String projectId,
        ListMemoryRepositoriesQo listMemoryRepositoriesQo) {
        return ResponseModel.success(
            memoryRepoManagementService.listMemoryRepositories(projectId, listMemoryRepositoriesQo));
    }

    @Override
    public ResponseEntity<ModifyMemoryRepoResponseBody> modifyMemoryRepo(String projectId, String memoryRepoId,
        String workspaceId, ModifyMemoryRepoRequestBody body) {
        return ResponseModel.success(
            memoryRepoManagementService.modifyMemoryRepo(projectId, memoryRepoId, workspaceId, body));
    }

    @Override
    public ResponseEntity<ShowMemoryRepoResponseBody> showMemoryRepo(String projectId, String memoryRepoId,
        String workspaceId) {
        return ResponseModel.success(memoryRepoManagementService.showMemoryRepo(projectId, memoryRepoId, workspaceId));
    }
}