/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.bo;

import com.openjiuwen.studio.agent.manager.dto.WorkflowValidationVOErrors;
import com.openjiuwen.studio.agent.manager.service.WorkflowValidationService;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 工作流校验配置包装类
 *
 */
@Data
@Builder
public class ValidateConfigWrapper {
    /**
     * 校验节点的定义
     */
    WorkflowValidationService.Node node;

    /**
     * 校验节点和当前节点可选输入的映射
     */
    Map<String, List<String>> inputReference;

    /**
     * 模型id列表
     */
    Set<String> modelIds;

    /**
     * 错误信息
     */
    List<WorkflowValidationVOErrors> errors;
}
