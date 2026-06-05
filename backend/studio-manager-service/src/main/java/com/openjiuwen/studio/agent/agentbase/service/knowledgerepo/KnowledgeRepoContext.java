/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.agentbase.service.knowledgerepo;

import com.openjiuwen.studio.agent.agentbase.common.constant.Constants;
import com.openjiuwen.studio.agent.common.enums.KnowledgeSourceEnum;
import com.openjiuwen.studio.agent.common.enums.RagAuthMode;
import com.openjiuwen.studio.agent.foundation.base.exception.AgentBaseException;
import com.openjiuwen.studio.agent.foundation.base.exception.ErrorCode;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

/**
 * 知识库服务实现类的Bean获取
 *
 * @since 2025-01-03
 */
@Slf4j
@Service
public class KnowledgeRepoContext {

    @Autowired
    private Map<String, KnowledgeRepoService> knowledgeRepoServiceMap;

    @Value("${knowledge.source}")
    private String knowledgeSource;

    @Value("${knowledge.lakeSearch.auth-mode}")
    private String authMode;

    /**
     * 获取 KnowledgeRepoService 实现类
     *
     * @param source 知识库来源
     * @return KnowledgeRepoService 实现类
     */
    public KnowledgeRepoService getService(String source) {
        String serviceName = getServiceName(source);
        log.info("knowledge repo serviceName is [{}]", serviceName);
        KnowledgeRepoService knowledgeRepoService = knowledgeRepoServiceMap.get("Mg" + serviceName);
        if (StringUtils.isBlank(serviceName) || Objects.isNull(knowledgeRepoService)) {
            log.error("Fail to get KnowledgeRepoService, source [{}], serviceName [{}]", source, serviceName);
            throw new AgentBaseException(ErrorCode.INVALID_KNOWLEDGE_REPO_SOURCE);
        }
        return knowledgeRepoService;
    }

    private String getServiceName(String source) {
        String realSource = StringUtils.isBlank(source) ? knowledgeSource : source;

        // CUSTOM使用LakeSearch知识库，对应MgLakeService实现类
        if (KnowledgeSourceEnum.CUSTOM.toString().equals(realSource)) {
            return KnowledgeSourceEnum.LAKESEARCH.toString();
        }

        // 使用kerberos认证对接LakeSearch，对应MgKerLakeSearch实现类
        if (KnowledgeSourceEnum.LAKESEARCH.toString().equals(realSource) && RagAuthMode.KERBEROS.toString()
            .equalsIgnoreCase(authMode)) {
            return Constants.LAKE_SEARCH_SERVICE_NAME_WITH_KERBEROS;
        }
        return realSource;
    }
}
