/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.foundation.connection;

import com.openjiuwen.studio.agent.foundation.connection.model.ExternalKnowledgeBaseInfo;
import com.openjiuwen.studio.agent.foundation.connection.model.ExternalRetrieveResultInfo;
import com.openjiuwen.studio.agent.foundation.connection.model.KnowledgeBaseTagInfo;
import com.openjiuwen.studio.agent.foundation.connection.model.PageResult;
import com.openjiuwen.studio.agent.foundation.connection.model.RetrieveKnowledgeBaseReq;

import java.util.List;

/**
 * 功能描述
 *
 * @since 2025-08-22
 */
public interface IKnowledgeBaseConnector {

    /**
     * 查询知识库列表
     *
     * @param name
     * @param offset
     * @param limit
     * @return
     */
    PageResult<ExternalKnowledgeBaseInfo> listKnowledgeBase(ConnectorDefinition connectorDefinition, String name,
        Integer offset, Integer limit);

    /**
     * 知识检索
     *
     * @param request
     * @param offset
     * @param limit
     * @return
     */
    PageResult<ExternalRetrieveResultInfo> retrieveKnowledgeBase(ConnectorDefinition connectorDefinition,
        RetrieveKnowledgeBaseReq request, Integer offset, Integer limit);

    /**
     * 获取知识库下的标签列表
     *
     * @param knowledgeBaseId
     * @return
     */
    List<KnowledgeBaseTagInfo> listKnowledgeBaseTags(ConnectorDefinition connectorDefinition, String knowledgeBaseId);

}
