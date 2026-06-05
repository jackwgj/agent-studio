/*
 *  Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

package com.openjiuwen.studio.agent.runtime.rce.client;

import com.openjiuwen.studio.agent.common.dto.knowledge.SearchTextReq;
import com.openjiuwen.studio.agent.runtime.dto.knowledge.ListFilesRsp;
import com.openjiuwen.studio.agent.runtime.rce.model.SearchTextResp;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;
import java.util.List;

/**
 * KooSearch知识库相关API client
 *
 * @since 2024-04-19
 */
@FeignClient(name = "CssUniSearch", url = "http://localhost:8080")
public interface CssUniSearchClient {
    /**
     * 检索知识库
     *
     * @param projectId 项目id
     * @param applicationId 应用id
     * @param searchTextReq 检索请求
     * @return SearchTextResp 检索结果
     */
    @PostMapping("/v1/{project_id}/applications/{application_id}/uni-search/experience/searchtext")
    SearchTextResp searchText(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId, @RequestBody SearchTextReq searchTextReq);

    /**
     * 下载指定文档
     *
     * @param projectId 项目id
     * @param applicationId 应用id
     * @param fileId 文档id
     * @return MultipartFile
     */
    @GetMapping("/v1/{project_id}/applications/{application_id}/uni-search/files/{file_id}")
    ResponseEntity<byte[]> downloadFiles(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId, @RequestParam("repo_id") String repoId,
        @PathVariable(value = "file_id") String fileId);

    /**
     * 查询分块中的图片
     *
     * @param projectId
     * @param applicationId
     * @param imageId
     * @return InputStream
     */
    @GetMapping("/v1/{project_id}/applications/{application_id}/uni-search/img/{image_id}")
    Resource queryImage(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId, @RequestParam("repo_id") String repoId,
        @RequestParam("file_id") String fileId, @PathVariable(value = "image_id") String imageId);

    /**
     * 分页查询文档列表
     *
     * @param projectId 项目id
     * @param applicationId 应用id
     * @param repoId 知识库id
     * @param pageNum 页数
     * @param pageSize 每页数量
     * @param fileName 文档名
     * @param fileType 文档类型
     * @param fileStatus 文档状态
     * @return ListFilesRsp
     */
    @GetMapping("/v1/{project_id}/applications/{application_id}/uni-search/{repo_id}/files/search")
    ListFilesRsp searchFiles(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId, @PathVariable(value = "repo_id") String repoId,
        @RequestParam(value = "page_num") Integer pageNum, @RequestParam(value = "page_size") Integer pageSize,
        @RequestParam(value = "file_name") String fileName, @RequestParam(value = "file_type") String fileType,
        @RequestParam(value = "file_status") String fileStatus);

    /**
     * 分页查询FAQ文档列表
     *
     * @param projectId 项目id
     * @param applicationId 应用id
     * @param repoId 知识库id
     * @param pageNum 页数
     * @param pageSize 每页数量
     * @param fileName 文档名
     * @param fileStatus 文档状态
     * @param ids 按id列表精确查询
     * @return ListFilesRsp
     */
    @GetMapping("/v1/{project_id}/applications/{application_id}/uni-search/{repo_id}/faq/batch/search")
    ListFilesRsp searchFaqFiles(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId, @PathVariable(value = "repo_id") String repoId,
        @RequestParam(value = "page_num") Integer pageNum, @RequestParam(value = "page_size") Integer pageSize,
        @RequestParam(value = "file_name") String fileName, @RequestParam(value = "file_status") String fileStatus,
        @RequestParam(value = "ids") List<String> ids);

    /**
     * 下载指定FAQ文档
     *
     * @param projectId 项目id
     * @param applicationId 应用id
     * @param fileId 文档id
     * @return MultipartFile
     */
    @GetMapping("/v1/{project_id}/applications/{application_id}/uni-search/faq/batch/{file_id}")
    ResponseEntity<byte[]> downloadFaqFiles(URI baseUrl, @RequestHeader HttpHeaders headers,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId, @RequestParam("repo_id") String repoId,
        @PathVariable(value = "file_id") String fileId);

}
