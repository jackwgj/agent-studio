/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service;

import com.openjiuwen.studio.agent.manager.dto.NotifyReq;
import com.openjiuwen.studio.agent.manager.dto.NotifyResp;

/**
 * RosCleanResource service
 */

public interface IRosCleanResourceService {

    /**
     * cleanNotify
     *
     * @param moduleName moduleName
     * @param body body
     */
    NotifyResp cleanNotify(String moduleName, NotifyReq body);
}