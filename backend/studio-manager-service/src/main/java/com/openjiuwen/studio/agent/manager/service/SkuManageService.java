/* * Copyright (c) Huawei Technologies Co., Ltd. 2021-2025. All rights reserved. */

package com.openjiuwen.studio.agent.manager.service;

import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.manager.entity.Agent;
import com.openjiuwen.studio.agent.manager.entity.WorkflowEntity;
import com.openjiuwen.studio.agent.manager.mapper.AgentMapper;
import com.openjiuwen.studio.agent.manager.mapper.WorkflowMapper;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 添加类的描述
 *
 */
@Slf4j
@Service
public class SkuManageService {

    @Value("${common.sku.enable:true}")
    private boolean skuValidateEnable;

    @Value("${env.type}")
    private String envType;

    @Resource
    private AgentMapper agentMapper;

    @Resource
    private WorkflowMapper workflowMapper;

    public void validateExceededLimitOrNot(String attrCode, int currentCount) {
        if (!CommonConstant.EnvType.HC.equalsIgnoreCase(envType)) {
            return;
        }
        validateLimitInternal(attrCode, currentCount);
    }

    public void validateAttrEnable(String attrCode) {
        if (!skuValidateEnable) {
            return;
        }
        validateAttrInternal(attrCode);
    }

    public void validateInsertAppCountExceededLimitOrNot() {
        if (!CommonConstant.EnvType.HC.equalsIgnoreCase(envType)) {
            return;
        }
        validateInsertCountInternal();
    }

    private void validateLimitInternal(String attrCode, int currentCount) {
    }

    private void validateAttrInternal(String attrCode) {
    }

    private void validateInsertCountInternal() {
    }

    /**
     * 查询当前租户资源已用额度和总额度
     *
     */
    public List<Integer> queryAppCountExceededLimit() {
        return List.of(-1, -1);
    }

    public void validateImportAppCountExceededLimitOrNot(List<String> importAgentList,
        List<String> importWorkflowList, String targetWorkspaceId) {

        // 待扩展
    }

    public int calculateAppendAgentCount(List<String> importAgentList, String targeWorkspaceId) {
        if (CollectionUtils.isEmpty(importAgentList)) {
            return 0;
        }

        List<String> validImportAgentList = importAgentList.stream().filter(StringUtils::isNotBlank).toList();

        List<Agent> existAgentList = agentMapper.selectByIdsAndProjectIdAndWorkspaceId(
            RequestContextUtils.getRequestProjectId(), targeWorkspaceId, validImportAgentList);

        Set<String> existAgentIdSet = existAgentList.stream().map(Agent::getAgentId).collect(Collectors.toSet());

        List<String> appendAgentList =
            validImportAgentList.stream().filter(agentId -> !existAgentIdSet.contains(agentId)).toList();

        return appendAgentList.size();
    }

    public int calculateAppendWorkflowCount(List<String> importWorkflowList, String targetWorkspaceId) {
        if (CollectionUtils.isEmpty(importWorkflowList)) {
            return 0;
        }

        List<String> validImportWorkflowList = importWorkflowList.stream().filter(StringUtils::isNotBlank).toList();

        List<WorkflowEntity> workflowEntityList = workflowMapper.selectByWorkflowIdList(
            RequestContextUtils.getRequestProjectId(), targetWorkspaceId, validImportWorkflowList);

        Set<String> existWorkflowIdSet =
            workflowEntityList.stream().map(WorkflowEntity::getId).collect(Collectors.toSet());

        List<String> appendWorkflowList =
            validImportWorkflowList.stream().filter(id -> !existWorkflowIdSet.contains(id)).toList();

        return appendWorkflowList.size();
    }
}
