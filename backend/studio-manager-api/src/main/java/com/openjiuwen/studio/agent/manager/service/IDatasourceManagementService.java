/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service;

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

/**
 * DatasourceManagement service
 */

public interface IDatasourceManagementService {

    /**
     * batchDeleteDatasource
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     * @param body body
     */
    DatasourceBatchDeleteRsp batchDeleteDatasource(String projectId, String workspaceId, DatasourceBatchDeleteReq body);

    /**
     * createDatasource
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     * @param body body
     */
    DatasourceBriefRsp createDatasource(String projectId, String workspaceId, DatasourceInfoReq body);

    /**
     * deleteDatasource
     *
     * @param projectId projectId
     * @param datasourceId datasourceId
     * @param workspaceId workspaceId
     */
    DatasourceDeleteRsp deleteDatasource(String projectId, String datasourceId, String workspaceId);

    /**
     * listDatasource
     *
     * @param projectId projectId
     * @param listDatasourceQo listDatasourceQo
     */
    DatasourceListRsp listDatasource(String projectId, ListDatasourceQo listDatasourceQo);

    /**
     * modifyDatasource
     *
     * @param projectId projectId
     * @param datasourceId datasourceId
     * @param workspaceId workspaceId
     * @param body body
     */
    DatasourceBriefRsp modifyDatasource(String projectId, String datasourceId, String workspaceId,
        DatasourceInfoReq body);

    /**
     * retrieveDatasource
     *
     * @param projectId projectId
     * @param datasourceId datasourceId
     * @param workspaceId workspaceId
     */
    DatasourceInfoRsp retrieveDatasource(String projectId, String datasourceId, String workspaceId);

    /**
     * retrieveDatasourceTableColumns
     *
     * @param projectId projectId
     * @param datasourceId datasourceId
     * @param tableName tableName
     * @param retrieveDatasourceTableColumnsQo retrieveDatasourceTableColumnsQo
     */
    DatasourceTableColumnsRsp retrieveDatasourceTableColumns(String projectId, String datasourceId, String tableName,
        RetrieveDatasourceTableColumnsQo retrieveDatasourceTableColumnsQo);

    /**
     * retrieveDatasourceTables
     *
     * @param projectId projectId
     * @param datasourceId datasourceId
     * @param retrieveDatasourceTablesQo retrieveDatasourceTablesQo
     */
    DatasourceTablesRsp retrieveDatasourceTables(String projectId, String datasourceId,
        RetrieveDatasourceTablesQo retrieveDatasourceTablesQo);
}