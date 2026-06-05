/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.manager.service;

import com.openjiuwen.studio.agent.manager.dto.ModelInterfaceProtocolListRsp;
import com.openjiuwen.studio.agent.manager.dto.QueryMdInterfaceProtocolQo;

/**
 * ModelInterfaceProtocolMgmt service
 */

public interface IModelInterfaceProtocolMgmtService {

    /**
     * queryMdInterfaceProtocol
     *
     * @param projectId projectId
     * @param queryMdInterfaceProtocolQo queryMdInterfaceProtocolQo
     */
    ModelInterfaceProtocolListRsp queryMdInterfaceProtocol(String projectId,
        QueryMdInterfaceProtocolQo queryMdInterfaceProtocolQo);
}