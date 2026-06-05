/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.app.builder;

import com.openjiuwen.studio.agent.space.api.auth.AccessKeyAuth;
import com.openjiuwen.studio.agent.space.api.controller.AgentBuilderOpenApi;
import com.openjiuwen.studio.agent.space.api.vo.request.AgentBuilderChatReq;
import com.openjiuwen.studio.agent.space.api.vo.request.AgentBuilderCreateTaskReq;
import com.openjiuwen.studio.agent.space.api.vo.request.AgentBuilderMessageListReq;
import com.openjiuwen.studio.agent.space.api.vo.request.AgentBuilderOperateTaskReq;
import com.openjiuwen.studio.agent.space.api.vo.request.AgentBuilderQueryTaskReq;
import com.openjiuwen.studio.agent.space.api.vo.response.AgentBuilderTaskListRsp;
import com.openjiuwen.studio.agent.space.api.vo.response.deepresearch.DRMessageVo;
import com.openjiuwen.studio.agent.space.app.annotation.operatelog.OperateLog;
import com.openjiuwen.studio.agent.space.app.annotation.operatelog.OperateType;
import com.openjiuwen.studio.agent.space.app.service.builder.AgentBuilderService;
import com.openjiuwen.studio.agent.space.common.model.BaseResp;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Slf4j
@RestController
public class AgentBuilderOpenApiController implements AgentBuilderOpenApi {
    @Resource
    private AgentBuilderService AgentBuilderService;

    @Override
    @AccessKeyAuth
    @OperateLog(objectType = "createTask", operateType = OperateType.CREATE, idField = "task_id")
    public BaseResp<String> createTask(AgentBuilderCreateTaskReq req) {
        log.info("AgentBuilderOpenApiController.createTask start");
        return BaseResp.success(AgentBuilderService.createTask(req));
    }

    @Override
    @AccessKeyAuth
    public BaseResp<AgentBuilderTaskListRsp> listTask(AgentBuilderQueryTaskReq req) {
        log.info("AgentBuilderOpenApiController.listTask start");
        return BaseResp.success(AgentBuilderService.listTask(req));
    }

    @Override
    @AccessKeyAuth
    @OperateLog(objectType = "chat", operateType = OperateType.CHAT, idField = "task_id")
    public SseEmitter chat(String workspaceId, AgentBuilderChatReq req) {
        log.info("AgentBuilderOpenApiController.chat start. taskId = {}", req.getTaskId());
        return AgentBuilderService.chat(workspaceId, req);
    }

    @Override
    @AccessKeyAuth
    @OperateLog(objectType = "operateTask", operateType = OperateType.CHAT, idField = "task_id")
    public BaseResp<String> operateTask(AgentBuilderOperateTaskReq req) {
        log.info("AgentBuilderOpenApiController.operateTask start. taskId = {}, operateType = {}", req.getTaskId(),
            req.getOperateType());
        return BaseResp.success(AgentBuilderService.operateTask(req));
    }

    @Override
    @AccessKeyAuth
    public BaseResp<List<DRMessageVo>> listMessage(AgentBuilderMessageListReq req) {
        log.info("AgentBuilderOpenApiController.listMessage start. taskId = {}", req.getTaskId());
        return BaseResp.success(AgentBuilderService.listMessage(req));
    }

    @Override
    @AccessKeyAuth
    @OperateLog(objectType = "downloadFile", operateType = OperateType.DOWNLOAD, idField = "fileId")
    public ResponseEntity<InputStreamResource> downloadFile(String taskId, String fileId, String targetFormat) {
        log.info("AgentBuilderOpenApiController.downloadFile start. taskId = {}, fileId = {}", taskId, fileId);
        return AgentBuilderService.downloadFile(taskId, fileId, targetFormat != null ? targetFormat : "");
    }
}
