/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.rce.models;

import lombok.Data;

import java.util.List;

/**
 * 查询工作空间列表响应体
 *
 */
@Data
public class WorkspaceListResp {
    /**
     * 工作空间数量
     */
    private Integer count;

    /**
     * 工作空间列表
     */
    private List<Workspace> workspaces;

    @Data
    public static class Workspace {
        /**
         * 工作空间id
         */
        private String id;

        /**
         * 工作空间名
         */
        private String name;
    }
}
