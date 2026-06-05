/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */


package com.openjiuwen.studio.agent.runtime.controller;

import com.openjiuwen.studio.agent.common.utils.ResponseModel;
import com.openjiuwen.studio.agent.common.dto.analytics.AnalyticsEventReq;
import com.openjiuwen.studio.agent.common.dto.analytics.AnalyticsEventResp;
import com.openjiuwen.studio.agent.runtime.service.IAnalyticsEventService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;


/**
 * AnalyticsEvent controller
 */
@RestController

public class AnalyticsEventApiController implements AnalyticsEventApi {
    private static final Logger log = LoggerFactory.getLogger(AnalyticsEventApiController.class);

    @Autowired
    private IAnalyticsEventService analyticsEventService;

    @Override
    public ResponseEntity<AnalyticsEventResp> analyticsEvent(String projectId, String agentId, AnalyticsEventReq body) {
        return ResponseModel.success(analyticsEventService.analyticsEvent(projectId, agentId, body));
    }
}