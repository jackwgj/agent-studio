/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.prompt.engineering.service;

import com.openjiuwen.studio.prompt.engineering.dto.CreatePromptResp;
import com.openjiuwen.studio.prompt.engineering.dto.GetPromptListsQo;
import com.openjiuwen.studio.prompt.engineering.dto.PePromptNewListVo;
import com.openjiuwen.studio.prompt.engineering.dto.PePromptTemplateNewVo;
import com.openjiuwen.studio.prompt.engineering.dto.QueryCustomPromptApiActionsQo;

/**
 * PromptApi service
 */

public interface IPromptApiService {

    /**
     * createCustomPromptApiAction
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     * @param body body
     */
    String createCustomPromptApiAction(String projectId, String workspaceId, PePromptTemplateNewVo body);

    /**
     * getPromptLists
     *
     * @param projectId projectId
     * @param getPromptListsQo getPromptListsQo
     */
    PePromptNewListVo getPromptLists(String projectId, GetPromptListsQo getPromptListsQo);

    /**
     * queryCustomPromptApiActions
     *
     * @param projectId projectId
     * @param promptId promptId
     * @param queryCustomPromptApiActionsQo queryCustomPromptApiActionsQo
     */
    PePromptTemplateNewVo queryCustomPromptApiActions(String projectId, String promptId, QueryCustomPromptApiActionsQo queryCustomPromptApiActionsQo);

    /**
     * savePromptTemplate
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     * @param body body
     */
    CreatePromptResp savePromptTemplate(String projectId, String workspaceId, PePromptTemplateNewVo body);

    /**
     * updateCustomPromptApiAction
     *
     * @param projectId projectId
     * @param workspaceId workspaceId
     * @param body body
     */
    CreatePromptResp updateCustomPromptApiAction(String projectId, String workspaceId, PePromptTemplateNewVo body);
}
