/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.controller;

import com.openjiuwen.studio.agent.manager.dto.AvailableModelServicesQo;
import com.openjiuwen.studio.agent.manager.dto.BaseResp;
import com.openjiuwen.studio.agent.manager.dto.MaasServiceRsp;
import com.openjiuwen.studio.agent.manager.dto.ModelInvokeDataListRsp;
import com.openjiuwen.studio.agent.manager.dto.ModelNameResp;
import com.openjiuwen.studio.agent.manager.dto.ModelServiceListQo;
import com.openjiuwen.studio.agent.manager.dto.ModelServiceListRsp;
import com.openjiuwen.studio.agent.manager.dto.ModelServiceReq;
import com.openjiuwen.studio.agent.manager.dto.ModelServiceRsp;
import com.openjiuwen.studio.agent.manager.dto.ModelStatusReq;
import com.openjiuwen.studio.agent.manager.service.IModelServiceMgmtService;
import com.openjiuwen.studio.agent.common.utils.ResponseModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * ModelServiceMgmt controller
 */
@RestController

public class ModelServiceMgmtApiController implements ModelServiceMgmtApi {
    private static final Logger log = LoggerFactory.getLogger(ModelServiceMgmtApiController.class);

    @Autowired
    private IModelServiceMgmtService modelServiceMgmtService;

    @Override
    public ResponseEntity<Object> availableModelServices(String projectId,
        AvailableModelServicesQo availableModelServicesQo) {
        return ResponseModel.success(
            modelServiceMgmtService.availableModelServices(projectId, availableModelServicesQo));
    }

    @Override
    public ResponseEntity<Object> createModelService(String projectId, String workspaceId, Boolean availableCheck,
        ModelServiceReq body) {
        return ResponseModel.success(
            modelServiceMgmtService.createModelService(projectId, workspaceId, availableCheck, body));
    }

    @Override
    public ResponseEntity<Void> deleteModelService(String projectId, String workspaceId, String id) {
        return ResponseModel.success(modelServiceMgmtService.deleteModelService(projectId, workspaceId, id));
    }

    @Override
    public ResponseEntity<ModelNameResp> existsModelName(String projectId, String workspaceId, String modelName) {
        return ResponseModel.success(modelServiceMgmtService.existsModelName(projectId, workspaceId, modelName));
    }

    @Override
    public ResponseEntity<MaasServiceRsp> maasModelServiceList(String projectId) {
        return ResponseModel.success(modelServiceMgmtService.maasModelServiceList(projectId));
    }

    @Override
    public ResponseEntity<ModelServiceRsp> modelServiceDetail(String projectId, String workspaceId, String id) {
        return ResponseModel.success(modelServiceMgmtService.modelServiceDetail(projectId, workspaceId, id));
    }

    @Override
    public ResponseEntity<ModelServiceListRsp> modelServiceList(String projectId,
        ModelServiceListQo modelServiceListQo) {
        return ResponseModel.success(modelServiceMgmtService.modelServiceList(projectId, modelServiceListQo));
    }

    @Override
    public ResponseEntity<Void> offlineModelService(String projectId, String workspaceId, String id) {
        return ResponseModel.success(modelServiceMgmtService.offlineModelService(projectId, workspaceId, id));
    }

    @Override
    public ResponseEntity<Void> onlineModelService(String projectId, String workspaceId, Boolean availableCheck,
        String id) {
        return ResponseModel.success(
            modelServiceMgmtService.onlineModelService(projectId, workspaceId, availableCheck, id));
    }

    @Override
    public ResponseEntity<ModelInvokeDataListRsp> queryModelInvokeData(String projectId, String modelId,
        String workspaceId) {
        return ResponseModel.success(modelServiceMgmtService.queryModelInvokeData(projectId, modelId, workspaceId));
    }

    @Override
    public ResponseEntity<BaseResp> syncModelService(String projectId, String workspaceId, String providerId) {
        return ResponseModel.success(modelServiceMgmtService.syncModelService(projectId, workspaceId, providerId));
    }

    @Override
    public ResponseEntity<Object> updateModelService(String projectId, String workspaceId, Boolean availableCheck,
        String id, ModelServiceReq body) {
        return ResponseModel.success(
            modelServiceMgmtService.updateModelService(projectId, workspaceId, availableCheck, id, body));
    }

    @Override
    public ResponseEntity<Void> updateModelStatus(String projectId, String workspaceId, ModelStatusReq body) {
        return ResponseModel.success(modelServiceMgmtService.updateModelStatus(projectId, workspaceId, body));
    }
}