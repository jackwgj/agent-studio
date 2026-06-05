/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.controller;

import com.openjiuwen.studio.agent.manager.dto.CommonDeleteRsp;
import com.openjiuwen.studio.agent.manager.dto.CreateExternalKnowledgeBaseRequestBody;
import com.openjiuwen.studio.agent.manager.dto.CreateExternalKnowledgeBaseResponseBody;
import com.openjiuwen.studio.agent.manager.dto.CreateKnowledgeRepoRequestBody;
import com.openjiuwen.studio.agent.manager.dto.CreateKnowledgeRepoResponseBody;
import com.openjiuwen.studio.agent.manager.dto.CreateKnowledgeRepoTagsReq;
import com.openjiuwen.studio.agent.manager.dto.CreateKnowledgeRepoTagsRsp;
import com.openjiuwen.studio.agent.manager.dto.DeleteKnowledgeRepoTagsReq;
import com.openjiuwen.studio.agent.manager.dto.DeleteKnowledgeRepoTagsRsp;
import com.openjiuwen.studio.agent.manager.dto.DeleteOneResponseBody;
import com.openjiuwen.studio.agent.manager.dto.KnowledgeSearchRecordRsp;
import com.openjiuwen.studio.agent.manager.dto.ListExternalKnowledgeBasesQo;
import com.openjiuwen.studio.agent.manager.dto.ListExternalKnowledgeBasesResponseBody;
import com.openjiuwen.studio.agent.manager.dto.ListKnowledgeBasesQo;
import com.openjiuwen.studio.agent.manager.dto.ListKnowledgeBasesResponseBody;
import com.openjiuwen.studio.agent.manager.dto.ListKnowledgeModelsQo;
import com.openjiuwen.studio.agent.manager.dto.ListKnowledgeModelsResponseBody;
import com.openjiuwen.studio.agent.manager.dto.ListKnowledgeRepoTagsQo;
import com.openjiuwen.studio.agent.manager.dto.ListKnowledgeSearchRecordsQo;
import com.openjiuwen.studio.agent.manager.dto.ListKnowledgeTagsResp;
import com.openjiuwen.studio.agent.manager.dto.ModifyKnowledgeRepoRequestBody;
import com.openjiuwen.studio.agent.manager.dto.ModifyKnowledgeRepoResponseBody;
import com.openjiuwen.studio.agent.manager.dto.PermissionsRequestBody;
import com.openjiuwen.studio.agent.manager.dto.SearchKnowledgeRepoRequestBody;
import com.openjiuwen.studio.agent.manager.dto.SearchKnowledgeRepoResponseBody;
import com.openjiuwen.studio.agent.manager.dto.ShowKnowledgeRepoResponseBody;
import com.openjiuwen.studio.agent.manager.dto.StartKnowledgeRepoResponseBody;
import com.openjiuwen.studio.agent.manager.dto.StopKnowledgeRepoResponseBody;
import com.openjiuwen.studio.agent.manager.dto.UpdateKnowledgeVisibilityResponse;
import com.openjiuwen.studio.agent.manager.service.IKnowledgeRepoManagementService;
import com.openjiuwen.studio.agent.common.utils.ResponseModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * KnowledgeRepoManagement controller
 */
@RestController

public class KnowledgeRepoManagementApiController implements KnowledgeRepoManagementApi {
    private static final Logger log = LoggerFactory.getLogger(KnowledgeRepoManagementApiController.class);

    @Autowired
    private IKnowledgeRepoManagementService knowledgeRepoManagementService;

    @Override
    public ResponseEntity<CreateExternalKnowledgeBaseResponseBody> createExternalKnowledgeBase(String projectId,
        String workspaceId, CreateExternalKnowledgeBaseRequestBody body) {
        return ResponseModel.success(
            knowledgeRepoManagementService.createExternalKnowledgeBase(projectId, workspaceId, body));
    }

    @Override
    public ResponseEntity<CreateKnowledgeRepoResponseBody> createKnowledgeRepo(String projectId, String workspaceId,
        CreateKnowledgeRepoRequestBody body) {
        return ResponseModel.success(knowledgeRepoManagementService.createKnowledgeRepo(projectId, workspaceId, body));
    }

    @Override
    public ResponseEntity<CreateKnowledgeRepoTagsRsp> createKnowledgeRepoTags(String projectId, String workspaceId,
        String knowledgeRepoId, CreateKnowledgeRepoTagsReq body) {
        return ResponseModel.success(
            knowledgeRepoManagementService.createKnowledgeRepoTags(projectId, workspaceId, knowledgeRepoId, body));
    }

    @Override
    public ResponseEntity<DeleteOneResponseBody> deleteKnowledgeRepo(String projectId, String knowledgeRepoId,
        String workspaceId) {
        return ResponseModel.success(
            knowledgeRepoManagementService.deleteKnowledgeRepo(projectId, knowledgeRepoId, workspaceId));
    }

