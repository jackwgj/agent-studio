/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.space.api.vo.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.util.List;

/**
 * 查询工作流agent返回体
 */
@Data
public class QueryManagerWorkflowResp {
    /**
     * 成功删除的taskId
     */
    @JsonProperty("task_ids")
    private List<String> taskIds;

    private int count;

    private List<WorkflowListBean> workflowList;

    @Data
    public static class WorkflowListBean {
        @JsonProperty("workflow_id")
        private String workflowId;

        private String name;

        private String avatar;

        private String code;

        private String description;

        private String creator;

        private String type;

        private String group;

        private String url;

        private int status;

        @JsonProperty("customize_node")
        private boolean customizeNode;
    }
}
