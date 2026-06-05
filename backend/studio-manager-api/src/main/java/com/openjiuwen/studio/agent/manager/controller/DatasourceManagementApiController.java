/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.controller;

import com.openjiuwen.studio.agent.manager.dto.DatasourceBatchDeleteReq;
import com.openjiuwen.studio.agent.manager.dto.DatasourceBatchDeleteRsp;
import com.openjiuwen.studio.agent.manager.dto.DatasourceBriefRsp;
import com.openjiuwen.studio.agent.manager.dto.DatasourceDeleteRsp;
import com.openjiuwen.studio.agent.manager.dto.DatasourceInfoReq;
import com.openjiuwen.studio.agent.manager.dto.DatasourceInfoRsp;
import com.openjiuwen.studio.agent.manager.dto.DatasourceListRsp;
import com.openjiuwen.studio.agent.manager.dto.DatasourceTableColumnsRsp;
import com.openjiuwen.studio.agent.manager.dto.DatasourceTablesRsp;
import com.openjiuwen.studio.agent.manager.dto.ListDatasourceQo;
import com.openjiuwen.studio.agent.manager.dto.RetrieveDatasourceTableColumnsQo;
import com.openjiuwen.studio.agent.manager.dto.RetrieveDatasourceTablesQo;
import com.openjiuwen.studio.agent.manager.service.IDatasourceManagementService;
import com.openjiuwen.studio.agent.common.utils.ResponseModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * DatasourceManagement controller
 */
@RestController

public class DatasourceManagementApiController implements DatasourceManagementApi {
    private static final Logger log = LoggerFactory.getLogger(DatasourceManagementApiController.class);

    @Autowired
    private IDatasourceManagementService datasourceManagementService;

    @Override
    public ResponseEntity<DatasourceBatchDeleteRsp> batchDeleteDatasource(String projectId, String workspaceId,
        DatasourceBatchDeleteReq body) {
        return ResponseModel.success(datasourceManagementService.batchDeleteDatasource(projectId, workspaceId, body));
    }

    @Override
    public ResponseEntity<DatasourceBriefRsp> createDatasource(String projectId, String workspaceId,
        DatasourceInfoReq body) {
        return ResponseModel.success(datasourceManagementService.createDatasource(projectId, workspaceId, body));
    }

    @Override
    public ResponseEntity<DatasourceDeleteRsp> deleteDatasource(String projectId, String datasourceId,
        String workspaceId) {
        return ResponseModel.success(
            datasourceManagementService.deleteDatasource(projectId, datasourceId, workspaceId));
    }

    @Override
    public ResponseEntity<DatasourceListRsp> listDatasource(String projectId, ListDatasourceQo listDatasourceQo) {
        return ResponseModel.success(datasourceManagementService.listDatasource(projectId, listDatasourceQo));
    }

    @Override
    public ResponseEntity<DatasourceBriefRsp> modifyDatasource(String projectId, String datasourceId,
        String workspaceId, DatasourceInfoReq body) {
        return ResponseModel.success(
            datasourceManagementService.modifyDatasource(projectId, datasourceId, workspaceId, body));
    }

    @Override
    public ResponseEntity<DatasourceInfoRsp> retrieveDatasource(String projectId, String datasourceId,
        String workspaceId) {
        return ResponseModel.success(
            datasourceManagementService.retrieveDatasource(projectId, datasourceId, workspaceId));
    }

    @Override
    public ResponseEntity<DatasourceTableColumnsRsp> retrieveDatasourceTableColumns(String projectId,
        String datasourceId, String tableName, RetrieveDatasourceTableColumnsQo retrieveDatasourceTableColumnsQo) {
        return ResponseModel.success(
            datasourceManagementService.retrieveDatasourceTableColumns(projectId, datasourceId, tableName,
                retrieveDatasourceTableColumnsQo));
    }

    @Override
    public ResponseEntity<DatasourceTablesRsp> retrieveDatasourceTables(String projectId, String datasourceId,
        RetrieveDatasourceTablesQo retrieveDatasourceTablesQo) {
        return ResponseModel.success(
            datasourceManagementService.retrieveDatasourceTables(projectId, datasourceId, retrieveDatasourceTablesQo));
    }
}