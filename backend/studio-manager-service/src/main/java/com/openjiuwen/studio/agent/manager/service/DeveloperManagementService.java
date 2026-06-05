/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service;

import com.alibaba.fastjson.JSONObject;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.manager.dto.DeveloperAgentInfo;
import com.openjiuwen.studio.agent.manager.dto.DeveloperAgentListRsp;
import com.openjiuwen.studio.agent.manager.dto.DeveloperSpacePage;
import com.openjiuwen.studio.agent.manager.dto.ListDeveloperAgentsQo;
import com.openjiuwen.studio.agent.manager.dto.SearchCriteria;
import com.openjiuwen.studio.agent.manager.dto.iam.DeveloperHeaderUserInfo;
import com.openjiuwen.studio.agent.manager.entity.Agent;
import com.openjiuwen.studio.agent.manager.entity.WorkspaceEntity;
import com.openjiuwen.studio.agent.manager.enums.AgentType;
import com.openjiuwen.studio.agent.manager.mapper.AgentMapper;
import com.openjiuwen.studio.agent.manager.service.workspace.WorkspaceService;
import com.openjiuwen.studio.prompt.engineering.utils.HttpUtil;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 功能描述 开发者空间管理Service
 *
 */
@Slf4j
@Service
public class DeveloperManagementService implements ITenantOpenApiService {
    private static final String ASSET_TYPE_NAME = "ai-agent";

    private static final String ASSET_TYPE_DISPLAY_NAME_CN = "资产中心agent";

    private static final String ASSET_TYPE_DISPLAY_NAME_EN = "Asset Center AI Agent";

    @Autowired
    private WorkspaceService workspaceService;

    @Autowired
    private AgentMapper agentMapper;

    /**
     * 判断当前用户是否有已发布的agent
     *
     */
    @Override
    public Boolean existPublishedAgent(String domainId, Long publishedDate) {
        if (StringUtils.isBlank(domainId)) {
            return false;
        }
        Timestamp now = new Timestamp(publishedDate);
        return agentMapper.selectByDomainId(domainId, CommonConstant.AGENT_PUBLISHED, now) > 0;
    }

    @Override
    public DeveloperAgentListRsp listDeveloperAgents(String xUserInfo, ListDeveloperAgentsQo listDeveloperAgentsQo) {
        log.info("begin listDeveloperAgents, xUserInfo: {}, listDeveloperAgentsQo: {}", xUserInfo, listDeveloperAgentsQo);
        DeveloperHeaderUserInfo userInfoHeader = JSONObject.parseObject(xUserInfo, DeveloperHeaderUserInfo.class);
        checkDeveloperIamInfo(userInfoHeader);
        String workspaceId;
        if (StringUtils.isEmpty(listDeveloperAgentsQo.getWorkspaceId())) {
            WorkspaceEntity workspaceInfo = workspaceService.getPersonalWorkspaceInfo(
                userInfoHeader.getIamUserInfo().getProjectId(), userInfoHeader.getIamUserInfo().getUserId());
            workspaceId = workspaceInfo == null ? null : workspaceInfo.getId();
        } else {
            workspaceId = listDeveloperAgentsQo.getWorkspaceId();
        }
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setWorkspaceId(workspaceId);
        searchCriteria.setUserId(userInfoHeader.getIamUserInfo().getUserId());

        List<Agent> agentList = agentMapper
                .selectByProjectIdAndSearchCriteria(userInfoHeader.getIamUserInfo().getProjectId(), searchCriteria);
        log.info("get agent list by workspaceId: {}, agent count is {}", workspaceId, agentList.size());
        int from = listDeveloperAgentsQo.getMarker() * listDeveloperAgentsQo.getLimit();
        int offset = from + listDeveloperAgentsQo.getLimit();
        offset = Math.min(offset, agentList.size());
        if (from > agentList.size()) {
            from = agentList.size();
        }
        List<Agent> res;
        if (offset == 0) {
            res = agentList;
        } else {
            res = agentList.subList(from, offset);
        }
        // 拼接返回值
        return getTenantAgentResponse(res, agentList.size(), listDeveloperAgentsQo.getLimit(), offset,
                listDeveloperAgentsQo.getMarker());
    }

