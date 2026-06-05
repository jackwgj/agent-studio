/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */


package com.openjiuwen.studio.agent.runtime.controller;

import com.openjiuwen.studio.agent.common.utils.ResponseModel;
import com.openjiuwen.studio.agent.runtime.dto.DatasourceExecuteReq;
import com.openjiuwen.studio.agent.runtime.dto.DatasourceExecuteResp;
import com.openjiuwen.studio.agent.runtime.service.IDatasourceRuntimeService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;


/**
 * DatasourceRuntime controller
 */
@RestController

public class DatasourceRuntimeApiController implements DatasourceRuntimeApi {
    private static final Logger log = LoggerFactory.getLogger(DatasourceRuntimeApiController.class);

    @Autowired
    private IDatasourceRuntimeService datasourceRuntimeService;

    @Override
    public ResponseEntity<DatasourceExecuteResp> executeDatasource(String datasourceId, DatasourceExecuteReq body) {
        return ResponseModel.success(datasourceRuntimeService.executeDatasource(datasourceId, body));
    }
}