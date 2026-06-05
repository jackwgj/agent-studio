/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.service.knowledgerepo;

import com.google.common.collect.ImmutableMap;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.runtime.enums.ConnectorTypeEnum;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class KnowledgeRepoServiceFactory implements InitializingBean {

    @Autowired
    private List<KnowledgeRepoService> knowledgeRepoServiceList;

    private Map<ConnectorTypeEnum, KnowledgeRepoService> knowledgeRepoServiceMap;

    @Value("${knowledge.source}")
    private String knowledgeSource;

    @Value("${knowledge.lakeSearch.auth-mode}")
    private String authMode;

    @Override
    public void afterPropertiesSet() {
        if (knowledgeRepoServiceList == null || knowledgeRepoServiceList.isEmpty()) {
            this.knowledgeRepoServiceMap = ImmutableMap.of();
        } else {
            this.knowledgeRepoServiceMap = knowledgeRepoServiceList.stream()
                .collect(Collectors.toMap(KnowledgeRepoService::type, Function.identity()));
        }
    }

    public KnowledgeRepoService chooseKnowledgeService(ConnectorTypeEnum connectorType) {
        KnowledgeRepoService knowledgeRepoService = this.knowledgeRepoServiceMap.get(connectorType);
        if (knowledgeRepoService == null) {
            throw new AgentStudioException(StudioError.PARAMS_IS_REQUIRED);
        }
        return knowledgeRepoService;
    }

}