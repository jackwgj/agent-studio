/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */


package com.openjiuwen.studio.agent.runtime.service;

import com.openjiuwen.studio.agent.common.dto.agent.ConversionQueries;
import com.openjiuwen.studio.agent.common.dto.agent.ExecutionInfo;
import com.openjiuwen.studio.agent.common.dto.agent.Status;
import com.openjiuwen.studio.agent.common.dto.run.AdditionalQuestionsWorkflowReq;
import com.openjiuwen.studio.agent.common.dto.run.ListConversationQueriesQo;
import com.openjiuwen.studio.agent.runtime.dto.AutoAddResultJsonObject;
import com.openjiuwen.studio.agent.common.dto.ExecutionQueries;
import com.openjiuwen.studio.agent.common.dto.run.GetExecutionInsightQo;
import com.openjiuwen.studio.agent.common.dto.run.ListExecutionQueriesQo;


/**
 * WorkflowRuntime service
 */

public interface IWorkflowRuntimeService {

    /**
     * abortConversation
     *
     * @param projectId projectId
     * @param workflowId workflowId
     * @param conversationId conversationId
     */
    Status abortConversation(String projectId, String workflowId, String conversationId) ;

    /**
     * additionalQuestionsWorkflow
     *
     * @param projectId projectId
     * @param workflowId workflowId
     * @param conversationId conversationId
     * @param workspaceId workspaceId
     * @param body body
     */
    AutoAddResultJsonObject additionalQuestionsWorkflow(String projectId, String workflowId, String conversationId, String workspaceId, AdditionalQuestionsWorkflowReq body) ;

    /**
     * getExecutionInsight
     *
     * @param projectId projectId
     * @param workflowId workflowId
     * @param executionId executionId
     * @param getExecutionInsightQo getExecutionInsightQo
     */
    ExecutionInfo getExecutionInsight(String projectId, String workflowId, String executionId, GetExecutionInsightQo getExecutionInsightQo) ;

    /**
     * listConversationQueries
     *
     * @param projectId projectId
     * @param workflowId workflowId
     * @param listConversationQueriesQo listConversationQueriesQo
     */
    ConversionQueries listConversationQueries(String projectId, String workflowId, ListConversationQueriesQo listConversationQueriesQo) ;

    /**
     * listExecutionQueries
     *
     * @param projectId projectId
     * @param workflowId workflowId
     * @param conversationId conversationId
     * @param listExecutionQueriesQo listExecutionQueriesQo
     */
    ExecutionQueries listExecutionQueries(String projectId, String workflowId, String conversationId, ListExecutionQueriesQo listExecutionQueriesQo) ;
}