/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service;

import static com.openjiuwen.studio.agent.manager.constant.CommonConstant.Workflow.NODE_START;

import com.alibaba.fastjson2.JSON;
import com.openjiuwen.studio.agent.common.constant.Constants;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.manager.dto.AgentInfo;
import com.openjiuwen.studio.agent.manager.dto.ReleaseAppInfo;
import com.openjiuwen.studio.agent.manager.dto.RetrieveReleaseAppInfoQo;
import com.openjiuwen.studio.agent.manager.dto.WorkflowNodeVO;
import com.openjiuwen.studio.agent.manager.dto.WorkflowVO;
import com.openjiuwen.studio.agent.manager.entity.ReleaseChannel;
import com.openjiuwen.studio.agent.manager.entity.ReleaseVersion;
import com.openjiuwen.studio.agent.manager.entity.WorkflowEntity;
import com.openjiuwen.studio.agent.manager.mapper.ReleaseChannelMapper;
import com.openjiuwen.studio.agent.manager.mapper.ReleaseVersionMapper;
import com.openjiuwen.studio.agent.manager.mapper.WorkflowMapper;
import com.openjiuwen.studio.agent.manager.obs.MgObsService;

import com.openjiuwen.studio.agent.manager.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 功能描述 发布管理接口类
 *
 */
@Slf4j
@Service
public class ReleaseManagementService implements IReleaseManagementService {
    private static final String PROLOGUE = "prologue";

    private static final String SUGGEST_QUERIES = "suggest_queries";

    private final ReleaseVersionMapper releaseVersionMapper;

    private final ReleaseChannelMapper releaseChannelMapper;

    private final MgObsService mgObsService;

    private final WorkflowMapper workflowMapper;

    /**
     * 构造方法
     */
    public ReleaseManagementService(ReleaseVersionMapper releaseVersionMapper,
        ReleaseChannelMapper releaseChannelMapper, MgObsService mgObsService, WorkflowMapper workflowMapper) {
        this.releaseVersionMapper = releaseVersionMapper;
        this.releaseChannelMapper = releaseChannelMapper;
        this.mgObsService = mgObsService;
        this.workflowMapper = workflowMapper;
    }

    @Override
    public ReleaseAppInfo retrieveReleaseAppInfo(String projectId, String releaseId,
        RetrieveReleaseAppInfoQo retrieveReleaseAppInfoQo) {
        String channelId = null;
        String shortCode = null;
        if (retrieveReleaseAppInfoQo.getFindBy().equals("channel_id")) {
            channelId = releaseId;
        } else if (retrieveReleaseAppInfoQo.getFindBy().equals("short_code")) {
            shortCode = releaseId;
        }
        ReleaseChannel channel = releaseChannelMapper.selectByChannelIdOrShortCode(projectId, channelId, shortCode,
            retrieveReleaseAppInfoQo.getChannelType());
        if (channel == null) {
            log.error("release channel does not exist, projectId = {}, channelType = {}, releaseId = {}", projectId,
                retrieveReleaseAppInfoQo.getChannelType(), releaseId);
            throw new AgentStudioException(StudioError.APPLICATION_PUBLISH_CHANNEL_NOT_EXIST);
        }
        ReleaseVersion releaseVersion =
            releaseVersionMapper.selectByAppIdAndVersionId(channel.getAppId(), channel.getVersionId());
        if (releaseVersion == null) {
            throw new AgentStudioException(StudioError.AGENT_VERSION_NOT_EXIST);
        }
        return buildAppInfo(channel, releaseVersion);
    }

    private ReleaseAppInfo buildAppInfo(ReleaseChannel channel, ReleaseVersion releaseVersion) {
        ReleaseAppInfo appInfo = new ReleaseAppInfo();
        appInfo.setVersionId(releaseVersion.getVersionId());
        appInfo.setAppId(releaseVersion.getAppId());
        appInfo.setAppType(releaseVersion.getAppType());

        if (CommonConstant.WORKFLOW.equals(releaseVersion.getAppType())) {
            handleWorkflowApp(channel, appInfo);
        } else if (CommonConstant.AGENT.equals(releaseVersion.getAppType())) {
            handleAgentApp(releaseVersion, appInfo);
        }
        return appInfo;
    }

    private void handleWorkflowApp(ReleaseChannel channel, ReleaseAppInfo appInfo) {
        WorkflowEntity workflowEntity = workflowMapper.getWorkflowById(channel.getAppId());
        WorkflowVO workflowVO =
            JSON.parseObject(mgObsService.downloadObsFile(workflowEntity.getDslPath()), WorkflowVO.class);

        appInfo.setName(workflowEntity.getName());
        appInfo.setDescription(workflowVO.getDescription());
        appInfo.setIcon(workflowEntity.getAvatar());
        if (workflowVO.getConfigs() != null) {
            appInfo.setPrologue(workflowVO.getConfigs().getOrDefault(PROLOGUE, StringUtils.EMPTY).toString());
            Object suggestQueriesObj = workflowVO.getConfigs().getOrDefault(SUGGEST_QUERIES, StringUtils.EMPTY);
            if (suggestQueriesObj instanceof List<?>) {
                List<String> suggestList =
                    ((List<?>) suggestQueriesObj).stream().map(Object::toString).collect(Collectors.toList());
                appInfo.setSuggestQueries(suggestList);
            }
            Map<String, String> memoryConfig = JsonUtils.objectToClass(workflowVO.getConfigs().get(Constants.Workflow.MEMORY_CONFIG));
            if (ObjectUtils.isNotEmpty(memoryConfig)) {
                appInfo.setMemoryRepoId(
                    memoryConfig.getOrDefault(CommonConstant.MEMORY_REPO_ID, StringUtils.EMPTY));
            }
        } else {
            appInfo.setPrologue(StringUtils.EMPTY);
            appInfo.setSuggestQueries(Collections.emptyList());
        }
        appInfo.setWorkflowType(workflowEntity.getWorkflowType());
        appInfo.setNodes(fetchStartNode(workflowVO.getNodes()));
    }

    private List<WorkflowNodeVO> fetchStartNode(List<WorkflowNodeVO> nodes) {
        return nodes.stream().filter(node -> NODE_START.equals(node.getId())).collect(Collectors.toList());
    }

    private void handleAgentApp(ReleaseVersion releaseVersion, ReleaseAppInfo appInfo) {
        String agentDslJson = mgObsService.downloadObsFile(releaseVersion.getDslPath());
        AgentInfo agentInfo = JSON.parseObject(agentDslJson, AgentInfo.class);

        // 构造agent app响应体
        appInfo.setName(agentInfo.getName());
        appInfo.setDescription(agentInfo.getDescription());
        appInfo.setIcon(agentInfo.getIcon());
        appInfo.setPrologue(agentInfo.getPrologue());
        appInfo.setSuggestQueries(agentInfo.getSuggestQueries());
        appInfo.setVoiceInteraction(agentInfo.getVoiceInteraction());
        appInfo.setSubType(releaseVersion.getSubType());
        if (agentInfo.getMemoryConfig() != null) {
            appInfo.setMemoryRepoId(agentInfo.getMemoryConfig().getMemoryRepoId());
        }
    }
}