    @Override
    public ResponseEntity<DeleteKnowledgeRepoTagsRsp> deleteKnowledgeRepoTags(String projectId, String workspaceId,
        String knowledgeRepoId, String tagId, DeleteKnowledgeRepoTagsReq body) {
        return ResponseModel.success(
            knowledgeRepoManagementService.deleteKnowledgeRepoTags(projectId, workspaceId, knowledgeRepoId, tagId,
                body));
    }

    @Override
    public ResponseEntity<CommonDeleteRsp> deleteKnowledgeSearchRecord(String projectId, String knowledgeRepoId,
        String recordId, String workspaceId) {
        return ResponseModel.success(
            knowledgeRepoManagementService.deleteKnowledgeSearchRecord(projectId, knowledgeRepoId, recordId,
                workspaceId));
    }

    @Override
    public ResponseEntity<ListExternalKnowledgeBasesResponseBody> listExternalKnowledgeBases(String projectId,
        ListExternalKnowledgeBasesQo listExternalKnowledgeBasesQo) {
        return ResponseModel.success(
            knowledgeRepoManagementService.listExternalKnowledgeBases(projectId, listExternalKnowledgeBasesQo));
    }

    @Override
    public ResponseEntity<ListKnowledgeBasesResponseBody> listKnowledgeBases(String projectId,
        ListKnowledgeBasesQo listKnowledgeBasesQo) {
        return ResponseModel.success(
            knowledgeRepoManagementService.listKnowledgeBases(projectId, listKnowledgeBasesQo));
    }

    @Override
    public ResponseEntity<ListKnowledgeModelsResponseBody> listKnowledgeModels(String projectId,
        ListKnowledgeModelsQo listKnowledgeModelsQo) {
        return ResponseModel.success(
            knowledgeRepoManagementService.listKnowledgeModels(projectId, listKnowledgeModelsQo));
    }

    @Override
    public ResponseEntity<ListKnowledgeTagsResp> listKnowledgeRepoTags(String projectId, String knowledgeRepoId,
        ListKnowledgeRepoTagsQo listKnowledgeRepoTagsQo) {
        return ResponseModel.success(
            knowledgeRepoManagementService.listKnowledgeRepoTags(projectId, knowledgeRepoId, listKnowledgeRepoTagsQo));
    }

    @Override
    public ResponseEntity<KnowledgeSearchRecordRsp> listKnowledgeSearchRecords(String projectId, String knowledgeRepoId,
        ListKnowledgeSearchRecordsQo listKnowledgeSearchRecordsQo) {
        return ResponseModel.success(
            knowledgeRepoManagementService.listKnowledgeSearchRecords(projectId, knowledgeRepoId,
                listKnowledgeSearchRecordsQo));
    }

    @Override
    public ResponseEntity<UpdateKnowledgeVisibilityResponse> modifyKnowledgeBaseVisibility(String projectId,
        String knowledgeBaseId, String workspaceId, PermissionsRequestBody body) {
        return ResponseModel.success(
            knowledgeRepoManagementService.modifyKnowledgeBaseVisibility(projectId, knowledgeBaseId, workspaceId,
                body));
    }

    @Override
    public ResponseEntity<ModifyKnowledgeRepoResponseBody> modifyKnowledgeRepo(String projectId, String knowledgeRepoId,
        String workspaceId, ModifyKnowledgeRepoRequestBody body) {
        return ResponseModel.success(
            knowledgeRepoManagementService.modifyKnowledgeRepo(projectId, knowledgeRepoId, workspaceId, body));
    }

    @Override
    public ResponseEntity<SearchKnowledgeRepoResponseBody> searchKnowledgeRepo(String projectId, String workspaceId,
        Integer offset, Integer limit, SearchKnowledgeRepoRequestBody body) {
        return ResponseModel.success(
            knowledgeRepoManagementService.searchKnowledgeRepo(projectId, workspaceId, offset, limit, body));
    }

    @Override
    public ResponseEntity<ShowKnowledgeRepoResponseBody> showKnowledgeRepo(String projectId, String knowledgeRepoId,
        String workspaceId) {
        return ResponseModel.success(
            knowledgeRepoManagementService.showKnowledgeRepo(projectId, knowledgeRepoId, workspaceId));
    }

    @Override
    public ResponseEntity<StartKnowledgeRepoResponseBody> startKnowledgeRepo(String projectId, String knowledgeRepoId,
        String workspaceId) {
        return ResponseModel.success(
            knowledgeRepoManagementService.startKnowledgeRepo(projectId, knowledgeRepoId, workspaceId));
    }

    @Override
    public ResponseEntity<StopKnowledgeRepoResponseBody> stopKnowledgeRepo(String projectId, String knowledgeRepoId,
        String workspaceId) {
        return ResponseModel.success(
            knowledgeRepoManagementService.stopKnowledgeRepo(projectId, knowledgeRepoId, workspaceId));
    }
}