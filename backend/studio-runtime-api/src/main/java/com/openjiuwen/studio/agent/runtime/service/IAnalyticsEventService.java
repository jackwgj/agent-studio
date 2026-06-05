/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */


package com.openjiuwen.studio.agent.runtime.service;

import com.openjiuwen.studio.agent.common.dto.analytics.AnalyticsEventReq;
import com.openjiuwen.studio.agent.common.dto.analytics.AnalyticsEventResp;


/**
 * AnalyticsEvent service
 */

public interface IAnalyticsEventService {

    /**
     * analyticsEvent
     *
     * @param projectId projectId
     * @param agentId agentId
     * @param body body
     */
    AnalyticsEventResp analyticsEvent(String projectId, String agentId, AnalyticsEventReq body) ;
}