    private void checkDeveloperIamInfo(DeveloperHeaderUserInfo userInfoHeader) {
        if (ObjectUtils.isEmpty(userInfoHeader) || ObjectUtils.isEmpty(userInfoHeader.getIamUserInfo())) {
            log.error("hearder iam user info is null!");
            throw new AgentStudioException(StudioError.POC_AUTH_FAILED);
        }
        if (!Strings.CS.equals(userInfoHeader.getIamUserInfo().getUserId(), RequestContextUtils.getRequestUserId())
                || !Strings.CS.equals(userInfoHeader.getIamUserInfo().getDomainId(),
                RequestContextUtils.getRequestUserDomainId())
                || !Strings.CS.equals(userInfoHeader.getIamUserInfo().getProjectId(),
                RequestContextUtils.getRequestProjectId())) {
            log.error("hearder iam user info and token not match!");
            throw new AgentStudioException(StudioError.POC_AUTH_FAILED);
        }
    }

    private DeveloperAgentListRsp getTenantAgentResponse(List<Agent> res, int total, int pageSize, int offset,
                                                         int currentPage) {
        List<DeveloperAgentInfo> developerAgentInfoList = new ArrayList<>();
        res.forEach(agent -> {
            DeveloperAgentInfo developerAgentInfo = new DeveloperAgentInfo();
            developerAgentInfo.setName(agent.getName());
            developerAgentInfo.setAssetId(agent.getAgentId());
            developerAgentInfo.setAssetTypeName(ASSET_TYPE_NAME);
            if (Strings.CI.equals(HttpUtil.getLanguage(), "en-US")) {
                developerAgentInfo.setAssetTypeDisplayName(ASSET_TYPE_DISPLAY_NAME_EN);
            } else {
                developerAgentInfo.setAssetTypeDisplayName(ASSET_TYPE_DISPLAY_NAME_CN);
            }

            developerAgentInfo.setDescription(agent.getDescription());
            developerAgentInfo.setCreatedBy(agent.getCreatorId());
            if (!ObjectUtils.isEmpty(agent.getCreatedOn())) {
                developerAgentInfo.setCreateTime(agent.getCreatedOn().getTime());
            }
            developerAgentInfo.setUpdatedBy(agent.getCreatorId());
            if (!ObjectUtils.isEmpty(agent.getUpdatedOn())) {
                developerAgentInfo.setUpdateTime(agent.getUpdatedOn().getTime());
            }
            Map<String, Object> properties = new HashMap<>();
            properties.put("logo", agent.getIcon());
            if (Strings.CS.equals(agent.getType(), AgentType.AGENT.getValue())) {
                properties.put("asset_uri", "app-agent/detail");
                properties.put("asset_id_param", "agentId");
                properties.put("asset_other_param", "");
            } else {
                properties.put("asset_uri", "app-flow/flow");
                properties.put("asset_id_param", "id");
                properties.put("asset_other_param", "&type=multi");
            }
            developerAgentInfo.setProperties(properties);
            developerAgentInfoList.add(developerAgentInfo);
        });
        DeveloperAgentListRsp response = new DeveloperAgentListRsp();
        response.setData(developerAgentInfoList);
        DeveloperSpacePage page = new DeveloperSpacePage();
        Integer next_marker = null;
        if (total > offset) {
            next_marker = currentPage + 1;
        }
        page.setNextMarker(String.valueOf(next_marker));

        if (currentPage == 0) {
            page.setPreviousMarker(null);
        } else {
            page.setPreviousMarker(String.valueOf(currentPage - 1));
        }
        page.setCurrentCount(Math.min(res.size(), pageSize));
        response.setPageInfo(page);
        return response;
    }
}
