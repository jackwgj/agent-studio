/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.common.enums;

import static com.openjiuwen.studio.agent.common.constant.Constants.LAKE_SEARCH_ENDPOINT;

import lombok.Getter;

/**
 * LakeSearch API调用地址
 *
 */
@Getter
public enum LakeSearchApi {
    /**
     * 创建知识库
     */
    CREATE_KNOWLEDGE_REPO(MethodType.POST, LAKE_SEARCH_ENDPOINT + "/knowledge-repo"),

    /**
     * 修改知识库
     */
    MODIFY_KNOWLEDGE_REPO(MethodType.PUT, LAKE_SEARCH_ENDPOINT + "/knowledge-repo/%s"),

    /**
     * 查询知识库列表
     */
    QUERY_KNOWLEDGE_REPOS(MethodType.GET, LAKE_SEARCH_ENDPOINT + "/knowledge-repo"),

    /**
     * 删除知识库
     */
    DELETE_KNOWLEDGE_REPO(MethodType.DELETE, LAKE_SEARCH_ENDPOINT + "/knowledge-repo"),

    /**
     * 查询指定知识库
     */
    RETRIEVE_KNOWLEDGE_REPO(MethodType.GET, LAKE_SEARCH_ENDPOINT + "/knowledge-repo/%s"),

    /**
     * 上传文件
     */
    UPLOAD_FILE(MethodType.POST, LAKE_SEARCH_ENDPOINT + "/%s/files"),

    /**
     * 删除文件
     */
    DELETE_FILE(MethodType.DELETE, LAKE_SEARCH_ENDPOINT + "/%s/files/%s"),

    /**
     * 批量删除文件
     */
    BATCH_DELETE_FILE(MethodType.DELETE, LAKE_SEARCH_ENDPOINT + "/%s/files/batch"),

    /**
     * 查询文件列表
     */
    QUERY_FILES(MethodType.GET, LAKE_SEARCH_ENDPOINT + "/%s/files/search"),

    /**
     * 下载文件
     */
    DOWNLOAD_FILE(MethodType.GET, LAKE_SEARCH_ENDPOINT + "/files/%s"),

    /**
     * 新增文件分片
     */
    CREATE_FILE_CHUNK(MethodType.POST, LAKE_SEARCH_ENDPOINT + "/files/%s/docs"),

    /**
     * 修改文件分片
     */
    MODIFY_FILE_CHUNK(MethodType.PUT, LAKE_SEARCH_ENDPOINT + "/files/%s/docs/%s"),

    /**
     * 删除文件分片
     */
    DELETE_FILE_CHUNK(MethodType.DELETE, LAKE_SEARCH_ENDPOINT + "/files/%s/docs"),

    /**
     * 查询文件分片
     */
    QUERY_FILE_CHUNKS(MethodType.GET, LAKE_SEARCH_ENDPOINT + "/files/%s/docs"),

    /**
     * 知识库检索
     */
    SEARCH_TEXT(MethodType.POST, LAKE_SEARCH_ENDPOINT + "/experience/searchtext"),

    /**
     * 创建FAQ
     */
    CREATE_FAQ(MethodType.POST, LAKE_SEARCH_ENDPOINT + "/faq"),

    /**
     * 更新FAQ
     */
    MODIFY_FAQ(MethodType.PUT, LAKE_SEARCH_ENDPOINT + "/faq"),

    /**
     * 删除FAQ
     */
    DELETE_FAQ(MethodType.DELETE, LAKE_SEARCH_ENDPOINT + "/faq/%s/%s"),

    /**
     * 批量删除faq
     */
    BATCH_DELETE_FAQ(MethodType.DELETE, LAKE_SEARCH_ENDPOINT + "/faq/batch"),

    /**
     * 查询FAQ详情
     */
    RETRIEVE_FAQ(MethodType.GET, LAKE_SEARCH_ENDPOINT + "/faq/%s"),

    /**
     * 查询知识库下FAQ列表
     */
    LIST_FAQS(MethodType.GET, LAKE_SEARCH_ENDPOINT + "/%s/faq"),

    /**
     * 启用知识库
     */
    START_KNOWLEDGE_REPO(MethodType.PUT, LAKE_SEARCH_ENDPOINT + "/knowledge-repo/%s/start"),

