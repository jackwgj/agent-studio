/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.workflow.jiuwen.models;

import com.openjiuwen.studio.agent.manager.dto.WorkflowFieldVO;

import lombok.Data;

import java.util.List;

/**
 * JiuWen branch置类型
 *
 */
@Data
public class BranchConfig {
    private String logic;
    @Data
    public static class Condition {
        private String operator;

        private WorkflowFieldVO left;

        private WorkflowFieldVO right;
    }
    private List<Condition> conditions;
}
