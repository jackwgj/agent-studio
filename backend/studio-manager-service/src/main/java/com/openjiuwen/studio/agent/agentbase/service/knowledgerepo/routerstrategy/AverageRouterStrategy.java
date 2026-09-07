/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service.knowledgerepo.routerstrategy;

import com.openjiuwen.studio.agent.agentbase.entity.KbConnectionRouterEntity;

import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * 平均分配策略
 */
@Service("AverageRouterStrategy")
public class AverageRouterStrategy implements KbRouterStrategy {

    @Override
    public String findConnectionId(List<KbConnectionRouterEntity> kbConnectionRouterEntities) {
        kbConnectionRouterEntities.sort(Comparator.comparing(KbConnectionRouterEntity::getUsageRatio));
        return Optional.ofNullable(kbConnectionRouterEntities.get(0).getKnowledgeBaseConnectionId()).orElse("");
    }

}
