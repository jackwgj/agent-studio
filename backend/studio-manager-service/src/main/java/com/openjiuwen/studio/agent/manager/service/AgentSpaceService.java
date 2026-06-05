/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.service;

import com.alibaba.fastjson2.JSONObject;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.common.utils.RequestContextUtils;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.manager.entity.ReleaseChannel;
import com.openjiuwen.studio.agent.manager.rce.client.AgentSpaceClient;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AgentSpaceService {
    /**
     * workflow发布状态
     */
    private static final String RELEASED = "released";

    @Autowired
    private AgentSpaceClient agentSpaceClient;

    /**
     * 查询发布状态
     *
     * @param workspaceId 工作空间id
     * @param applicationId 应用id
     * @return ReleaseChannel
     */
    ReleaseChannel publishQuery(String workspaceId, String applicationId) {
        ResponseEntity<JSONObject> response = agentSpaceClient.publish(RequestContextUtils.getRequestAuthToken(),
            workspaceId, applicationId);
        if (ObjectUtils.isEmpty(response) || ObjectUtils.isEmpty(response.getBody().getJSONObject("data"))
            || ObjectUtils.isEmpty(response.getBody().getJSONObject("data").getBoolean("publish_status"))) {
            log.info("Query [{}] publish status from agent space service failed.", applicationId);
            throw new AgentStudioException(StudioError.UNEXPECTED_ERROR);
        }
        if (!response.getBody().getJSONObject("data").getBoolean("publish_status")) {
            return null;
        }
        ReleaseChannel releaseChannel = new ReleaseChannel();
        releaseChannel.setId(applicationId);
        releaseChannel.setAppId(applicationId);
        releaseChannel.setWorkspaceId(workspaceId);
        releaseChannel.setChannelType(CommonConstant.AGENT_BUILDER);
        releaseChannel.setStatus(RELEASED);
        releaseChannel.setUrl(response.getBody().getJSONObject("data").getString("request_url"));
        releaseChannel.setReleasedOn(response.getBody().getJSONObject("data").getDate("created_date"));
        return releaseChannel;
    }

    /**
     * 发布
     *
     * @param body 发布请求body体
     */
    void publish(ReleaseChannel body) {
        ResponseEntity<JSONObject> response = agentSpaceClient.publish(RequestContextUtils.getRequestAuthToken(), body);
        if (ObjectUtils.isEmpty(response.getBody()) || !Strings.CS.equals(response.getBody().getString("data"),
            "success")) {
            log.error("Publish [{}] to agent space service failed", body.getId());
            throw new AgentStudioException(StudioError.UNEXPECTED_ERROR);
        }
        log.info("Publish [{}] to agent space service success", body.getId());
    }

    /**
     * 取消发布
     *
     * @param workspaceId 工作空间id
     * @param applicationId 应用id
     */
    void unpublish(String workspaceId, String applicationId) {
        ResponseEntity<JSONObject> response = agentSpaceClient.unpublish(RequestContextUtils.getRequestAuthToken(),
            workspaceId, applicationId);
        if (ObjectUtils.isEmpty(response.getBody()) || !Strings.CS.equals(response.getBody().getString("data"),
            "success")) {
            log.error("Unpublish [{}] to agent space service failed", applicationId);
            throw new AgentStudioException(StudioError.UNEXPECTED_ERROR);
        }
        log.info("Unpublish [{}] to agent space service success", applicationId);
    }

    /**
     * 应用删除，联动取消发布
     *
     * @param workspaceId 工作空间id
     * @param applicationId 应用id
     */
    void delete(String workspaceId, String applicationId) {
        if (publishQuery(workspaceId, applicationId) == null) {
            return;
        }
        unpublish(workspaceId, applicationId);
    }
}
