/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.controller;

import com.openjiuwen.studio.agent.manager.dto.NotifyReq;
import com.openjiuwen.studio.agent.manager.dto.NotifyResp;
import com.openjiuwen.studio.agent.manager.service.IRosCleanResourceService;
import com.openjiuwen.studio.agent.common.utils.ResponseModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * RosCleanResource controller
 */
@RestController

public class RosCleanResourceApiController implements RosCleanResourceApi {
    private static final Logger log = LoggerFactory.getLogger(RosCleanResourceApiController.class);

    @Autowired
    private IRosCleanResourceService rosCleanResourceService;

    @Override
    public ResponseEntity<NotifyResp> cleanNotify(String moduleName, NotifyReq body) {
        return ResponseModel.success(rosCleanResourceService.cleanNotify(moduleName, body));
    }
}