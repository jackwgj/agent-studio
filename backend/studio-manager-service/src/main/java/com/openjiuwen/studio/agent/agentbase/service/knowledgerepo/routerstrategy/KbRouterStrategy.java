/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service.knowledgerepo.routerstrategy;

import com.openjiuwen.studio.agent.agentbase.entity.KbConnectionRouterEntity;

import java.util.List;

public interface KbRouterStrategy {

    String findConnectionId(List<KbConnectionRouterEntity> kbConnectionRouterEntities);

}
