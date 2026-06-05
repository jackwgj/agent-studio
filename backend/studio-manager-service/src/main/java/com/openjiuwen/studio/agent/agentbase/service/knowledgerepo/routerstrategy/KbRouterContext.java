/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service.knowledgerepo.routerstrategy;

import com.openjiuwen.studio.agent.foundation.base.exception.AgentBaseException;
import com.openjiuwen.studio.agent.foundation.base.exception.ErrorCode;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class KbRouterContext {

    @Autowired
    private Map<String, KbRouterStrategy> kbRouterStrategyMap;

    @Value("${knowledge.router-strategy:AverageRouterStrategy}")
    private String knowledgeRouterStrategy;

    /**
     * 获取 知识库路由策略
     *
     * @return KnowledgeRepoService 实现类
     */
    public KbRouterStrategy getRouterStrategyFromContext() {
        log.info("knowledge router strategy is [{}]", knowledgeRouterStrategy);
        KbRouterStrategy kbRouterStrategy = kbRouterStrategyMap.get(knowledgeRouterStrategy);
        if (StringUtils.isBlank(knowledgeRouterStrategy) || Objects.isNull(kbRouterStrategy)) {
            log.error("Fail to get knowledgeRouterStrategy, knowledgeRouterStrategy is [{}]", knowledgeRouterStrategy);
            throw new AgentBaseException(ErrorCode.INVALID_KNOWLEDGE_ROUTER_STRATEGY, knowledgeRouterStrategy);
        }
        log.info("successfully get knowledge router strategy is [{}]", kbRouterStrategy.getClass());
        return kbRouterStrategy;
    }

}
