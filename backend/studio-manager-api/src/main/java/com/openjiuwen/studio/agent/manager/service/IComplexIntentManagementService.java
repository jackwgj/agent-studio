/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service;

import com.openjiuwen.studio.agent.manager.dto.ComplexIntentBranchBriefRsp;
import com.openjiuwen.studio.agent.manager.dto.ComplexIntentBranchInfoReq;
import com.openjiuwen.studio.agent.manager.dto.ComplexIntentBranchListRsp;
import com.openjiuwen.studio.agent.manager.dto.ComplexIntentBriefRsp;
import com.openjiuwen.studio.agent.manager.dto.ComplexIntentExportParams;
import com.openjiuwen.studio.agent.manager.dto.ComplexIntentImportRsp;
import com.openjiuwen.studio.agent.manager.dto.ComplexIntentInfoReq;
import com.openjiuwen.studio.agent.manager.dto.ComplexIntentListRsp;
import com.openjiuwen.studio.agent.manager.dto.ComplexIntentRsp;
import com.openjiuwen.studio.agent.manager.dto.ListComplexIntentBranchQo;
import com.openjiuwen.studio.agent.manager.dto.ListComplexIntentQo;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * ComplexIntentManagement service
 */

public interface IComplexIntentManagementService {

    /**
     * createComplexIntent
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     * @param body body
     */
    ComplexIntentBriefRsp createComplexIntent(String projectId, String workspaceId, ComplexIntentInfoReq body);

    /**
     * createComplexIntentBranch
     *
     * @param projectId projectId
     * @param intentId intentId
     * @param workspaceId workspaceId
     * @param body body
     */
    ComplexIntentBranchBriefRsp createComplexIntentBranch(String projectId, String intentId, String workspaceId,
        ComplexIntentBranchInfoReq body);

    /**
     * deleteComplexIntent
     *
     * @param projectId projectId
     * @param intentId intentId
     * @param workspaceId workspaceId
     */
    ComplexIntentBriefRsp deleteComplexIntent(String projectId, String intentId, String workspaceId);

    /**
     * deleteComplexIntentBranch
     *
     * @param projectId projectId
     * @param intentId intentId
     * @param branchId branchId
     * @param workspaceId workspaceId
     */
    ComplexIntentBranchBriefRsp deleteComplexIntentBranch(String projectId, String intentId, String branchId,
        String workspaceId);

    /**
     * exportIntent
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     * @param body body
     */
    Resource exportIntent(String projectId, String workspaceId, ComplexIntentExportParams body);

    /**
     * exportIntentTemplate
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     */
    Resource exportIntentTemplate(String projectId, String workspaceId);

    /**
     * importIntent
     *
     * @param workspaceId workspaceId
     * @param projectId projectId
     * @param file file
     * @param intentName intentName
     */
    ComplexIntentImportRsp importIntent(String workspaceId, String projectId, MultipartFile file, String intentName);

    /**
     * importIntentBranch
     *
     * @param workspaceId workspaceId
     * @param projectId projectId
     * @param intentId intentId
     * @param file file
     */
    ComplexIntentImportRsp importIntentBranch(String workspaceId, String projectId, String intentId,
        MultipartFile file);

    /**
     * listComplexIntent
     *
     * @param projectId projectId
     * @param listComplexIntentQo listComplexIntentQo
     */
    ComplexIntentListRsp listComplexIntent(String projectId, ListComplexIntentQo listComplexIntentQo);

    /**
     * listComplexIntentBranch
     *
     * @param projectId projectId
     * @param intentId intentId
     * @param listComplexIntentBranchQo listComplexIntentBranchQo
     */
    ComplexIntentBranchListRsp listComplexIntentBranch(String projectId, String intentId,
        ListComplexIntentBranchQo listComplexIntentBranchQo);

    /**
     * modifyComplexIntent
     *
     * @param projectId projectId
     * @param intentId intentId
     * @param workspaceId workspaceId
     * @param body body
     */
    ComplexIntentBriefRsp modifyComplexIntent(String projectId, String intentId, String workspaceId,
        ComplexIntentInfoReq body);

    /**
     * modifyComplexIntentBranch
     *
     * @param projectId projectId
     * @param intentId intentId
     * @param branchId branchId
     * @param workspaceId workspaceId
     * @param body body
     */
    ComplexIntentBranchBriefRsp modifyComplexIntentBranch(String projectId, String intentId, String branchId,
        String workspaceId, ComplexIntentBranchInfoReq body);

    /**
     * retrieveComplexIntent
     *
     * @param projectId projectId
     * @param intentId intentId
     * @param workspaceId workspaceId
     */
    ComplexIntentRsp retrieveComplexIntent(String projectId, String intentId, String workspaceId);
}