/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.rce.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * UniFactory查询文档解析任务响应体
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UniFactoryShowTaskRsp {
    @JsonProperty("task_status")
    private String taskStatus;

    /**
     * 任务描述，主要为错误信息
     */
    @JsonProperty("task_desc")
    private String taskDesc;

    private TaskResult result;

    /**
     * 解析任务完成时的解析结果
     */
    @Data
    public static class TaskResult {
        @JsonProperty("doc_id")
        private String docId;

        @JsonProperty("doc_name")
        private String docName;

        @JsonProperty("doc_type")
        private String docType;

        private List<PageResult> pages;
    }

    /**
     * 文档页面信息
     */
    @Data
    public static class PageResult {
        @JsonProperty("page_num")
        private int pageNum;

        private List<Component> components;
    }

    /**
     * 页面中的段落信息
     */
    @Data
    public static class Component {
        private String id;

        private String text;

        private String title;

        @JsonProperty("component_num")
        private int componentNum;

        @JsonProperty("original_title")
        private String originalTitle;
    }
}
