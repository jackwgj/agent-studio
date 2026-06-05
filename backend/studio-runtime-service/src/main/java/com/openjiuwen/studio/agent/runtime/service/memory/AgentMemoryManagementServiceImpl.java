/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.service.memory;

import com.openjiuwen.studio.agent.common.dto.knowledge.ListUserVariableMemoryResponseBody;
import com.openjiuwen.studio.agent.common.utils.LanguageUtils;
import com.openjiuwen.studio.agent.runtime.dto.BatchDeleteUserVariableMemoryRequestBody;
import com.openjiuwen.studio.agent.common.dto.BatchDeleteUserVariableMemoryResponseBody;
import com.openjiuwen.studio.agent.runtime.dto.ReleaseInfo;
import com.openjiuwen.studio.agent.common.dto.run.ResetUserVariableMemoryResponseBody;
import com.openjiuwen.studio.agent.common.dto.knowledge.UserVariableMemoryInfo;
import com.openjiuwen.studio.agent.runtime.model.AgentIrContext;
import com.openjiuwen.studio.agent.runtime.model.AgentIrConversationVariable;
import com.openjiuwen.studio.agent.runtime.model.AgentIrMetadata;
import com.openjiuwen.studio.agent.runtime.model.AgentIrWrapper;
import com.openjiuwen.studio.agent.runtime.rce.client.JiuWenClient;
import com.openjiuwen.studio.agent.runtime.rce.model.memory.RetrieveUserVariableMemoriesRequestBody;
import com.openjiuwen.studio.agent.runtime.rce.model.memory.RetrieveUserVariableMemoriesResponseBody;
import com.openjiuwen.studio.agent.runtime.rce.model.memory.UserVariableMemoryValue;
import com.openjiuwen.studio.agent.runtime.service.AgentIrCacheService;
import com.openjiuwen.studio.agent.runtime.service.AppReleaseService;
import com.openjiuwen.studio.agent.runtime.service.IAgentMemoryManagementService;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Agent运行过程中使用的记忆接口
 */
