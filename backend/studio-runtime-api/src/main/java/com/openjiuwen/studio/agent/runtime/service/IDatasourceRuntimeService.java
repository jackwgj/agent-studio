/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */


package com.openjiuwen.studio.agent.runtime.service;

import com.openjiuwen.studio.agent.runtime.dto.DatasourceExecuteReq;
import com.openjiuwen.studio.agent.runtime.dto.DatasourceExecuteResp;


/**
 * DatasourceRuntime service
 */

public interface IDatasourceRuntimeService {

    /**
     * executeDatasource
     *
     * @param datasourceId datasourceId
     * @param body body
     */
    DatasourceExecuteResp executeDatasource(String datasourceId, DatasourceExecuteReq body) ;
}