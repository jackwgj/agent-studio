/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */
package com.openjiuwen.studio.agent.runtime.rce.client;

import com.openjiuwen.studio.agent.runtime.rce.model.UniFactoryShowTaskRsp;
import com.openjiuwen.studio.agent.runtime.rce.model.UniFactoryUploadFileRsp;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;

@FeignClient(name = "uniFactory", url = "${feign.client.config.uniFactory.url:}")
public interface UniFactoryClient {
    @PostMapping(value = "/v1/{project_id}/applications/{application_id}/doc-search/files",
        consumes = "multipart/form-data")
    UniFactoryUploadFileRsp uploadFile(@RequestHeader("X-Auth-Token") String authToken,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId, @RequestPart(value = "file") Resource file,
        @RequestParam(value = "ocr") Boolean ocr, @RequestParam(value = "ocr_enabled") Boolean ocrEnabled,
        @RequestParam(value = "image_enabled") Boolean imageEnabled,
        @RequestParam(value = "image_conf") String imageConf, @RequestParam(value = "split_mode") String splitMode,
        @RequestParam(value = "catalog_enabled") Boolean catalogEnabled);

    @GetMapping("/v1/{project_id}/applications/{application_id}/doc-search/tasks/{task_id}")
    UniFactoryShowTaskRsp showTaskResult(@RequestHeader("X-Auth-Token") String authToken,
        @PathVariable(value = "project_id") String projectId,
        @PathVariable(value = "application_id") String applicationId, @PathVariable(value = "task_id") String taskId);
}
