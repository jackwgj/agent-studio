/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.app.util;

import com.alibaba.fastjson2.JSON;
import com.openjiuwen.studio.agent.space.api.vo.AgentType;
import com.openjiuwen.studio.agent.space.api.vo.AgentBuilderFileVo;
import com.openjiuwen.studio.agent.space.api.vo.AgentBuilderTaskDisplayVo;
import com.openjiuwen.studio.agent.space.api.vo.AgentBuilderTaskInfoVo;
import com.openjiuwen.studio.agent.space.api.vo.request.AgentBuilderCreateTaskReq;
import com.openjiuwen.studio.agent.space.app.enums.DRTaskStatusType;
import com.openjiuwen.studio.agent.space.app.enums.AgentBuilderTaskStatusType;
import com.openjiuwen.studio.agent.space.dao.entity.AgentBuilderFileEntity;
import com.openjiuwen.studio.agent.space.dao.entity.AgentBuilderTaskEntity;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * AgentBudiler 实体类转换工具
 */
@Slf4j
@Service
public class AgentBuilderTransformUtil {
    public AgentBuilderTaskEntity createTask2TaskEntity(AgentBuilderCreateTaskReq req) {
        AgentBuilderTaskEntity taskEntity = new AgentBuilderTaskEntity();
        BeanUtils.copyProperties(req, taskEntity);
        taskEntity.setId(AppCommonUtils.getUUID())
            .setName(req.getTaskName().length() > 64 ? req.getTaskName().substring(0, 64) : req.getTaskName())
            .setStatus(AgentType.BUILT_IN.getCode() == req.getAgentType()
                ? Integer.valueOf(AgentBuilderTaskStatusType.INITIATING.getStatus())
                : (AgentType.DEEP_RESEARCH.getCode() == req.getAgentType()
                    ? DRTaskStatusType.INITIATING.getStatus()
                    : null))
            .setAgentType(req.getAgentType())
            .setDisplayInfo(req.getTaskName())
            .setAgentId(req.getAgentId())
            .setRunType(req.getRunType());
        AgentBuilderTaskDisplayVo display = new AgentBuilderTaskDisplayVo();
        taskEntity.setDisplayInfo(JSON.toJSONString(display));
        AppCommonUtils.setCommonCreateProperties(taskEntity);
        return taskEntity;
    }

    @SuppressWarnings("unchecked")
    public AgentBuilderTaskInfoVo taskEntity2TaskInfoVo(AgentBuilderTaskEntity taskEntity) {
        AgentBuilderTaskInfoVo task = new AgentBuilderTaskInfoVo();
        BeanUtils.copyProperties(taskEntity, task);
        task.setCreateTime(taskEntity.getCreatedDate())
            .setUpdateTime(taskEntity.getLastUpdatedDate())
            .setDisplayInfo(JSON.parseObject(taskEntity.getDisplayInfo(), AgentBuilderTaskDisplayVo.class))
            .setExtraAgentConfig(JSON.parseObject(taskEntity.getExtraAgentConfig(), Map.class));
        return task;
    }

    public AgentBuilderFileEntity fileVo2FileEntity(AgentBuilderFileVo AgentBuilderFileVo) {
        AgentBuilderFileEntity AgentBuilderFileEntity = new AgentBuilderFileEntity();
        BeanUtils.copyProperties(AgentBuilderFileVo, AgentBuilderFileEntity);
        AgentBuilderFileEntity.setId(AppCommonUtils.getUUID()).setType(1);
        AppCommonUtils.setCommonCreateProperties(AgentBuilderFileEntity);
        return AgentBuilderFileEntity;
    }
}