@Slf4j
@Service
public class AgentMemoryManagementServiceImpl implements IAgentMemoryManagementService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgentMemoryManagementServiceImpl.class);

    private final AgentIrCacheService irCacheService;

    private final AppReleaseService appReleaseService;

    private final JiuWenClient jiuWenClient;

    public AgentMemoryManagementServiceImpl(AgentIrCacheService irCacheService, AppReleaseService appReleaseService,
        JiuWenClient jiuWenClient) {
        this.irCacheService = irCacheService;
        this.appReleaseService = appReleaseService;
        this.jiuWenClient = jiuWenClient;
    }

    @Override
    public BatchDeleteUserVariableMemoryResponseBody batchDeleteUserVariableMemory(String projectId, String agentId,
        String workspaceId, BatchDeleteUserVariableMemoryRequestBody body) {
        LOGGER.info("operation log projectId: {}, userId:{}, batchDeleteUserVariableMemory", projectId,
            RequestContextUtils.getRequestUserId());
        // 先判断传入的agentId是不是agent发布之后的短编码
        String realAgentId = agentId;
        ReleaseInfo releaseInfo = appReleaseService.getReleaseInfo(agentId);
        String releasedAgentId = Optional.ofNullable(releaseInfo).map(ReleaseInfo::getAppId).orElse(null);
        if (StringUtils.isNotEmpty(releasedAgentId)) {
            realAgentId = releasedAgentId;
        }
        return jiuWenClient.batchDeleteUserVariableMemory(RequestContextUtils.getRequestAuthToken(),
                LanguageUtils.getLanguage(), projectId, realAgentId, RequestContextUtils.getRequestUserId(), body)
            .getBody();
    }

    @Override
    public ListUserVariableMemoryResponseBody listUserVariableMemory(String projectId, String agentId,
        String workspaceId) {
        // 先判断传入的agentId是不是agent发布之后的短编码
        String realAgentId = agentId;
        AgentIrWrapper irWrapper = null;
        ReleaseInfo releaseInfo = appReleaseService.getReleaseInfo(agentId);
        String releasedAgentId = Optional.ofNullable(releaseInfo).map(ReleaseInfo::getAppId).orElse(null);
        if (StringUtils.isNotEmpty(releasedAgentId)) {
            realAgentId = releasedAgentId;
            irWrapper = irCacheService.getPublishedAgentIr(realAgentId, releaseInfo.getVersionId());
        } else {
            irWrapper = irCacheService.getIrWrapperFromObs(realAgentId);
        }
        // 查询当前Agent中有哪些变量定义
        List<AgentIrConversationVariable> variables = Optional.ofNullable(irWrapper)
            .map(AgentIrWrapper::getContext)
            .map(AgentIrContext::getConversationVariables)
            .orElse(Collections.emptyList());

        if (CollectionUtils.isEmpty(variables)) {
            return new ListUserVariableMemoryResponseBody().setItems(Collections.emptyList());
        }
        // 查询用户这些变量的记忆值
        Date agentUpdateTime = Optional.ofNullable(irWrapper)
            .map(AgentIrWrapper::getMetadata)
            .map(AgentIrMetadata::getUpdatedAt)
            .orElse(new Date());
        List<UserVariableMemoryInfo> variablesResult = getUserVariableMemoryValues(projectId, realAgentId, variables,
            agentUpdateTime);
        return new ListUserVariableMemoryResponseBody().setItems(variablesResult);
    }

    private List<UserVariableMemoryInfo> getUserVariableMemoryValues(String projectId, String agentId,
        List<AgentIrConversationVariable> variables, Date agentUpdateTime) {
        List<String> variableKeys = variables.stream().map(AgentIrConversationVariable::getName).toList();
        RetrieveUserVariableMemoriesRequestBody requestBody = new RetrieveUserVariableMemoriesRequestBody();
        requestBody.setVariableKeys(variableKeys);
        RetrieveUserVariableMemoriesResponseBody response = jiuWenClient.retrieveUserVariableMemories(
            RequestContextUtils.getRequestAuthToken(), LanguageUtils.getLanguage(), projectId, agentId,
            RequestContextUtils.getRequestUserId(), requestBody).getBody();
        Map<String, UserVariableMemoryValue> variableMap = Optional.ofNullable(response)
            .map(RetrieveUserVariableMemoriesResponseBody::getVariables)
            .orElse(Collections.<UserVariableMemoryValue>emptyList())
            .stream()
            .collect(Collectors.toMap(UserVariableMemoryValue::getVariableKey, item -> item));

        return variables.stream().map(item -> {
            UserVariableMemoryInfo userVariableMemoryInfo = new UserVariableMemoryInfo();
            userVariableMemoryInfo.setVariableKey(item.getName());
            userVariableMemoryInfo.setVariableValue(variableMap.containsKey(item.getName())
                ? variableMap.get(item.getName()).getVariableValue()
                : item.getDefaultValue());
            // 九问针对记忆变量做了改造，当前已经不支持查看记忆的修改时间
            userVariableMemoryInfo.setUpdateTime(null);
            return userVariableMemoryInfo;
        }).toList();
    }

    @Override
    public ResetUserVariableMemoryResponseBody resetUserVariableMemory(String projectId, String agentId,
        String workspaceId) {
        LOGGER.info("operation log projectId: {}, userId:{}, resetUserVariableMemory", projectId,
            RequestContextUtils.getRequestUserId());
        // 先判断传入的agentId是不是agent发布之后的短编码
        String realAgentId = agentId;
        ReleaseInfo releaseInfo = appReleaseService.getReleaseInfo(agentId);
        String releasedAgentId = Optional.ofNullable(releaseInfo).map(ReleaseInfo::getAppId).orElse(null);
        if (StringUtils.isNotEmpty(releasedAgentId)) {
            realAgentId = releasedAgentId;
        }
        return jiuWenClient.resetUserVariableMemory(RequestContextUtils.getRequestAuthToken(),
            LanguageUtils.getLanguage(), projectId, realAgentId, RequestContextUtils.getRequestUserId()).getBody();
    }
}
