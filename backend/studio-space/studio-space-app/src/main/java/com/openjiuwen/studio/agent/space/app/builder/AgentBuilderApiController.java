/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.app.builder;

import com.openjiuwen.studio.agent.space.api.auth.IamTokenAuth;
import com.openjiuwen.studio.agent.space.api.controller.AgentBuilderApi;
import com.openjiuwen.studio.agent.space.api.vo.AgentBuilderFileExtConfigVo;
import com.openjiuwen.studio.agent.space.api.vo.AgentBuilderTaskInfoVo;
import com.openjiuwen.studio.agent.space.api.vo.request.PageInfo;
import com.openjiuwen.studio.agent.space.api.vo.request.AgentBuilderChatReq;
import com.openjiuwen.studio.agent.space.api.vo.request.AgentBuilderCreateTaskReq;
import com.openjiuwen.studio.agent.space.api.vo.request.AgentBuilderDeleteMessageReq;
import com.openjiuwen.studio.agent.space.api.vo.request.AgentBuilderMessageListReq;
import com.openjiuwen.studio.agent.space.api.vo.request.AgentBuilderOperateTaskReq;
import com.openjiuwen.studio.agent.space.api.vo.request.AgentBuilderQueryTaskReq;
import com.openjiuwen.studio.agent.space.api.vo.request.AgentBuilderUpdateTaskReq;
import com.openjiuwen.studio.agent.space.api.vo.response.AgentListResp;
import com.openjiuwen.studio.agent.space.api.vo.response.AgentBuilderTaskListRsp;
import com.openjiuwen.studio.agent.space.api.vo.response.deepresearch.DRMessageVo;
import com.openjiuwen.studio.agent.space.app.annotation.operatelog.OperateLog;
import com.openjiuwen.studio.agent.space.app.annotation.operatelog.OperateType;
import com.openjiuwen.studio.agent.space.app.service.builder.AgentBuilderService;
import com.openjiuwen.studio.agent.space.common.model.BaseResp;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Slf4j
@RestController
public class AgentBuilderApiController implements AgentBuilderApi {
    @Resource
    private AgentBuilderService AgentBuilderService;

    @Override
    @IamTokenAuth()
    @OperateLog(objectType = "createTask", operateType = OperateType.CREATE, idField = "task_id")
    public BaseResp<String> createTask(AgentBuilderCreateTaskReq req) {
        log.info("AgentBuilderApiController.createTask start");
        return BaseResp.success(AgentBuilderService.createTask(req));
    }

    @Override
    @IamTokenAuth()
    public BaseResp<AgentBuilderTaskListRsp> listTask(AgentBuilderQueryTaskReq req) {
        log.info("AgentBuilderApiController.listTask start");
        return BaseResp.success(AgentBuilderService.listTask(req));
    }

    @Override
    @IamTokenAuth()
    public BaseResp<String> renameTask(AgentBuilderUpdateTaskReq req) {
        log.info("AgentBuilderApiController.renameTask start, taskId = {}", req.getTaskId());
        return BaseResp.success(AgentBuilderService.renameTask(req));
    }

    @Override
    @IamTokenAuth()
    public BaseResp<AgentBuilderTaskInfoVo> detailTask(String taskId) {
        log.info("AgentBuilderApiController.detailTask start. taskId = {}", taskId);
        return BaseResp.success(AgentBuilderService.detailTask(taskId));
    }

    @Override
    @IamTokenAuth()
    @OperateLog(objectType = "operateTask", operateType = OperateType.UPDATE, idField = "task_id")
    public BaseResp<String> operateTask(AgentBuilderOperateTaskReq req) {
        log.info("AgentBuilderApiController.operateTask start. taskId = {}, operateType = {}", req.getTaskId(),
            req.getOperateType());
        return BaseResp.success(AgentBuilderService.operateTask(req));
    }

    @Override
    @IamTokenAuth()
    @OperateLog(objectType = "chat", operateType = OperateType.CHAT, idField = "task_id")
    public SseEmitter chat(String workspaceId, AgentBuilderChatReq req) {
        log.info("AgentBuilderApiController.chat start. taskId = {}", req.getTaskId());
        return AgentBuilderService.chat(workspaceId, req);
    }

    @Override
    @IamTokenAuth()
    public BaseResp<List<DRMessageVo>> listMessage(AgentBuilderMessageListReq req) {
        log.info("AgentBuilderApiController.listMessage start. taskId = {}", req.getTaskId());
        return BaseResp.success(AgentBuilderService.listMessage(req));
    }

    @Override
    public BaseResp<?> deleteMessage(@RequestBody @Valid AgentBuilderDeleteMessageReq req) {
        log.info("AgentBuilderApiController.deleteMessage start. messageId = {}", req.getMessageId());
        return BaseResp.success(AgentBuilderService.deleteMessage(req));
    }

    @OperateLog(objectType = "uploadFile", operateType = OperateType.CREATE, idField = "fileIds")
    @Override
    @IamTokenAuth()
    public BaseResp<String> uploadFile(MultipartFile file, Boolean isTemplate) {
        log.info("AgentBuilderApiController.uploadFile start.");
        AgentBuilderFileExtConfigVo extConfigVo = new AgentBuilderFileExtConfigVo();
        extConfigVo.setTemplate(isTemplate);
        return BaseResp.success(AgentBuilderService.uploadFile(file, extConfigVo));
    }

    @Override
    @IamTokenAuth()
    @OperateLog(objectType = "downloadFile", operateType = OperateType.DOWNLOAD, idField = "fileId")
    public ResponseEntity<InputStreamResource> downloadFile(String taskId, String fileId, String targetFormat) {
        log.info("AgentBuilderApiController.downloadFile start.");
        return AgentBuilderService.downloadFile(taskId, fileId, targetFormat != null ? targetFormat : "");
    }

    @Override
    public BaseResp<AgentListResp> listAgent(String name, Integer agentType, PageInfo pageInfo) {
        log.info("AgentBuilderApiController.listAgent start.");
        return BaseResp.success(AgentBuilderService.listAgent(name, agentType, pageInfo));
    }
}