    /**
     * 停用知识库
     */
    STOP_KNOWLEDGE_REPO(MethodType.PUT, LAKE_SEARCH_ENDPOINT + "/knowledge-repo/%s/stop"),

    /**
     * 创建知识文档分层规则
     */
    CREATE_SEGMENT_RULE(MethodType.POST, LAKE_SEARCH_ENDPOINT + "/rule-regex"),

    /**
     * 修改知识文档分层规则
     */
    MODIFY_SEGMENT_RULE(MethodType.PUT, LAKE_SEARCH_ENDPOINT + "/rule-regex/%s"),

    /**
     * 查询知识文档分层规则列表
     */
    LIST_SEGMENT_RULE(MethodType.GET, LAKE_SEARCH_ENDPOINT + "/rule-regex"),

    /**
     * 删除知识文档分层规则
     */
    DELETE_SEGMENT_RULE(MethodType.DELETE, LAKE_SEARCH_ENDPOINT + "/rule-regex/%s"),

    /**
     * 注册模型
     */
    ADD_MODEL(MethodType.POST, LAKE_SEARCH_ENDPOINT + "/models"),

    /**
     * 修改模型
     */
    MODIFY_MODEL(MethodType.PUT, LAKE_SEARCH_ENDPOINT + "/models/%s"),

    /**
     * 查询模型列表
     */
    LIST_MODELS(MethodType.GET, LAKE_SEARCH_ENDPOINT + "/models/search"),

    /**
     * 删除模型
     */
    DELETE_MODEL(MethodType.DELETE, LAKE_SEARCH_ENDPOINT + "/models/%s"),

    /**
     * 批量创建任务
     */
    CREATE_TASKS(MethodType.POST, LAKE_SEARCH_ENDPOINT + "/%s/tasks"),

    /**
     * 查询任务列表
     */
    LIST_TASKS(MethodType.GET, LAKE_SEARCH_ENDPOINT + "/%s/tasks"),

    /**
     * 批量删除任务
     */
    BATCH_DELETE_TASKS(MethodType.DELETE, LAKE_SEARCH_ENDPOINT + "/%s/tasks/batch"),

    /**
     * 上传FAQ文档
     */
    UPLOAD_FAQ_FILE(MethodType.POST, LAKE_SEARCH_ENDPOINT + "/%s/faq/batch/upload"),

    /**
     * 下载FAQ文档
     */
    DOWNLOAD_FAQ_FILE(MethodType.GET, LAKE_SEARCH_ENDPOINT + "/faq/batch/%s"),

    /**
     * 删除FAQ文档
     */
    DELETE_FAQ_FILE(MethodType.DELETE, LAKE_SEARCH_ENDPOINT + "/%s/faq/batch/%s"),

    /**
     * 查询FAQ文档列表
     */
    LIST_FAQ_FILES(MethodType.GET, LAKE_SEARCH_ENDPOINT + "/%s/faq/batch/search"),

    /**
     * 创建FAQ文件切片
     */
    CREATE_FAQ_FILE_CHUNK(MethodType.POST, LAKE_SEARCH_ENDPOINT + "/faq/batch/%s/docs"),

    /**
     * 批量删除FAQ文件切片
     */
    BATCH_DELETE_FAQ_FILE_CHUNKS(MethodType.DELETE, LAKE_SEARCH_ENDPOINT + "/faq/batch/%s/docs"),

    /**
     * 修改FAQ文件分片
     */
    MODIFY_FAQ_FILE_CHUNK(MethodType.PUT, LAKE_SEARCH_ENDPOINT + "/faq/batch/%s/docs/%s"),

    /**
     * 查询FAQ文档切片列表
     */
    LIST_FAQ_FILE_CHUNKS(MethodType.GET, LAKE_SEARCH_ENDPOINT + "/faq/batch/%s/docs"),

    /**
     * 查询知识库标签列表
     */
    LIST_KNOWLEDGE_REPO_TAGS(MethodType.GET, LAKE_SEARCH_ENDPOINT + "/%s/labels");

    private final MethodType method;

    private final String endpoint;

    LakeSearchApi(MethodType method, String endpoint) {
        this.method = method;
        this.endpoint = endpoint;
    }
}
