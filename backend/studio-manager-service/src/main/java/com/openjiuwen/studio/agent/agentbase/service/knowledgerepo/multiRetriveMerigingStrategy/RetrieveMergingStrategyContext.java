/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service.knowledgerepo.multiRetriveMerigingStrategy;

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
public class RetrieveMergingStrategyContext {

    @Autowired
    private Map<String, RetrieveMergingStrategy> retrieveMergingStrategyMap;

    @Value("${knowledge.retrieve-merging-strategy:KooSearchScoreMergingStrategy}")
    private String retrieveMergingStrategy;

    /**
     * 获取 多知识库检索融合策略
     *
     * @return KnowledgeRepoService 实现类
     */
    public RetrieveMergingStrategy getRetrieveMergingStrategyFromContext() {
        log.info("retrieve merging strategy is [{}]", retrieveMergingStrategy);
        RetrieveMergingStrategy retrieveMergeStrategy = retrieveMergingStrategyMap.get(retrieveMergingStrategy);
        if (StringUtils.isBlank(retrieveMergingStrategy) || Objects.isNull(retrieveMergeStrategy)) {
            log.error("Fail to get retrieveMergingStrategy, retrieveMergingStrategy is [{}]", retrieveMergingStrategy);
            throw new AgentBaseException(ErrorCode.INVALID_KNOWLEDGE_MERGING_STRATEGY, retrieveMergeStrategy);
        }
        log.info("successfully get retrieve merging strategy is [{}]", retrieveMergeStrategy.getClass());
        return retrieveMergeStrategy;
    }

}
