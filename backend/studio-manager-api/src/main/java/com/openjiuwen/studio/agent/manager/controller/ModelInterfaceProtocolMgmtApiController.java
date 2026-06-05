/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.controller;

import com.openjiuwen.studio.agent.manager.dto.ModelInterfaceProtocolListRsp;
import com.openjiuwen.studio.agent.manager.dto.QueryMdInterfaceProtocolQo;
import com.openjiuwen.studio.agent.manager.service.IModelInterfaceProtocolMgmtService;
import com.openjiuwen.studio.agent.common.utils.ResponseModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * ModelInterfaceProtocolMgmt controller
 */
@RestController

public class ModelInterfaceProtocolMgmtApiController implements ModelInterfaceProtocolMgmtApi {
    private static final Logger log = LoggerFactory.getLogger(ModelInterfaceProtocolMgmtApiController.class);

    @Autowired
    private IModelInterfaceProtocolMgmtService modelInterfaceProtocolMgmtService;

    @Override
    public ResponseEntity<ModelInterfaceProtocolListRsp> queryMdInterfaceProtocol(String projectId,
        QueryMdInterfaceProtocolQo queryMdInterfaceProtocolQo) {
        return ResponseModel.success(
            modelInterfaceProtocolMgmtService.queryMdInterfaceProtocol(projectId, queryMdInterfaceProtocolQo));
    }
}