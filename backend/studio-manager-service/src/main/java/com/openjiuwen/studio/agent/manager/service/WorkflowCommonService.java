/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.openjiuwen.studio.agent.common.constant.Constants;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.manager.dto.WorkflowVO;
import com.openjiuwen.studio.agent.manager.entity.WorkflowEntity;
import com.openjiuwen.studio.agent.manager.mapper.HistoryWorkflowMapper;
import com.openjiuwen.studio.agent.manager.mapper.WorkflowMapper;
import com.openjiuwen.studio.agent.manager.obs.MgObsService;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * 工作流service
 *
 */
@Service
@Slf4j
public class WorkflowCommonService {

    @Value("${op.svc.project-id}")
    private String opSvcProjectId;

    @Autowired
    private WorkflowMapper workflowMapper;

    @Autowired
    private HistoryWorkflowMapper historyWorkflowMapper;

    @Autowired
    IrAdapterService irAdapterService;

    @Autowired
    MgObsService mgObsService;


    /**
     * 根据工作流id查询指定空间和承载租户下的工作流
     *
     * @param projectId 项目id
     * @param workspaceId 空间id
     * @param workflowId 工作流id
     * @return 返回工作流
     */
    public WorkflowEntity getWorkflowByWorkspaceAndOpProject(String projectId, String workspaceId, String workflowId) {
        return workflowMapper.getWorkflowEntity(projectId, opSvcProjectId, workspaceId, workflowId);
    }

    @Transactional
    public void softDelete(WorkflowEntity workflowEntity) {
        historyWorkflowMapper.insert(workflowEntity);
        workflowMapper.deleteByPrimaryKey(workflowEntity.getProjectId(), workflowEntity.getId());
    }

    /**
     * 工作流dsl转ir
     *
     * @param workflowId 工作流Id
     * @param workflowEntity 工作流元数据
     * @param workflowVo 工作流dsl对象
     * @return 文件路径
     */
    public String workflowDslToIr(String workflowId, WorkflowEntity workflowEntity, WorkflowVO workflowVo,
        String versionId) {
        Map<String, Object> metadata = parseMetadata(workflowEntity);
        Map<String, Object> irInfo = irAdapterService.adaptWorkflow(workflowVo, metadata);
        String irObsKey = workflowId;
        if (!StringUtils.isEmpty(versionId)) {
            irObsKey = workflowId + Constants.UNDERLINE_STR + versionId;
        }
        return mgObsService.uploadObsFile(workflowId, irObsKey, CommonConstant.WORKFLOW,
            JSON.toJSONString(irInfo, JSONWriter.Feature.WriteMapNullValue), CommonConstant.Workflow.IR);
    }

    /**
     * 解析metadata字段存在Ir，用于执行时校验
     *
     * @param workflowEntity 工作流信息
     * @return metadata字段
     */
    public Map<String, Object> parseMetadata(WorkflowEntity workflowEntity) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("userId", workflowEntity.getCreatorId());
        metadata.put("projectId", workflowEntity.getProjectId());
        metadata.put("workspaceId", workflowEntity.getWorkspaceId());
        metadata.put("domainId", workflowEntity.getDomainId());
        metadata.put("updatedAt", workflowEntity.getUpdatedAt());
        metadata.put("publishedAt", workflowEntity.getPublishedAt());
        metadata.put("visibility", workflowEntity.getVisibility());
        metadata.put("status", workflowEntity.getStatus());
        metadata.put("type", workflowEntity.getWorkflowType());
        return metadata;
    }

}
